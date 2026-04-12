package model;

/**
 * Chrono : modèle de compte à rebours par joueur.
 * La classe ne contient PAS de Thread — c'est le contrôleur
 * qui appelle tick() à intervalles réguliers (Swing Timer).
 */
public class Chrono {

    private int secJ1;   // secondes restantes joueur 1
    private int secJ2;   // secondes restantes joueur 2
    private Piece.Couleur actif;
    private boolean actif_flag = false;

    public Chrono(int secondesParJoueur) {
        this.secJ1  = secondesParJoueur;
        this.secJ2  = secondesParJoueur;
        this.actif  = Piece.Couleur.BLANC;
    }

    // ── Contrôle ───────────────────────────────────────────────────────────────

    public void demarrer()         { actif_flag = true; }
    public void arreter()          { actif_flag = false; }
    public boolean estActif()      { return actif_flag; }

    /** Décrémente d'une seconde le joueur dont c'est le tour. */
    public void tick() {
        if (!actif_flag) return;
        if (actif == Piece.Couleur.BLANC) { if (secJ1 > 0) secJ1--; }
        else                              { if (secJ2 > 0) secJ2--; }
    }

    public void changerJoueur(Piece.Couleur c) { this.actif = c; }

    public void reset(int secondesParJoueur) {
        secJ1 = secJ2 = secondesParJoueur;
        actif = Piece.Couleur.BLANC;
        actif_flag = false;
    }

    // ── Accesseurs ─────────────────────────────────────────────────────────────

    public int getSecondes(Piece.Couleur c) {
        return c == Piece.Couleur.BLANC ? secJ1 : secJ2;
    }

    public boolean estEpuise(Piece.Couleur c) {
        return getSecondes(c) <= 0;
    }

    /** Retourne "m:ss" pour le joueur donné. */
    public String formater(Piece.Couleur c) {
        int s = Math.max(0, getSecondes(c));
        return String.format("%d:%02d", s / 60, s % 60);
    }

    /** true si le temps restant est inférieur à 20 s (alerte rouge). */
    public boolean estCritique(Piece.Couleur c) {
        return getSecondes(c) <= 20;
    }
}
