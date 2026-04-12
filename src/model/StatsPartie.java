package model;

/**
 * StatsPartie : collecte les statistiques d'une partie.
 * Mis à jour par Jeu à chaque coup joué.
 */
public class StatsPartie {

    // ── Compteurs ──────────────────────────────────────────────────────────────
    private int coupsJ1, coupsJ2;
    private int capturesJ1, capturesJ2;
    private int damesJ1, damesJ2;
    private long tempsDebut;

    public StatsPartie() { reset(); }

    // ── Mutations ──────────────────────────────────────────────────────────────

    public void reset() {
        coupsJ1 = coupsJ2 = 0;
        capturesJ1 = capturesJ2 = 0;
        damesJ1 = damesJ2 = 0;
        tempsDebut = System.currentTimeMillis();
    }

    public void ajouterCoup(Piece.Couleur c) {
        if (c == Piece.Couleur.BLANC) coupsJ1++; else coupsJ2++;
    }

    public void ajouterCapture(Piece.Couleur c) {
        if (c == Piece.Couleur.BLANC) capturesJ1++; else capturesJ2++;
    }

    public void ajouterDame(Piece.Couleur c) {
        if (c == Piece.Couleur.BLANC) damesJ1++; else damesJ2++;
    }

    // ── Accesseurs ─────────────────────────────────────────────────────────────

    public int getCoupsJ1()    { return coupsJ1; }
    public int getCoupsJ2()    { return coupsJ2; }
    public int getCapturesJ1() { return capturesJ1; }
    public int getCapturesJ2() { return capturesJ2; }
    public int getDamesJ1()    { return damesJ1; }
    public int getDamesJ2()    { return damesJ2; }
    public int getTotalCoups() { return coupsJ1 + coupsJ2; }

    /** Durée de la partie en secondes. */
    public long getDureeSecondes() {
        return (System.currentTimeMillis() - tempsDebut) / 1000;
    }

    /** Durée formatée "mm:ss". */
    public String getDureeFormatee() {
        long s = getDureeSecondes();
        return String.format("%d:%02d", s / 60, s % 60);
    }
}
