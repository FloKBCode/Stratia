package model;

/**
 * Pion : pièce classique.
 * Hérite de Piece — démontre l'héritage Java.
 * Se déplace uniquement vers l'avant en diagonale.
 */
public class Pion extends Piece {

    public Pion(Couleur couleur, int ligne, int colonne) {
        super(couleur, ligne, colonne);
    }

    @Override
    public boolean estDame() { return false; }

    @Override
    public String toString() { return super.toString(); }
}
