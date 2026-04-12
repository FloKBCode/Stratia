package model;

/**
 * Plateau : grille 8×8 du jeu de dames.
 *
 * <p>Contient un tableau bidimensionnel de {@link Case}, chacune pouvant
 * héberger une {@link Piece}. Expose les opérations de base : déplacement,
 * suppression, promotion et comptage.</p>
 *
 * <p>Possède un <b>constructeur de copie profonde</b> utilisé par {@link Bot}
 * afin de simuler des coups futurs sans altérer le plateau réel.</p>
 */
public class Plateau {

    /** Dimension du plateau (8 lignes × 8 colonnes). */
    public static final int TAILLE = 8;

    /** Grille de cases, indexée par [ligne][colonne]. */
    private final Case[][] cases;

    // ── Constructeurs ──────────────────────────────────────────────────────────

    /**
     * Crée un plateau vierge et place les 12 pions de chaque camp
     * sur les cases jouables (cases sombres, somme des indices impaire).
     */
    public Plateau() {
        cases = new Case[TAILLE][TAILLE];
        for (int i = 0; i < TAILLE; i++)
            for (int j = 0; j < TAILLE; j++)
                cases[i][j] = new Case(i, j);
        initialiserPieces();
    }

    /**
     * Constructeur de copie profonde (deep copy).
     * Crée un nouveau Plateau avec des pièces indépendantes de la source.
     *
     * @param src le plateau à copier
     */
    public Plateau(Plateau src) {
        cases = new Case[TAILLE][TAILLE];
        for (int i = 0; i < TAILLE; i++)
            for (int j = 0; j < TAILLE; j++) {
                cases[i][j] = new Case(i, j);
                Piece p = src.cases[i][j].getPiece();
                if (p != null) {
                    // Copie la pièce en préservant son type (Pion ou Dame)
                    Piece copie = p.estDame()
                        ? new Dame(p.getCouleur(), i, j)
                        : new Pion(p.getCouleur(), i, j);
                    cases[i][j].setPiece(copie);
                }
            }
    }

    /**
     * Place les pions en début de partie :
     * Noirs sur les lignes 0-2, Blancs sur les lignes 5-7,
     * uniquement sur les cases jouables (somme ligne+colonne impaire).
     */
    private void initialiserPieces() {
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < TAILLE; j++)
                if ((i + j) % 2 == 1)
                    cases[i][j].setPiece(new Pion(Piece.Couleur.NOIR, i, j));

        for (int i = 5; i < TAILLE; i++)
            for (int j = 0; j < TAILLE; j++)
                if ((i + j) % 2 == 1)
                    cases[i][j].setPiece(new Pion(Piece.Couleur.BLANC, i, j));
    }

    // ── Accesseurs ─────────────────────────────────────────────────────────────

    /**
     * Retourne la case en (l, c), ou {@code null} si les coordonnées sont hors plateau.
     *
     * @param l ligne (0-7)
     * @param c colonne (0-7)
     * @return la Case ou {@code null}
     */
    public Case getCase(int l, int c) { return estValide(l, c) ? cases[l][c] : null; }

    /**
     * Retourne la pièce présente en (l, c), ou {@code null} si la case est vide
     * ou hors plateau.
     *
     * @param l ligne (0-7)
     * @param c colonne (0-7)
     * @return la Piece ou {@code null}
     */
    public Piece getPiece(int l, int c) { return estValide(l, c) ? cases[l][c].getPiece() : null; }

    /**
     * Vérifie si les coordonnées (l, c) sont à l'intérieur du plateau.
     *
     * @param l ligne
     * @param c colonne
     * @return {@code true} si les coordonnées sont valides (0 ≤ l,c ≤ 7)
     */
    public boolean estValide(int l, int c) {
        return l >= 0 && l < TAILLE && c >= 0 && c < TAILLE;
    }

    // ── Mutations ──────────────────────────────────────────────────────────────

    /**
     * Déplace la pièce de (fL, fC) vers (tL, tC) et libère la case de départ.
     *
     * @param fL ligne de départ
     * @param fC colonne de départ
     * @param tL ligne d'arrivée
     * @param tC colonne d'arrivée
     */
    public void deplacerPiece(int fL, int fC, int tL, int tC) {
        Piece p = cases[fL][fC].getPiece();
        if (p != null) {
            p.setPosition(tL, tC);
            cases[tL][tC].setPiece(p);
            cases[fL][fC].vider();
        }
    }

    /**
     * Retire la pièce présente en (l, c) du plateau (capture).
     *
     * @param l ligne de la pièce à supprimer
     * @param c colonne de la pièce à supprimer
     */
    public void supprimerPiece(int l, int c) {
        if (estValide(l, c)) cases[l][c].vider();
    }

    /**
     * Remplace le Pion en (l, c) par une Dame (promotion).
     * N'a d'effet que si la pièce en place est bien un {@link Pion}.
     *
     * @param l ligne de la case à promouvoir
     * @param c colonne de la case à promouvoir
     */
    public void promouvoir(int l, int c) {
        Piece p = getPiece(l, c);
        if (p instanceof Pion) cases[l][c].setPiece(new Dame((Pion) p));
    }

    // ── Utilitaires ────────────────────────────────────────────────────────────

    /**
     * Compte le nombre de pièces d'une couleur encore présentes sur le plateau.
     * Utilisé pour afficher le score et détecter une fin de partie par manque de pièces.
     *
     * @param couleur la couleur à compter
     * @return nombre de pièces de cette couleur
     */
    public int compterPieces(Piece.Couleur couleur) {
        int n = 0;
        for (int i = 0; i < TAILLE; i++)
            for (int j = 0; j < TAILLE; j++) {
                Piece p = cases[i][j].getPiece();
                if (p != null && p.getCouleur() == couleur) n++;
            }
        return n;
    }
}
