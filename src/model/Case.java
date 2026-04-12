package model;

/**
 * Case : représente une case du plateau.
 * Peut être vide ou contenir une pièce.
 */
public class Case {

    private final int ligne;
    private final int colonne;
    private Piece piece;

    public Case(int ligne, int colonne) {
        this.ligne   = ligne;
        this.colonne = colonne;
        this.piece   = null;
    }

    // ── Accesseurs ─────────────────────────────────────────────────────────────

    public boolean estOccupee() { return piece != null; }
    public Piece   getPiece()   { return piece; }
    public void    setPiece(Piece p) { this.piece = p; }
    public void    vider()      { this.piece = null; }
    public int     getLigne()   { return ligne; }
    public int     getColonne() { return colonne; }

    /**
     * Une case est jouable si elle est de couleur sombre
     * (somme des indices impaire dans notre convention).
     */
    public boolean estJouable() { return (ligne + colonne) % 2 == 1; }
}
