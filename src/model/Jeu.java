package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Jeu : chef d'orchestre du modèle MVC.
 * Gère règles, historique (ArrayList), stats, et conditions de victoire.
 * Possède un constructeur de copie pour le minimax du Bot.
 */
public class Jeu {

    private Plateau           plateau;
    private final Joueur      joueur1;
    private final Joueur      joueur2;
    private Joueur            joueurActuel;
    private ArrayList<String> historique;
    private int               tourNumero;
    private boolean           partieTerminee;
    private Joueur            vainqueur;
    private Piece             pieceEnCours;
    private final StatsPartie stats;

    // ── Constructeur normal ────────────────────────────────────────────────────

    public Jeu(String nomJ1, String nomJ2) {
        joueur1 = new Joueur(nomJ1, Piece.Couleur.BLANC);
        joueur2 = new Joueur(nomJ2, Piece.Couleur.NOIR);
        stats   = new StatsPartie();
        historique = new ArrayList<>();
        initialiserPartie();
    }

    /** Constructeur de copie profonde pour le minimax (Bot). */
    public Jeu(Jeu src) {
        this.joueur1        = src.joueur1;
        this.joueur2        = src.joueur2;
        this.plateau        = new Plateau(src.plateau);
        this.joueurActuel   = (src.joueurActuel == src.joueur1) ? joueur1 : joueur2;
        this.historique     = new ArrayList<>(src.historique);
        this.tourNumero     = src.tourNumero;
        this.partieTerminee = src.partieTerminee;
        this.vainqueur      = src.vainqueur == null ? null
                            : (src.vainqueur == src.joueur1 ? joueur1 : joueur2);
        this.stats          = new StatsPartie();
        if (src.pieceEnCours != null)
            this.pieceEnCours = plateau.getPiece(
                src.pieceEnCours.getLigne(), src.pieceEnCours.getColonne());
    }

    private void initialiserPartie() {
        plateau        = new Plateau();
        joueurActuel   = joueur1;
        tourNumero     = 1;
        partieTerminee = false;
        vainqueur      = null;
        pieceEnCours   = null;
        historique.add("=== Début de la partie ===");
        historique.add(joueur1.getNom() + " (Blanc) vs " + joueur2.getNom() + " (Noir)");
        stats.reset();
    }

    // ── API publique ───────────────────────────────────────────────────────────

    public boolean jouerCoup(int fL, int fC, int tL, int tC) {
        if (partieTerminee) return false;
        Coup coup = trouverCoup(fL, fC, tL, tC);
        if (coup == null) return false;

        plateau.deplacerPiece(fL, fC, tL, tC);
        if (coup.estCapture()) {
            plateau.supprimerPiece(coup.getCaptureLigne(), coup.getCaptureColonne());
            stats.ajouterCapture(joueurActuel.getCouleur());
        }

        boolean promu = verifierPromotion(tL, tC);
        if (promu) coup.setPromotion(true);

        stats.ajouterCoup(joueurActuel.getCouleur());
        historique.add("T" + tourNumero + " " + joueurActuel.getNom() + " : " + coup);

        if (coup.estCapture() && !promu) {
            Piece p = plateau.getPiece(tL, tC);
            List<Coup> suite = p.estDame() ? getCapturesDame(tL, tC) : getCapturesPion(tL, tC);
            if (!suite.isEmpty()) { pieceEnCours = p; return true; }
        }

        pieceEnCours = null;
        joueurActuel = (joueurActuel == joueur1) ? joueur2 : joueur1;
        tourNumero++;
        verifierFinPartie();
        return true;
    }

    public List<Coup> getCoupsValides(int ligne, int col) {
        List<Coup> coups = new ArrayList<>();
        Piece p = plateau.getPiece(ligne, col);
        if (p == null || p.getCouleur() != joueurActuel.getCouleur()) return coups;
        if (pieceEnCours != null && pieceEnCours != p) return coups;

        if (pieceEnCours != null)
            return p.estDame() ? getCapturesDame(ligne, col) : getCapturesPion(ligne, col);

        boolean captObl = existeCapturePour(joueurActuel.getCouleur());
        if (captObl)
            return p.estDame() ? getCapturesDame(ligne, col) : getCapturesPion(ligne, col);

        List<Coup> depl = p.estDame() ? getDeplacementsDame(ligne, col) : getDeplacementsPion(ligne, col);
        List<Coup> capt = p.estDame() ? getCapturesDame(ligne, col)     : getCapturesPion(ligne, col);
        coups.addAll(depl); coups.addAll(capt);
        return coups;
    }

    public List<int[]> getPiecesJouables() {
        List<int[]> res = new ArrayList<>();
        for (int i=0;i<Plateau.TAILLE;i++)
            for (int j=0;j<Plateau.TAILLE;j++)
                if (!getCoupsValides(i,j).isEmpty()) res.add(new int[]{i,j});
        return res;
    }

    public void recommencer() { historique.clear(); initialiserPartie(); }

    // ── Logique interne ────────────────────────────────────────────────────────

    private Coup trouverCoup(int fL, int fC, int tL, int tC) {
        for (Coup c : getCoupsValides(fL, fC))
            if (c.getToLigne()==tL && c.getToColonne()==tC) return c;
        return null;
    }

    private boolean existeCapturePour(Piece.Couleur couleur) {
        for (int i=0;i<Plateau.TAILLE;i++) for (int j=0;j<Plateau.TAILLE;j++) {
            Piece p = plateau.getPiece(i,j);
            if (p!=null && p.getCouleur()==couleur) {
                List<Coup> c = p.estDame() ? getCapturesDame(i,j) : getCapturesPion(i,j);
                if (!c.isEmpty()) return true;
            }
        }
        return false;
    }

    private List<Coup> getDeplacementsPion(int l, int c) {
        List<Coup> r = new ArrayList<>();
        Piece p = plateau.getPiece(l,c); if (p==null) return r;
        int dir = p.getCouleur()==Piece.Couleur.BLANC ? -1 : 1;
        for (int dc : new int[]{-1,1}) {
            int nl=l+dir, nc=c+dc;
            if (plateau.estValide(nl,nc) && plateau.getPiece(nl,nc)==null)
                r.add(new Coup(l,c,nl,nc));
        }
        return r;
    }

    private List<Coup> getCapturesPion(int l, int c) {
        List<Coup> r = new ArrayList<>();
        Piece p = plateau.getPiece(l,c); if (p==null) return r;
        int[][] dirs = {{-1,-1},{-1,1},{1,-1},{1,1}};
        for (int[] d : dirs) {
            int mL=l+d[0], mC=c+d[1], dL=l+2*d[0], dC=c+2*d[1];
            if (!plateau.estValide(dL,dC)) continue;
            Piece mid = plateau.getPiece(mL,mC);
            if (mid!=null && mid.getCouleur()!=p.getCouleur() && plateau.getPiece(dL,dC)==null) {
                Coup cp = new Coup(l,c,dL,dC); cp.setPieceCapturee(mid,mL,mC); r.add(cp);
            }
        }
        return r;
    }

    private List<Coup> getDeplacementsDame(int l, int c) {
        List<Coup> r = new ArrayList<>();
        int[][] dirs = {{-1,-1},{-1,1},{1,-1},{1,1}};
        for (int[] d : dirs) {
            int ll=l+d[0], cc=c+d[1];
            while (plateau.estValide(ll,cc) && plateau.getPiece(ll,cc)==null) {
                r.add(new Coup(l,c,ll,cc)); ll+=d[0]; cc+=d[1];
            }
        }
        return r;
    }

    private List<Coup> getCapturesDame(int l, int c) {
        List<Coup> r = new ArrayList<>();
        Piece dame = plateau.getPiece(l,c); if (dame==null) return r;
        int[][] dirs = {{-1,-1},{-1,1},{1,-1},{1,1}};
        for (int[] d : dirs) {
            int ll=l+d[0], cc=c+d[1];
            Piece cib=null; int cL=-1,cC=-1;
            while (plateau.estValide(ll,cc)) {
                Piece cur = plateau.getPiece(ll,cc);
                if (cur!=null) {
                    if (cib==null && cur.getCouleur()!=dame.getCouleur()) { cib=cur; cL=ll; cC=cc; }
                    else break;
                } else if (cib!=null) {
                    Coup cp=new Coup(l,c,ll,cc); cp.setPieceCapturee(cib,cL,cC); r.add(cp);
                }
                ll+=d[0]; cc+=d[1];
            }
        }
        return r;
    }

    private boolean verifierPromotion(int tL, int tC) {
        Piece p = plateau.getPiece(tL,tC);
        if (p==null || p.estDame()) return false;
        boolean promo = (p.getCouleur()==Piece.Couleur.BLANC && tL==0)
                     || (p.getCouleur()==Piece.Couleur.NOIR  && tL==Plateau.TAILLE-1);
        if (promo) { plateau.promouvoir(tL,tC); stats.ajouterDame(p.getCouleur());
                     historique.add("  ♛ "+joueurActuel.getNom()+" : nouvelle Dame !"); }
        return promo;
    }

    private void verifierFinPartie() {
        boolean aPieces=false, aCoups=false;
        for (int i=0;i<Plateau.TAILLE&&!aCoups;i++)
            for (int j=0;j<Plateau.TAILLE&&!aCoups;j++) {
                Piece p=plateau.getPiece(i,j);
                if (p!=null && p.getCouleur()==joueurActuel.getCouleur()) {
                    aPieces=true;
                    if (!getCoupsValides(i,j).isEmpty()) aCoups=true;
                }
            }
        if (!aPieces || !aCoups) {
            partieTerminee=true;
            vainqueur=(joueurActuel==joueur1)?joueur2:joueur1;
            historique.add("=== "+vainqueur.getNom()+" a gagné ! ===");
        }
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public Plateau            getPlateau()       { return plateau; }
    public Joueur             getJoueurActuel()  { return joueurActuel; }
    public Joueur             getJoueur1()       { return joueur1; }
    public Joueur             getJoueur2()       { return joueur2; }
    public ArrayList<String>  getHistorique()    { return historique; }
    public boolean            isPartieTerminee() { return partieTerminee; }
    public Joueur             getVainqueur()     { return vainqueur; }
    public int                getTourNumero()    { return tourNumero; }
    public Piece              getPieceEnCours()  { return pieceEnCours; }
    public StatsPartie        getStats()         { return stats; }

    /** Force la fin de partie (ex. temps écoulé). */
    public void forcerDefaite(Joueur perdant) {
        partieTerminee = true;
        vainqueur = (perdant == joueur1) ? joueur2 : joueur1;
        historique.add("=== Temps écoulé ! "+vainqueur.getNom()+" gagne ! ===");
    }
}
