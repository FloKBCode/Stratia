package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Bot : intelligence artificielle jouant pour le joueur Noir.
 *
 * <p>Trois niveaux de difficulté sont disponibles :</p>
 * <ul>
 *   <li><b>FACILE</b> : coup entièrement aléatoire</li>
 *   <li><b>MOYEN</b>  : algorithme glouton (capture &gt; avance &gt; aléatoire)</li>
 *   <li><b>DIFFICILE</b> : algorithme Minimax avec élagage Alpha-Bêta (profondeur 6)</li>
 * </ul>
 *
 * <p>Le minimax utilise une <b>copie profonde</b> de {@link Jeu} ({@code new Jeu(src)})
 * pour simuler les coups futurs sans altérer la partie réelle.</p>
 */
public class Bot {

    /**
     * Enumération des niveaux de difficulté du bot.
     * Chaque niveau possède un libellé affiché dans l'interface.
     */
    public enum Niveau {
        FACILE("Facile"),
        MOYEN("Moyen"),
        DIFFICILE("Difficile");

        /** Libellé affiché dans l'écran d'accueil. */
        public final String label;
        Niveau(String l) { this.label = l; }
    }

    /** Profondeur de recherche pour l'algorithme minimax (niveau Difficile). */
    private static final int PROFONDEUR_DIFFICILE = 6;

    /** Niveau de difficulté sélectionné pour cette instance. */
    private final Niveau niveau;

    /** Générateur de nombres aléatoires pour les niveaux Facile et Moyen. */
    private final Random rng = new Random();

    /**
     * Crée un bot avec le niveau de difficulté spécifié.
     *
     * @param niveau le niveau de difficulté choisi par le joueur
     */
    public Bot(Niveau niveau) { this.niveau = niveau; }

    /** @return le niveau de difficulté de ce bot */
    public Niveau getNiveau() { return niveau; }

    // ── Point d'entrée public ──────────────────────────────────────────────────

    /**
     * Calcule et retourne le meilleur coup disponible selon le niveau du bot.
     * Appelé par {@link controller.JeuController} dans un thread séparé.
     *
     * @param jeu l'état courant de la partie
     * @return le coup choisi, ou {@code null} si aucun coup n'est disponible
     */
    public Coup choisirCoup(Jeu jeu) {
        return switch (niveau) {
            case FACILE    -> coupAleatoire(jeu);
            case MOYEN     -> coupGlouton(jeu);
            case DIFFICILE -> coupMinimax(jeu);
        };
    }

    // ── Niveau Facile ─────────────────────────────────────────────────────────

    /**
     * Choisit un coup au hasard parmi tous les coups valides disponibles.
     * Stratégie : aucune — adapté aux débutants.
     *
     * @param jeu l'état courant de la partie
     * @return un coup aléatoire, ou {@code null} si aucun coup disponible
     */
    private Coup coupAleatoire(Jeu jeu) {
        List<Coup> tous = tousLesCoupsValides(jeu);
        if (tous.isEmpty()) return null;
        return tous.get(rng.nextInt(tous.size()));
    }

    // ── Niveau Moyen (Greedy) ─────────────────────────────────────────────────

    /**
     * Choisit le meilleur coup selon une heuristique gloutonne (greedy) :
     * <ol>
     *   <li>Priorité aux captures (obligatoires selon les règles)</li>
     *   <li>Sinon, préférence aux coups qui avancent vers la promotion</li>
     *   <li>Sinon, coup aléatoire parmi les restants</li>
     * </ol>
     *
     * @param jeu l'état courant de la partie
     * @return le coup sélectionné, ou {@code null} si aucun disponible
     */
    private Coup coupGlouton(Jeu jeu) {
        List<Coup> tous = tousLesCoupsValides(jeu);
        if (tous.isEmpty()) return null;

        // Étape 1 : préférer les captures
        List<Coup> captures = new ArrayList<>();
        for (Coup c : tous) if (c.estCapture()) captures.add(c);
        if (!captures.isEmpty()) return captures.get(rng.nextInt(captures.size()));

        // Étape 2 : préférer les coups qui rapprochent d'une promotion
        Piece.Couleur couleur = jeu.getJoueurActuel().getCouleur();
        List<Coup> avances = new ArrayList<>();
        for (Coup c : tous) {
            boolean avance = (couleur == Piece.Couleur.BLANC)
                ? c.getToLigne() < c.getFromLigne()  // blanc avance vers ligne 0
                : c.getToLigne() > c.getFromLigne(); // noir avance vers ligne 7
            if (avance) avances.add(c);
        }
        if (!avances.isEmpty()) return avances.get(rng.nextInt(avances.size()));

        // Étape 3 : coup aléatoire
        return tous.get(rng.nextInt(tous.size()));
    }

    // ── Niveau Difficile (Minimax + Alpha-Bêta) ───────────────────────────────

    /**
     * Choisit le meilleur coup en explorant l'arbre des coups possibles
     * avec l'algorithme Minimax et l'élagage Alpha-Bêta.
     *
     * <p>Pour chaque coup disponible, on simule la partie sur une copie
     * et on évalue le résultat à profondeur {@value #PROFONDEUR_DIFFICILE}.</p>
     *
     * @param jeu l'état courant de la partie
     * @return le coup avec le meilleur score minimax
     */
    private Coup coupMinimax(Jeu jeu) {
        List<Coup> tous = tousLesCoupsValides(jeu);
        if (tous.isEmpty()) return null;

        Coup  meilleur    = null;
        int   meilleureVal = Integer.MIN_VALUE;
        Piece.Couleur couleurBot = jeu.getJoueurActuel().getCouleur();

        for (Coup c : tous) {
            Jeu copie = new Jeu(jeu); // copie profonde : la vraie partie n'est pas modifiée
            copie.jouerCoup(c.getFromLigne(), c.getFromColonne(),
                            c.getToLigne(),   c.getToColonne());
            int val = minimax(copie, PROFONDEUR_DIFFICILE - 1,
                              Integer.MIN_VALUE, Integer.MAX_VALUE,
                              false, couleurBot);
            if (val > meilleureVal) { meilleureVal = val; meilleur = c; }
        }
        return meilleur;
    }

    /**
     * Algorithme Minimax récursif avec élagage Alpha-Bêta.
     *
     * <p>Principe :</p>
     * <ul>
     *   <li>Aux nœuds <i>maximisants</i>, le bot choisit le coup qui maximise son score.</li>
     *   <li>Aux nœuds <i>minimisants</i>, l'adversaire choisit le coup qui minimise ce score.</li>
     *   <li>L'élagage coupe les branches dont le résultat ne peut pas améliorer
     *       ce qu'on a déjà trouvé (alpha ≥ beta).</li>
     * </ul>
     *
     * @param jeu        l'état simulé de la partie
     * @param depth      profondeur restante à explorer
     * @param alpha      meilleur score que le maximiseur est sûr d'obtenir
     * @param beta       meilleur score que le minimiseur est sûr d'obtenir
     * @param maximisant {@code true} si c'est le tour du bot, {@code false} pour l'adversaire
     * @param couleurBot couleur du bot (pour orienter l'évaluation)
     * @return le score heuristique du nœud courant
     */
    private int minimax(Jeu jeu, int depth, int alpha, int beta,
                        boolean maximisant, Piece.Couleur couleurBot) {
        // Cas de base : profondeur atteinte ou partie terminée
        if (depth == 0 || jeu.isPartieTerminee())
            return evaluer(jeu, couleurBot);

        List<Coup> tous = tousLesCoupsValides(jeu);
        if (tous.isEmpty())
            return maximisant ? Integer.MIN_VALUE + 1 : Integer.MAX_VALUE - 1;

        if (maximisant) {
            int best = Integer.MIN_VALUE;
            for (Coup c : tous) {
                Jeu copie = new Jeu(jeu);
                copie.jouerCoup(c.getFromLigne(), c.getFromColonne(),
                                c.getToLigne(),   c.getToColonne());
                int val = minimax(copie, depth - 1, alpha, beta, false, couleurBot);
                best  = Math.max(best, val);
                alpha = Math.max(alpha, val);
                if (beta <= alpha) break; // élagage alpha (branche inutile)
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (Coup c : tous) {
                Jeu copie = new Jeu(jeu);
                copie.jouerCoup(c.getFromLigne(), c.getFromColonne(),
                                c.getToLigne(),   c.getToColonne());
                int val = minimax(copie, depth - 1, alpha, beta, true, couleurBot);
                best = Math.min(best, val);
                beta = Math.min(beta, val);
                if (beta <= alpha) break; // élagage bêta (branche inutile)
            }
            return best;
        }
    }

    /**
     * Fonction d'évaluation heuristique d'un état de jeu.
     *
     * <p>Le score attribué à chaque pièce prend en compte :</p>
     * <ul>
     *   <li><b>+100</b> par pion, <b>+300</b> par dame (valeur de base)</li>
     *   <li>Bonus d'avance : plus une pièce est proche de la promotion, plus elle vaut</li>
     *   <li>Bonus de centralité : les pièces au centre contrôlent plus de cases</li>
     *   <li>Bonus de bord latéral : protection contre les captures obliques</li>
     * </ul>
     * <p>Le score est positif si le bot domine, négatif si l'adversaire domine.</p>
     *
     * @param jeu        l'état à évaluer
     * @param couleurBot la couleur du bot (score positif = favorable au bot)
     * @return score heuristique de la position
     */
    private int evaluer(Jeu jeu, Piece.Couleur couleurBot) {
        // Cas terminal : victoire ou défaite
        if (jeu.isPartieTerminee()) {
            if (jeu.getVainqueur() == null) return 0;
            return jeu.getVainqueur().getCouleur() == couleurBot ? 100_000 : -100_000;
        }

        int score = 0;
        for (int i = 0; i < Plateau.TAILLE; i++)
            for (int j = 0; j < Plateau.TAILLE; j++) {
                Piece p = jeu.getPlateau().getPiece(i, j);
                if (p == null) continue;

                int val = p.estDame() ? 300 : 100; // dame vaut 3x un pion

                // Bonus d'avance : encourage la progression vers la promotion
                if (!p.estDame()) {
                    int avance = (p.getCouleur() == Piece.Couleur.BLANC) ? (7 - i) : i;
                    val += avance * 5;
                }

                // Bonus de centralité : contrôle du centre du plateau
                val += (3 - Math.abs(j - 3)) * 2 + (3 - Math.abs(i - 3)) * 2;

                // Léger bonus pour les pièces sur les colonnes latérales (protection)
                if (j == 0 || j == 7) val += 3;

                if (p.getCouleur() == couleurBot) score += val;
                else                              score -= val;
            }
        return score;
    }

    // ── Utilitaire ────────────────────────────────────────────────────────────

    /**
     * Collecte tous les coups valides du joueur actuel sur l'ensemble du plateau.
     *
     * @param jeu l'état courant de la partie
     * @return liste complète des coups jouables
     */
    private List<Coup> tousLesCoupsValides(Jeu jeu) {
        List<Coup> tous = new ArrayList<>();
        for (int[] pos : jeu.getPiecesJouables())
            tous.addAll(jeu.getCoupsValides(pos[0], pos[1]));
        return tous;
    }
}
