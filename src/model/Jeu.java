package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Jeu : classe principale du modèle (patron MVC — couche Model).
 *
 * <p>Responsabilités :</p>
 * <ul>
 *   <li>Initialiser et réinitialiser une partie</li>
 *   <li>Valider et exécuter les coups (règles des dames françaises)</li>
 *   <li>Gérer la capture obligatoire et les captures multiples</li>
 *   <li>Promouvoir automatiquement un Pion en Dame</li>
 *   <li>Stocker l'historique dans un {@link ArrayList} de String</li>
 *   <li>Détecter les conditions de fin de partie</li>
 * </ul>
 *
 * <p>Possède un <b>constructeur de copie profonde</b> utilisé par {@link Bot}
 * pour simuler des coups futurs sans modifier l'état réel du jeu.</p>
 */
public class Jeu {

    /** Le plateau 8×8 contenant toutes les cases et pièces. */
    private Plateau           plateau;

    /** Joueur 1 — joue les pièces blanches, commence toujours. */
    private final Joueur      joueur1;

    /** Joueur 2 — joue les pièces noires. */
    private final Joueur      joueur2;

    /** Référence au joueur dont c'est actuellement le tour. */
    private Joueur            joueurActuel;

    /**
     * Historique des coups joués, stocké dans un ArrayList (collection Java).
     * Chaque élément est une chaîne lisible affichée dans l'interface.
     */
    private ArrayList<String> historique;

    /** Numéro du tour courant (commence à 1). */
    private int               tourNumero;

    /** Indique si la partie est terminée. */
    private boolean           partieTerminee;

    /** Joueur qui a remporté la partie (null si la partie est en cours). */
    private Joueur            vainqueur;

    /**
     * Référence à la pièce en cours de capture multiple.
     * Non null uniquement lorsqu'une pièce doit enchaîner des prises.
     */
    private Piece             pieceEnCours;

    /** Statistiques de la partie (coups, captures, dames). */
    private final StatsPartie stats;

    // ── Constructeurs ──────────────────────────────────────────────────────────

    /**
     * Crée une nouvelle partie avec deux joueurs humains.
     *
     * @param nomJ1 nom du joueur 1 (pièces blanches)
     * @param nomJ2 nom du joueur 2 (pièces noires ou nom du bot)
     */
    public Jeu(String nomJ1, String nomJ2) {
        joueur1    = new Joueur(nomJ1, Piece.Couleur.BLANC);
        joueur2    = new Joueur(nomJ2, Piece.Couleur.NOIR);
        stats      = new StatsPartie();
        historique = new ArrayList<>();
        initialiserPartie();
    }

    /**
     * Constructeur de copie profonde (deep copy) utilisé par le {@link Bot}.
     *
     * <p>Crée un état identique à {@code src} mais entièrement indépendant,
     * afin que le minimax puisse simuler des coups sans modifier la vraie partie.</p>
     *
     * @param src l'instance de Jeu à copier
     */
    public Jeu(Jeu src) {
        this.joueur1        = src.joueur1;
        this.joueur2        = src.joueur2;
        this.plateau        = new Plateau(src.plateau); // copie profonde du plateau
        this.joueurActuel   = (src.joueurActuel == src.joueur1) ? joueur1 : joueur2;
        this.historique     = new ArrayList<>(src.historique);
        this.tourNumero     = src.tourNumero;
        this.partieTerminee = src.partieTerminee;
        this.vainqueur      = src.vainqueur == null ? null
                            : (src.vainqueur == src.joueur1 ? joueur1 : joueur2);
        this.stats          = new StatsPartie();
        if (src.pieceEnCours != null)
            this.pieceEnCours = plateau.getPiece(
                src.pieceEnCours.getLigne(), src.pieceEnCours.getColonne());
    }

    /**
     * Initialise (ou réinitialise) la partie :
     * crée un plateau vierge, replace les pièces et remet les compteurs à zéro.
     */
    private void initialiserPartie() {
        plateau        = new Plateau();
        joueurActuel   = joueur1; // le blanc commence
        tourNumero     = 1;
        partieTerminee = false;
        vainqueur      = null;
        pieceEnCours   = null;
        historique.add("=== Début de la partie ===");
        historique.add(joueur1.getNom() + " (Blanc) vs " + joueur2.getNom() + " (Noir)");
        stats.reset();
    }

    // ── API publique ───────────────────────────────────────────────────────────

    /**
     * Tente d'exécuter le déplacement de la case (fL, fC) vers (tL, tC).
     *
     * <p>Étapes exécutées si le coup est valide :</p>
     * <ol>
     *   <li>Déplacement de la pièce sur le plateau</li>
     *   <li>Suppression de la pièce capturée le cas échéant</li>
     *   <li>Vérification de la promotion en Dame</li>
     *   <li>Ajout du coup dans l'historique</li>
     *   <li>Détection d'une capture multiple possible</li>
     *   <li>Alternance du tour et vérification de fin de partie</li>
     * </ol>
     *
     * @param fL ligne de départ
     * @param fC colonne de départ
     * @param tL ligne d'arrivée
     * @param tC colonne d'arrivée
     * @return {@code true} si le coup a été joué, {@code false} s'il est invalide
     */
    public boolean jouerCoup(int fL, int fC, int tL, int tC) {
        if (partieTerminee) return false;
        Coup coup = trouverCoup(fL, fC, tL, tC);
        if (coup == null) return false;

        plateau.deplacerPiece(fL, fC, tL, tC);
        if (coup.estCapture()) {
            plateau.supprimerPiece(coup.getCaptureLigne(), coup.getCaptureColonne());
            stats.ajouterCapture(joueurActuel.getCouleur());
        }

        boolean promu = verifierPromotion(tL, tC);
        if (promu) coup.setPromotion(true);

        stats.ajouterCoup(joueurActuel.getCouleur());
        historique.add("T" + tourNumero + " " + joueurActuel.getNom() + " : " + coup);

        // Vérification d'une capture multiple : si la pièce peut encore capturer, on garde le tour
        if (coup.estCapture() && !promu) {
            Piece p = plateau.getPiece(tL, tC);
            List<Coup> suite = p.estDame() ? getCapturesDame(tL, tC) : getCapturesPion(tL, tC);
            if (!suite.isEmpty()) { pieceEnCours = p; return true; }
        }

        // Fin du tour : on passe au joueur suivant
        pieceEnCours = null;
        joueurActuel = (joueurActuel == joueur1) ? joueur2 : joueur1;
        tourNumero++;
        verifierFinPartie();
        return true;
    }

    /**
     * Retourne la liste des coups valides pour la pièce en (ligne, col).
     *
     * <p>Applique les règles suivantes :</p>
     * <ul>
     *   <li>Seul le joueur actuel peut déplacer ses pièces</li>
     *   <li>Si une capture multiple est en cours, seule la pièce active peut jouer</li>
     *   <li>Si une capture est possible pour le joueur, elle est <b>obligatoire</b></li>
     * </ul>
     *
     * @param ligne ligne de la pièce (0-7)
     * @param col   colonne de la pièce (0-7)
     * @return liste des coups jouables, vide si aucun coup disponible
     */
    public List<Coup> getCoupsValides(int ligne, int col) {
        List<Coup> coups = new ArrayList<>();
        Piece p = plateau.getPiece(ligne, col);
        if (p == null || p.getCouleur() != joueurActuel.getCouleur()) return coups;
        if (pieceEnCours != null && pieceEnCours != p) return coups;

        if (pieceEnCours != null)
            return p.estDame() ? getCapturesDame(ligne, col) : getCapturesPion(ligne, col);

        boolean captObl = existeCapturePour(joueurActuel.getCouleur());
        if (captObl)
            return p.estDame() ? getCapturesDame(ligne, col) : getCapturesPion(ligne, col);

        List<Coup> depl = p.estDame() ? getDeplacementsDame(ligne, col) : getDeplacementsPion(ligne, col);
        List<Coup> capt = p.estDame() ? getCapturesDame(ligne, col)     : getCapturesPion(ligne, col);
        coups.addAll(depl); coups.addAll(capt);
        return coups;
    }

    /**
     * Retourne les positions [ligne, col] de toutes les pièces du joueur actuel
     * qui possèdent au moins un coup valide. Utilisé par la vue pour le surlignage.
     *
     * @return liste de tableaux {ligne, colonne}
     */
    public List<int[]> getPiecesJouables() {
        List<int[]> res = new ArrayList<>();
        for (int i = 0; i < Plateau.TAILLE; i++)
            for (int j = 0; j < Plateau.TAILLE; j++)
                if (!getCoupsValides(i, j).isEmpty()) res.add(new int[]{i, j});
        return res;
    }

    /**
     * Remet la partie à zéro : nouveau plateau, historique effacé, tour 1.
     */
    public void recommencer() { historique.clear(); initialiserPartie(); }

    /**
     * Force la fin de partie suite à un dépassement de temps.
     * Le joueur {@code perdant} perd immédiatement.
     *
     * @param perdant le joueur dont le temps est écoulé
     */
    public void forcerDefaite(Joueur perdant) {
        partieTerminee = true;
        vainqueur = (perdant == joueur1) ? joueur2 : joueur1;
        historique.add("=== Temps écoulé ! " + vainqueur.getNom() + " gagne ! ===");
    }

    // ── Logique interne ────────────────────────────────────────────────────────

    /**
     * Recherche dans les coups valides le coup correspondant à la destination (tL, tC).
     *
     * @return le Coup trouvé, ou {@code null} si le déplacement est invalide
     */
    private Coup trouverCoup(int fL, int fC, int tL, int tC) {
        for (Coup c : getCoupsValides(fL, fC))
            if (c.getToLigne() == tL && c.getToColonne() == tC) return c;
        return null;
    }

    /**
     * Vérifie si le joueur de la couleur donnée possède au moins une capture disponible.
     * Utilisé pour appliquer la règle de capture obligatoire.
     *
     * @param couleur la couleur du joueur à vérifier
     * @return {@code true} si au moins une capture est possible
     */
    private boolean existeCapturePour(Piece.Couleur couleur) {
        for (int i = 0; i < Plateau.TAILLE; i++)
            for (int j = 0; j < Plateau.TAILLE; j++) {
                Piece p = plateau.getPiece(i, j);
                if (p != null && p.getCouleur() == couleur) {
                    List<Coup> c = p.estDame() ? getCapturesDame(i, j) : getCapturesPion(i, j);
                    if (!c.isEmpty()) return true;
                }
            }
        return false;
    }

    /**
     * Calcule les déplacements simples d'un Pion (diagonale avant uniquement, 1 case).
     * Les Blancs avancent vers les lignes décroissantes, les Noirs vers les lignes croissantes.
     *
     * @param l ligne du pion
     * @param c colonne du pion
     * @return liste des coups de déplacement possibles
     */
    private List<Coup> getDeplacementsPion(int l, int c) {
        List<Coup> r = new ArrayList<>();
        Piece p = plateau.getPiece(l, c);
        if (p == null) return r;
        int dir = p.getCouleur() == Piece.Couleur.BLANC ? -1 : 1; // blanc monte, noir descend
        for (int dc : new int[]{-1, 1}) {
            int nl = l + dir, nc = c + dc;
            if (plateau.estValide(nl, nc) && plateau.getPiece(nl, nc) == null)
                r.add(new Coup(l, c, nl, nc));
        }
        return r;
    }

    /**
     * Calcule les captures possibles d'un Pion (saut par-dessus une pièce adverse,
     * dans les 4 directions diagonales).
     *
     * @param l ligne du pion
     * @param c colonne du pion
     * @return liste des coups de capture possibles
     */
    private List<Coup> getCapturesPion(int l, int c) {
        List<Coup> r = new ArrayList<>();
        Piece p = plateau.getPiece(l, c);
        if (p == null) return r;
        int[][] dirs = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        for (int[] d : dirs) {
            int mL = l + d[0], mC = c + d[1]; // case de la pièce capturée
            int dL = l + 2 * d[0], dC = c + 2 * d[1]; // case d'atterrissage
            if (!plateau.estValide(dL, dC)) continue;
            Piece mid = plateau.getPiece(mL, mC);
            // Capture valide : pièce adverse au milieu, case d'arrivée libre
            if (mid != null && mid.getCouleur() != p.getCouleur()
                    && plateau.getPiece(dL, dC) == null) {
                Coup cp = new Coup(l, c, dL, dC);
                cp.setPieceCapturee(mid, mL, mC);
                r.add(cp);
            }
        }
        return r;
    }

    /**
     * Calcule les déplacements d'une Dame (N cases dans les 4 diagonales,
     * jusqu'à une pièce ou le bord du plateau).
     *
     * @param l ligne de la dame
     * @param c colonne de la dame
     * @return liste des coups de déplacement possibles
     */
    private List<Coup> getDeplacementsDame(int l, int c) {
        List<Coup> r = new ArrayList<>();
        int[][] dirs = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        for (int[] d : dirs) {
            int ll = l + d[0], cc = c + d[1];
            // Glisser dans la diagonale tant que les cases sont libres
            while (plateau.estValide(ll, cc) && plateau.getPiece(ll, cc) == null) {
                r.add(new Coup(l, c, ll, cc));
                ll += d[0]; cc += d[1];
            }
        }
        return r;
    }

    /**
     * Calcule les captures d'une Dame : elle peut sauter par-dessus une pièce adverse
     * et atterrir sur n'importe quelle case libre après elle dans la même diagonale.
     *
     * @param l ligne de la dame
     * @param c colonne de la dame
     * @return liste des coups de capture possibles
     */
    private List<Coup> getCapturesDame(int l, int c) {
        List<Coup> r = new ArrayList<>();
        Piece dame = plateau.getPiece(l, c);
        if (dame == null) return r;
        int[][] dirs = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        for (int[] d : dirs) {
            int ll = l + d[0], cc = c + d[1];
            Piece cible = null; int cL = -1, cC = -1;
            while (plateau.estValide(ll, cc)) {
                Piece cur = plateau.getPiece(ll, cc);
                if (cur != null) {
                    // Première pièce rencontrée : doit être adverse pour capturer
                    if (cible == null && cur.getCouleur() != dame.getCouleur()) {
                        cible = cur; cL = ll; cC = cc;
                    } else break; // deux pièces consécutives : stop
                } else if (cible != null) {
                    // Case libre après la pièce capturée : destination valide
                    Coup cp = new Coup(l, c, ll, cc);
                    cp.setPieceCapturee(cible, cL, cC);
                    r.add(cp);
                }
                ll += d[0]; cc += d[1];
            }
        }
        return r;
    }

    /**
     * Vérifie si la pièce arrivée en (tL, tC) doit être promue en Dame.
     * Un Pion blanc est promu en ligne 0, un Pion noir en ligne 7.
     *
     * @param tL ligne d'arrivée
     * @param tC colonne d'arrivée
     * @return {@code true} si une promotion a été effectuée
     */
    private boolean verifierPromotion(int tL, int tC) {
        Piece p = plateau.getPiece(tL, tC);
        if (p == null || p.estDame()) return false;
        boolean promo = (p.getCouleur() == Piece.Couleur.BLANC && tL == 0)
                     || (p.getCouleur() == Piece.Couleur.NOIR  && tL == Plateau.TAILLE - 1);
        if (promo) {
            plateau.promouvoir(tL, tC);
            stats.ajouterDame(p.getCouleur());
            historique.add("  ♛ " + joueurActuel.getNom() + " : nouvelle Dame !");
        }
        return promo;
    }

    /**
     * Vérifie si la partie est terminée après chaque coup.
     * Le joueur actuel perd s'il n'a plus de pièces ou plus de coups valides.
     */
    private void verifierFinPartie() {
        boolean aPieces = false, aCoups = false;
        for (int i = 0; i < Plateau.TAILLE && !aCoups; i++)
            for (int j = 0; j < Plateau.TAILLE && !aCoups; j++) {
                Piece p = plateau.getPiece(i, j);
                if (p != null && p.getCouleur() == joueurActuel.getCouleur()) {
                    aPieces = true;
                    if (!getCoupsValides(i, j).isEmpty()) aCoups = true;
                }
            }
        if (!aPieces || !aCoups) {
            partieTerminee = true;
            vainqueur = (joueurActuel == joueur1) ? joueur2 : joueur1;
            historique.add("=== " + vainqueur.getNom() + " a gagné ! ===");
        }
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    /** @return le plateau de jeu courant */
    public Plateau            getPlateau()       { return plateau; }

    /** @return le joueur dont c'est actuellement le tour */
    public Joueur             getJoueurActuel()  { return joueurActuel; }

    /** @return joueur 1 (pièces blanches) */
    public Joueur             getJoueur1()       { return joueur1; }

    /** @return joueur 2 (pièces noires ou bot) */
    public Joueur             getJoueur2()       { return joueur2; }

    /** @return l'ArrayList contenant tous les coups joués sous forme lisible */
    public ArrayList<String>  getHistorique()    { return historique; }

    /** @return {@code true} si la partie est terminée */
    public boolean            isPartieTerminee() { return partieTerminee; }

    /** @return le vainqueur, ou {@code null} si la partie est en cours */
    public Joueur             getVainqueur()     { return vainqueur; }

    /** @return le numéro du tour actuel */
    public int                getTourNumero()    { return tourNumero; }

    /**
     * @return la pièce en cours de capture multiple, ou {@code null} si aucune
     */
    public Piece              getPieceEnCours()  { return pieceEnCours; }

    /** @return les statistiques de la partie en cours */
    public StatsPartie        getStats()         { return stats; }
}
