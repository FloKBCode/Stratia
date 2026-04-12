package model;

/**
 * Dame : pièce promue (pion ayant atteint la dernière rangée adverse).
 * Hérite de Piece — peut se déplacer dans les 4 diagonales sur N cases.
 */
public class Dame extends Piece {

    public Dame(Couleur couleur, int ligne, int colonne) {
        super(couleur, ligne, colonne);
    }

    /** Constructeur de promotion : crée une Dame à partir d'un Pion. */
    public Dame(Pion pion) {
        super(pion.getCouleur(), pion.getLigne(), pion.getColonne());
    }

    @Override
    public boolean estDame() { return true; }

    @Override
    public String toString() { return super.toString(); }
}
