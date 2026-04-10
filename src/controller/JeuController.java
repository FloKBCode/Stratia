package controller;

import model.*;
import sound.SoundManager;
import java.util.ArrayList;
import java.util.List;

/**
 * JeuController : couche Controller (MVC).
 * Gère sélection, coups humains, tours du Bot, Chrono.
 * Aucune référence directe à Swing — communique via Runnable callbacks.
 */
public class JeuController {

    private final Jeu    jeu;
    private final Bot    bot;           // null si vs humain
    private final Chrono chrono;
    private Theme        theme;

    private int selectedLigne   = -1;
    private int selectedColonne = -1;

    private Runnable onUpdate;    // appelé après chaque changement d'état
    private Runnable onBotThink;  // appelé quand le bot commence à réfléchir
    private Runnable onTimeOut;   // appelé quand le temps d'un joueur est écoulé

    private static final int SECONDES_PAR_JOUEUR = 120; // 2 minutes

    // ── Constructeur ──────────────────────────────────────────────────────────

    public JeuController(Jeu jeu, Bot bot, Theme theme) {
        this.jeu    = jeu;
        this.bot    = bot;
        this.theme  = theme;
        this.chrono = new Chrono(SECONDES_PAR_JOUEUR);
    }

    public void setCallbacks(Runnable onUpdate, Runnable onBotThink, Runnable onTimeOut) {
        this.onUpdate   = onUpdate;
        this.onBotThink = onBotThink;
        this.onTimeOut  = onTimeOut;
    }

    // ── Clic humain ───────────────────────────────────────────────────────────

    public boolean handleCellClick(int ligne, int col) {
        if (jeu.isPartieTerminee()) return false;
        if (estTourDuBot()) return false;   // ignorer clics pendant le tour bot

        Piece p = jeu.getPlateau().getPiece(ligne, col);
        boolean changed = false;

        if (selectedLigne >= 0) {
            boolean moved = jeu.jouerCoup(selectedLigne, selectedColonne, ligne, col);
            changed = true;
            selectedLigne = -1; selectedColonne = -1;

            if (moved) {
                SoundManager.playDeplacement();
                syncChronoApresJeu();
                // Multi-capture : re-sélectionner automatiquement
                if (jeu.getPieceEnCours() != null) {
                    Piece pc = jeu.getPieceEnCours();
                    selectedLigne   = pc.getLigne();
                    selectedColonne = pc.getColonne();
                }
                if (jeu.isPartieTerminee()) SoundManager.playFinPartie();
            } else if (p != null && p.getCouleur() == jeu.getJoueurActuel().getCouleur()
                    && !jeu.getCoupsValides(ligne, col).isEmpty()) {
                selectedLigne = ligne; selectedColonne = col;
            }
        } else {
            if (p != null && p.getCouleur() == jeu.getJoueurActuel().getCouleur()
                    && !jeu.getCoupsValides(ligne, col).isEmpty()) {
                selectedLigne = ligne; selectedColonne = col; changed = true;
            }
        }

        if (onUpdate != null) onUpdate.run();

        // Déclencher le tour du bot si c'est son tour
        if (!jeu.isPartieTerminee() && estTourDuBot()) lancerTourBot();

        return changed;
    }

    // ── Tour du Bot ───────────────────────────────────────────────────────────

    public boolean estTourDuBot() {
        return bot != null
            && jeu.getJoueurActuel().getCouleur() == Piece.Couleur.NOIR
            && !jeu.isPartieTerminee();
    }

    /** Lance le calcul du coup bot dans un thread séparé (ne bloque pas l'EDT). */
    public void lancerTourBot() {
        if (onBotThink != null) onBotThink.run();
        Thread t = new Thread(() -> {
            // Délai artificiel pour simuler la réflexion
            try { Thread.sleep(bot.getNiveau() == Bot.Niveau.FACILE ? 300
                             : bot.getNiveau() == Bot.Niveau.MOYEN  ? 600 : 1000);
            } catch (InterruptedException ignored) {}

            Coup c = bot.choisirCoup(jeu);
            if (c != null) {
                jeu.jouerCoup(c.getFromLigne(), c.getFromColonne(),
                              c.getToLigne(),   c.getToColonne());
                SoundManager.playDeplacement();
                syncChronoApresJeu();
                // Enchaîner les multi-captures du bot
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

    /** Appelé toutes les secondes par le Swing Timer de FenetreJeu. */
    public void tickChrono() {
        if (jeu.isPartieTerminee() || !chrono.estActif()) return;
        chrono.tick();
        Piece.Couleur actif = jeu.getJoueurActuel().getCouleur();
        if (chrono.estEpuise(actif)) {
            chrono.arreter();
            Joueur perdant = jeu.getJoueurActuel();
            jeu.forcerDefaite(perdant);
            SoundManager.playTempsEcoule();
            if (onTimeOut != null) onTimeOut.run();
        } else {
            if (chrono.estCritique(actif)) SoundManager.playChrono();
        }
        if (onUpdate != null) onUpdate.run();
    }

    private void syncChronoApresJeu() {
        chrono.changerJoueur(jeu.getJoueurActuel().getCouleur());
    }

    public void demarrerChrono() { chrono.demarrer(); }
    public void arreterChrono()  { chrono.arreter(); }

    // ── Réinitialisation ──────────────────────────────────────────────────────

    public void recommencer() {
        jeu.recommencer();
        selectedLigne = selectedColonne = -1;
        chrono.reset(SECONDES_PAR_JOUEUR);
        chrono.demarrer();
    }

    // ── Accesseurs ────────────────────────────────────────────────────────────

    public int         getSelectedLigne()   { return selectedLigne; }
    public int         getSelectedColonne() { return selectedColonne; }
    public List<Coup>  getCoupsDisponibles(){
        if (selectedLigne < 0) return new ArrayList<>();
        return jeu.getCoupsValides(selectedLigne, selectedColonne);
    }
    public Jeu    getJeu()    { return jeu; }
    public Chrono getChrono() { return chrono; }
    public Theme  getTheme()  { return theme; }
    public void   setTheme(Theme t) { this.theme = t; }
    public void   resetSelection()  { selectedLigne = selectedColonne = -1; }
}
