package controller;

import model.*;
import sound.SoundManager;
import java.util.ArrayList;
import java.util.List;

/**
 * JeuController : couche Controller du patron MVC.
 *
 * <p>Responsabilités :</p>
 * <ul>
 *   <li>Recevoir les clics souris de la vue et les traduire en coups</li>
 *   <li>Gérer la sélection de pièce et les destinations possibles</li>
 *   <li>Déclencher le tour du Bot dans un thread séparé</li>
 *   <li>Faire avancer le chrono et gérer l'expiration du temps</li>
 *   <li>Notifier la vue via des callbacks {@link Runnable}</li>
 * </ul>
 *
 * <p><b>Séparation MVC stricte</b> : cette classe ne contient aucune
 * référence directe à un composant Swing. La communication vers la vue
 * se fait exclusivement via des Runnables injectés par
 * {@link #setCallbacks(Runnable, Runnable, Runnable)}.</p>
 */
public class JeuController {

    /** Le modèle de jeu (règles, état, historique). */
    private final Jeu    jeu;

    /** Le bot adversaire, ou {@code null} en mode humain vs humain. */
    private final Bot    bot;

    /** Le compte à rebours de 2 minutes par joueur. */
    private final Chrono chrono;

    /** Thème visuel actif (Classique ou Obsidian). */
    private Theme        theme;

    /** Ligne de la pièce actuellement sélectionnée (-1 si aucune). */
    private int selectedLigne   = -1;

    /** Colonne de la pièce actuellement sélectionnée (-1 si aucune). */
    private int selectedColonne = -1;

    /** Callback déclenché après chaque mise à jour de l'état de jeu. */
    private Runnable onUpdate;

    /** Callback déclenché quand le bot commence à calculer. */
    private Runnable onBotThink;

    /** Callback déclenché quand le temps d'un joueur est écoulé. */
    private Runnable onTimeOut;

    /** Durée du chrono par joueur en secondes (2 minutes). */
    private static final int SECONDES_PAR_JOUEUR = 120;

    // ── Constructeur ──────────────────────────────────────────────────────────

    /**
     * Crée le contrôleur en reliant le modèle, le bot et le thème.
     *
     * @param jeu   le modèle de jeu
     * @param bot   le bot (peut être {@code null} pour un mode 2 joueurs humains)
     * @param theme le thème visuel initial
     */
    public JeuController(Jeu jeu, Bot bot, Theme theme) {
        this.jeu    = jeu;
        this.bot    = bot;
        this.theme  = theme;
        this.chrono = new Chrono(SECONDES_PAR_JOUEUR);
    }

    /**
     * Injecte les callbacks de notification vers la vue.
     * Doit être appelé après construction, avant toute interaction.
     *
     * @param onUpdate   appelé après chaque changement d'état (repaint de la vue)
     * @param onBotThink appelé quand le bot commence à réfléchir
     * @param onTimeOut  appelé quand le temps d'un joueur est écoulé
     */
    public void setCallbacks(Runnable onUpdate, Runnable onBotThink, Runnable onTimeOut) {
        this.onUpdate   = onUpdate;
        this.onBotThink = onBotThink;
        this.onTimeOut  = onTimeOut;
    }

    // ── Gestion des clics ──────────────────────────────────────────────────────

    /**
     * Traite un clic sur la case (ligne, col) par le joueur humain.
     *
     * <p>Comportement :</p>
     * <ul>
     *   <li>Si aucune pièce n'est sélectionnée et qu'on clique sur une pièce
     *       jouable → elle devient sélectionnée</li>
     *   <li>Si une pièce est déjà sélectionnée et qu'on clique sur une destination
     *       valide → le coup est joué</li>
     *   <li>Si le coup échoue → on tente de re-sélectionner la case cliquée</li>
     * </ul>
     *
     * @param ligne ligne de la case cliquée (0-7)
     * @param col   colonne de la case cliquée (0-7)
     * @return {@code true} si un changement d'état a eu lieu
     */
    public boolean handleCellClick(int ligne, int col) {
        if (jeu.isPartieTerminee()) return false;
        if (estTourDuBot()) return false; // ignorer les clics pendant le tour du bot

        Piece p = jeu.getPlateau().getPiece(ligne, col);
        boolean changed = false;

        if (selectedLigne >= 0) {
            // Tentative de déplacement vers la case cliquée
            boolean moved = jeu.jouerCoup(selectedLigne, selectedColonne, ligne, col);
            changed = true;
            selectedLigne = -1; selectedColonne = -1;

            if (moved) {
                SoundManager.playDeplacement();
                syncChronoApresJeu();
                // Capture multiple : re-sélectionner automatiquement la pièce active
                if (jeu.getPieceEnCours() != null) {
                    Piece pc = jeu.getPieceEnCours();
                    selectedLigne   = pc.getLigne();
                    selectedColonne = pc.getColonne();
                }
                if (jeu.isPartieTerminee()) SoundManager.playFinPartie();
            } else if (p != null && p.getCouleur() == jeu.getJoueurActuel().getCouleur()
                    && !jeu.getCoupsValides(ligne, col).isEmpty()) {
                // Le coup était invalide, mais on clique sur une autre pièce jouable
                selectedLigne = ligne; selectedColonne = col;
            }
        } else {
            // Première sélection : doit être une pièce du joueur actuel avec un coup disponible
            if (p != null && p.getCouleur() == jeu.getJoueurActuel().getCouleur()
                    && !jeu.getCoupsValides(ligne, col).isEmpty()) {
                selectedLigne = ligne; selectedColonne = col; changed = true;
            }
        }

        if (onUpdate != null) onUpdate.run();

        // Si c'est maintenant le tour du bot, déclencher son calcul
        if (!jeu.isPartieTerminee() && estTourDuBot()) lancerTourBot();

        return changed;
    }

    // ── Tour du Bot ───────────────────────────────────────────────────────────

    /**
     * Indique si c'est actuellement le tour du bot (joueur Noir + bot présent).
     *
     * @return {@code true} si le bot doit jouer
     */
    public boolean estTourDuBot() {
        return bot != null
            && jeu.getJoueurActuel().getCouleur() == Piece.Couleur.NOIR
            && !jeu.isPartieTerminee();
    }

    /**
     * Lance le calcul du coup du bot dans un thread daemon séparé.
     * Cela évite de bloquer l'EDT (Event Dispatch Thread) de Swing pendant
     * le calcul minimax qui peut prendre jusqu'à ~1 seconde.
     */
    public void lancerTourBot() {
        if (onBotThink != null) onBotThink.run();
        Thread t = new Thread(() -> {
            // Délai artificiel pour simuler la réflexion et laisser la vue se rafraîchir
            try {
                Thread.sleep(bot.getNiveau() == Bot.Niveau.FACILE ? 300
                           : bot.getNiveau() == Bot.Niveau.MOYEN  ? 600 : 1000);
            } catch (InterruptedException ignored) {}

            // Calcul du meilleur coup
            Coup c = bot.choisirCoup(jeu);
            if (c != null) {
                jeu.jouerCoup(c.getFromLigne(), c.getFromColonne(),
                              c.getToLigne(),   c.getToColonne());
                SoundManager.playDeplacement();
                syncChronoApresJeu();

                // Gérer les captures multiples du bot automatiquement
                while (jeu.getPieceEnCours() != null) {
                    Piece pc = jeu.getPieceEnCours();
                    List<Coup> suites = jeu.getCoupsValides(pc.getLigne(), pc.getColonne());
                    if (suites.isEmpty()) break;
                    Coup suite = suites.get(0);
                    jeu.jouerCoup(suite.getFromLigne(), suite.getFromColonne(),
                                  suite.getToLigne(),   suite.getToColonne());
                    SoundManager.playCapture();
                }
                if (jeu.isPartieTerminee()) SoundManager.playFinPartie();
            }
            if (onUpdate != null) onUpdate.run();
        }, "bot-thread");
        t.setDaemon(true);
        t.start();
    }

    // ── Chrono ────────────────────────────────────────────────────────────────

    /**
     * Décrémente d'une seconde le chrono du joueur actuel.
     * Appelé toutes les secondes par le Swing Timer de {@link view.FenetreJeu}.
     * Si le temps est écoulé, force la défaite du joueur actuel.
     */
    public void tickChrono() {
        if (jeu.isPartieTerminee() || !chrono.estActif()) return;
        chrono.tick();
        Piece.Couleur actif = jeu.getJoueurActuel().getCouleur();
        if (chrono.estEpuise(actif)) {
            chrono.arreter();
            jeu.forcerDefaite(jeu.getJoueurActuel());
            SoundManager.playTempsEcoule();
            if (onTimeOut != null) onTimeOut.run();
        } else {
            if (chrono.estCritique(actif)) SoundManager.playChrono();
        }
        if (onUpdate != null) onUpdate.run();
    }

    /** Synchronise le chrono sur le joueur dont c'est le nouveau tour. */
    private void syncChronoApresJeu() {
        chrono.changerJoueur(jeu.getJoueurActuel().getCouleur());
    }

    /** Démarre le décompte du chrono (appelé depuis FenetreJeu au lancement). */
    public void demarrerChrono() { chrono.demarrer(); }

    /** Arrête le décompte (appelé en fin de partie ou en pause). */
    public void arreterChrono()  { chrono.arreter(); }

    // ── Réinitialisation ──────────────────────────────────────────────────────

    /**
     * Remet à zéro le jeu, la sélection et le chrono.
     * Appelé par le bouton "Nouvelle partie".
     */
    public void recommencer() {
        jeu.recommencer();
        selectedLigne = selectedColonne = -1;
        chrono.reset(SECONDES_PAR_JOUEUR);
        chrono.demarrer();
    }

    // ── Accesseurs pour la vue ─────────────────────────────────────────────────

    /** @return la ligne de la pièce sélectionnée (-1 si aucune) */
    public int getSelectedLigne()   { return selectedLigne; }

    /** @return la colonne de la pièce sélectionnée (-1 si aucune) */
    public int getSelectedColonne() { return selectedColonne; }

    /**
     * @return la liste des coups disponibles pour la pièce sélectionnée,
     *         vide si aucune pièce n'est sélectionnée
     */
    public List<Coup> getCoupsDisponibles() {
        if (selectedLigne < 0) return new ArrayList<>();
        return jeu.getCoupsValides(selectedLigne, selectedColonne);
    }

    /** @return le modèle de jeu */
    public Jeu    getJeu()    { return jeu; }

    /** @return le chrono du jeu */
    public Chrono getChrono() { return chrono; }

    /** @return le thème visuel actif */
    public Theme  getTheme()  { return theme; }

    /**
     * Change le thème visuel actif.
     * @param t le nouveau thème
     */
    public void setTheme(Theme t) { this.theme = t; }

    /** Efface la sélection courante (aucune pièce sélectionnée). */
    public void resetSelection() { selectedLigne = selectedColonne = -1; }
}
