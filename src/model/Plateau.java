package model;

/**
 * Plateau : grille 8x8.
 * Inclut un constructeur de copie (deep copy) utilisé par le Bot (minimax).
 */
public class Plateau {

    public static final int TAILLE = 8;
    private final Case[][] cases;

    public Plateau() {
        cases = new Case[TAILLE][TAILLE];
        for (int i = 0; i < TAILLE; i++)
            for (int j = 0; j < TAILLE; j++)
                cases[i][j] = new Case(i, j);
        initialiserPieces();
    }

    /** Constructeur de copie profonde — utilisé par Bot pour le minimax. */
    public Plateau(Plateau src) {
        cases = new Case[TAILLE][TAILLE];
        for (int i = 0; i < TAILLE; i++)
            for (int j = 0; j < TAILLE; j++) {
                cases[i][j] = new Case(i, j);
                Piece p = src.cases[i][j].getPiece();
                if (p != null) {
                    Piece copie = p.estDame()
                        ? new Dame(p.getCouleur(), i, j)
                        : new Pion(p.getCouleur(), i, j);
                    cases[i][j].setPiece(copie);
                }
            }
    }

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

    public Case    getCase(int l, int c)  { return estValide(l,c) ? cases[l][c] : null; }
    public Piece   getPiece(int l, int c) { return estValide(l,c) ? cases[l][c].getPiece() : null; }
    public boolean estValide(int l, int c){ return l>=0 && l<TAILLE && c>=0 && c<TAILLE; }

    public void deplacerPiece(int fL, int fC, int tL, int tC) {
        Piece p = cases[fL][fC].getPiece();
        if (p != null) { p.setPosition(tL,tC); cases[tL][tC].setPiece(p); cases[fL][fC].vider(); }
    }
    public void supprimerPiece(int l, int c) { if (estValide(l,c)) cases[l][c].vider(); }
    public void promouvoir(int l, int c) {
        Piece p = getPiece(l,c);
        if (p instanceof Pion) cases[l][c].setPiece(new Dame((Pion)p));
    }

    public int compterPieces(Piece.Couleur couleur) {
        int n = 0;
        for (int i=0;i<TAILLE;i++) for (int j=0;j<TAILLE;j++) {
            Piece p = cases[i][j].getPiece();
            if (p!=null && p.getCouleur()==couleur) n++;
        }
        return n;
    }
}
