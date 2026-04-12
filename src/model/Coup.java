package model;

/**
 * Coup : représente un déplacement (avec ou sans capture).
 * Stocké dans l'ArrayList d'historique de la classe Jeu.
 */
public class Coup {

    private final int fromLigne, fromColonne;
    private final int toLigne, toColonne;

    // Informations sur la capture éventuelle
    private Piece pieceCapturee;
    private int   captureLigne;
    private int   captureColonne;

    private boolean promotion;

    public Coup(int fromL, int fromC, int toL, int toC) {
        this.fromLigne   = fromL;
        this.fromColonne = fromC;
        this.toLigne     = toL;
        this.toColonne   = toC;
        this.promotion   = false;
    }

    // ── Accesseurs ─────────────────────────────────────────────────────────────

    public int   getFromLigne()      { return fromLigne; }
    public int   getFromColonne()    { return fromColonne; }
    public int   getToLigne()        { return toLigne; }
    public int   getToColonne()      { return toColonne; }
    public Piece getPieceCapturee()  { return pieceCapturee; }
    public int   getCaptureLigne()   { return captureLigne; }
    public int   getCaptureColonne() { return captureColonne; }
    public boolean estCapture()      { return pieceCapturee != null; }
    public boolean estPromotion()    { return promotion; }
    public void  setPromotion(boolean b) { promotion = b; }

    public void setPieceCapturee(Piece p, int l, int c) {
        this.pieceCapturee  = p;
        this.captureLigne   = l;
        this.captureColonne = c;
    }

    @Override
    public String toString() {
        String s = "(" + fromLigne + "," + fromColonne + ") → ("
                 + toLigne + "," + toColonne + ")";
        if (estCapture())  s += " [x]";
        if (promotion)     s += " [♛]";
        return s;
    }
}
