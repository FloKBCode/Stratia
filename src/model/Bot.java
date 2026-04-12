package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Bot : intelligence artificielle pour le jeu de dames.
 *
 * NIVEAUX :
 *  - FACILE    : coup aléatoire parmi les coups valides
 *  - MOYEN     : greedy (préfère captures > promotions > avance > aléatoire)
 *  - DIFFICILE : minimax avec élagage alpha-bêta (profondeur 6)
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * Pour un bot de niveau "vrai joueur" (> niveau difficile ici),
 * cherchez sur GitHub :
 *   • "draughts minimax java alpha beta"
 *   • "checkers engine java github"
 * Dépôts de référence :
 *   - https://github.com/justinwp/draughts  (alpha-beta + tables de fin)
 *   - https://github.com/jonasanders1/Checkers-Minimax
 *   - Moteur Scan (C++) portage Java : rechercher "scan draughts engine"
 * Pour une IA proche d'un vrai joueur, visez profondeur 8-10 + quiescence search.
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class Bot {

    public enum Niveau {
        FACILE("Facile 🟢"),
        MOYEN("Moyen 🟡"),
        DIFFICILE("Difficile 🔴");

        public final String label;
        Niveau(String l) { this.label = l; }
    }

    private static final int PROFONDEUR_MOYEN    = 3;
    private static final int PROFONDEUR_DIFFICILE = 6;

    private final Niveau  niveau;
    private final Random  rng = new Random();

    public Bot(Niveau niveau) { this.niveau = niveau; }
    public Niveau getNiveau() { return niveau; }

    // ── Choix du coup public ──────────────────────────────────────────────────

    /**
     * Calcule et retourne le meilleur coup pour le joueur actuel dans jeu.
     * Retourne null si aucun coup n'est disponible.
     */
    public Coup choisirCoup(Jeu jeu) {
        return switch (niveau) {
            case FACILE    -> coupAleatoire(jeu);
            case MOYEN     -> coupGlouton(jeu);
            case DIFFICILE -> coupMinimax(jeu);
        };
    }

    // ── Niveau Facile ─────────────────────────────────────────────────────────

    private Coup coupAleatoire(Jeu jeu) {
        List<Coup> tous = tousLesCoupsValides(jeu);
        if (tous.isEmpty()) return null;
        return tous.get(rng.nextInt(tous.size()));
    }

    // ── Niveau Moyen (greedy) ─────────────────────────────────────────────────

    private Coup coupGlouton(Jeu jeu) {
        List<Coup> tous = tousLesCoupsValides(jeu);
        if (tous.isEmpty()) return null;

        // 1. Préférer les captures
        List<Coup> captures = new ArrayList<>();
        for (Coup c : tous) if (c.estCapture()) captures.add(c);
        if (!captures.isEmpty()) return captures.get(rng.nextInt(captures.size()));

        // 2. Préférer les coups qui avancent vers la promotion
        Piece.Couleur couleur = jeu.getJoueurActuel().getCouleur();
        List<Coup> avances = new ArrayList<>();
        for (Coup c : tous) {
            boolean avance = (couleur == Piece.Couleur.BLANC)
                ? c.getToLigne() < c.getFromLigne()
                : c.getToLigne() > c.getFromLigne();
            if (avance) avances.add(c);
        }
        if (!avances.isEmpty()) return avances.get(rng.nextInt(avances.size()));

        return tous.get(rng.nextInt(tous.size()));
    }

    // ── Niveau Difficile (minimax + alpha-bêta) ───────────────────────────────

    private Coup coupMinimax(Jeu jeu) {
        List<Coup> tous = tousLesCoupsValides(jeu);
        if (tous.isEmpty()) return null;

        Coup meilleur = null;
        int  meilleureVal = Integer.MIN_VALUE;
        Piece.Couleur couleurBot = jeu.getJoueurActuel().getCouleur();

        for (Coup c : tous) {
            Jeu copie = new Jeu(jeu);
            copie.jouerCoup(c.getFromLigne(), c.getFromColonne(),
                            c.getToLigne(),   c.getToColonne());
            int val = minimax(copie, PROFONDEUR_DIFFICILE - 1,
                              Integer.MIN_VALUE, Integer.MAX_VALUE,
                              false, couleurBot);
            if (val > meilleureVal) { meilleureVal = val; meilleur = c; }
        }
        return meilleur;
    }

    private int minimax(Jeu jeu, int depth, int alpha, int beta,
                        boolean maximisant, Piece.Couleur couleurBot) {
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
                int val = minimax(copie, depth-1, alpha, beta, false, couleurBot);
                best  = Math.max(best, val);
                alpha = Math.max(alpha, val);
                if (beta <= alpha) break; // élagage
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (Coup c : tous) {
                Jeu copie = new Jeu(jeu);
                copie.jouerCoup(c.getFromLigne(), c.getFromColonne(),
                                c.getToLigne(),   c.getToColonne());
                int val = minimax(copie, depth-1, alpha, beta, true, couleurBot);
                best = Math.min(best, val);
                beta = Math.min(beta, val);
                if (beta <= alpha) break;
            }
            return best;
        }
    }

    // ── Fonction d'évaluation heuristique ────────────────────────────────────

    private int evaluer(Jeu jeu, Piece.Couleur couleurBot) {
        if (jeu.isPartieTerminee()) {
            if (jeu.getVainqueur() == null) return 0;
            return jeu.getVainqueur().getCouleur() == couleurBot
                ? 100_000 : -100_000;
        }
        int score = 0;
        for (int i = 0; i < Plateau.TAILLE; i++)
            for (int j = 0; j < Plateau.TAILLE; j++) {
                Piece p = jeu.getPlateau().getPiece(i, j);
                if (p == null) continue;

                int val = p.estDame() ? 300 : 100;

                // Bonus d'avance (plus proche de la promotion = mieux)
                if (!p.estDame()) {
                    int avance = (p.getCouleur() == Piece.Couleur.BLANC)
                        ? (7 - i) : i;
                    val += avance * 5;
                }

                // Bonus de centralité
                int centerJ = Math.abs(j - 3);
                int centerI = Math.abs(i - 3);
                val += (3 - centerJ) * 2 + (3 - centerI) * 2;

                // Bonus bord (sécurité latérale)
                if (j == 0 || j == 7) val += 3;

                if (p.getCouleur() == couleurBot) score += val;
                else                              score -= val;
            }
        return score;
    }

    // ── Utilitaire ────────────────────────────────────────────────────────────

    private List<Coup> tousLesCoupsValides(Jeu jeu) {
        List<Coup> tous = new ArrayList<>();
        for (int[] pos : jeu.getPiecesJouables())
            tous.addAll(jeu.getCoupsValides(pos[0], pos[1]));
        return tous;
    }
}
