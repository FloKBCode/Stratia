package model;

/**
 * Classe abstraite représentant une pièce générique du jeu de dames.
 * Utilise l'encapsulation (attributs privés/protégés, getters/setters).
 * Sert de base à l'héritage pour Pion et Dame (polymorphisme via estDame()).
 */
public abstract class Piece {

    /** Enumération des deux couleurs possibles */
    public enum Couleur { BLANC, NOIR }

    protected Couleur couleur;
    protected int ligne;
    protected int colonne;

    public Piece(Couleur couleur, int ligne, int colonne) {
        this.couleur  = couleur;
        this.ligne    = ligne;
        this.colonne  = colonne;
    }

    /** Méthode abstraite — polymorphisme : chaque sous-classe répond différemment */
    public abstract boolean estDame();

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public Couleur getCouleur()  { return couleur; }
    public int     getLigne()    { return ligne;   }
    public int     getColonne()  { return colonne; }

    public void setPosition(int ligne, int colonne) {
        this.ligne   = ligne;
        this.colonne = colonne;
    }

    @Override
    public String toString() {
        return (couleur == Couleur.BLANC ? "B" : "N") + (estDame() ? "D" : "P");
    }
}
