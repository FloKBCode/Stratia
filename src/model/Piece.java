package model;

/**
 * Piece : classe abstraite représentant une pièce du jeu de dames.
 *
 * <p><b>Encapsulation</b> : les attributs {@code couleur}, {@code ligne} et
 * {@code colonne} sont {@code protected} — accessibles dans le package model
 * mais protégés de l'extérieur via des getters/setters.</p>
 *
 * <p><b>Héritage</b> : {@link Pion} et {@link Dame} étendent cette classe
 * et héritent de ses attributs et méthodes.</p>
 *
 * <p><b>Polymorphisme</b> : la méthode abstraite {@link #estDame()} est
 * redéfinie dans chaque sous-classe, permettant de distinguer leur comportement
 * sans connaître leur type exact à la compilation.</p>
 *
 * <p><b>Abstraction</b> : on ne peut pas instancier {@code Piece} directement
 * ({@code new Piece(...)}) — seules les sous-classes concrètes sont instanciables.</p>
 */
public abstract class Piece {

    /**
     * Enumération des deux couleurs possibles dans le jeu.
     * BLANC joue en premier, NOIR joue en second.
     */
    public enum Couleur { BLANC, NOIR }

    /** Couleur de la pièce (BLANC ou NOIR). Accessible aux sous-classes. */
    protected Couleur couleur;

    /** Ligne actuelle de la pièce sur le plateau (0 = haut, 7 = bas). */
    protected int ligne;

    /** Colonne actuelle de la pièce sur le plateau (0 = gauche, 7 = droite). */
    protected int colonne;

    /**
     * Constructeur commun à toutes les pièces.
     *
     * @param couleur  couleur de la pièce (BLANC ou NOIR)
     * @param ligne    ligne initiale sur le plateau (0-7)
     * @param colonne  colonne initiale sur le plateau (0-7)
     */
    public Piece(Couleur couleur, int ligne, int colonne) {
        this.couleur  = couleur;
        this.ligne    = ligne;
        this.colonne  = colonne;
    }

    /**
     * Méthode abstraite imposant le contrat d'abstraction.
     *
     * <p>Implémentée différemment selon la sous-classe (polymorphisme) :</p>
     * <ul>
     *   <li>{@link Pion#estDame()} retourne {@code false}</li>
     *   <li>{@link Dame#estDame()} retourne {@code true}</li>
     * </ul>
     *
     * @return {@code true} si la pièce est une Dame, {@code false} si c'est un Pion
     */
    public abstract boolean estDame();

    // ── Getters ────────────────────────────────────────────────────────────────

    /** @return la couleur de la pièce */
    public Couleur getCouleur() { return couleur; }

    /** @return la ligne actuelle de la pièce (0-7) */
    public int getLigne()       { return ligne; }

    /** @return la colonne actuelle de la pièce (0-7) */
    public int getColonne()     { return colonne; }

    /**
     * Met à jour la position de la pièce après un déplacement.
     *
     * @param ligne   nouvelle ligne (0-7)
     * @param colonne nouvelle colonne (0-7)
     */
    public void setPosition(int ligne, int colonne) {
        this.ligne   = ligne;
        this.colonne = colonne;
    }

    /**
     * Représentation textuelle compacte : "BP" (Blanc Pion), "ND" (Noir Dame), etc.
     *
     * @return chaîne de 2 caractères décrivant la pièce
     */
    @Override
    public String toString() {
        return (couleur == Couleur.BLANC ? "B" : "N") + (estDame() ? "D" : "P");
    }
}
