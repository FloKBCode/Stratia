package model;

/**
 * Joueur : représente un joueur humain avec son nom et sa couleur.
 */
public class Joueur {

    private final String nom;
    private final Piece.Couleur couleur;

    public Joueur(String nom, Piece.Couleur couleur) {
        this.nom     = nom;
        this.couleur = couleur;
    }

    public String        getNom()    { return nom; }
    public Piece.Couleur getCouleur(){ return couleur; }

    @Override
    public String toString() {
        return nom + " (" + couleur + ")";
    }
}
