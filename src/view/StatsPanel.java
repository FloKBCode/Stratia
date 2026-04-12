package view;

import controller.JeuController;
import model.Piece;
import model.StatsPartie;

import javax.swing.*;
import java.awt.*;

/**
 * StatsPanel : panneau de statistiques affiché/masqué par un bouton.
 * Montre les coups, captures, dames et durée en temps réel.
 */
public class StatsPanel extends JPanel {

    private final JeuController controller;

    private final JLabel[] valeurs = new JLabel[10];

    public StatsPanel(JeuController controller) {
        this.controller = controller;
        setPreferredSize(new Dimension(200, 0));
        setBackground(new Color(20, 20, 35));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 2, 0, 0, new Color(80, 80, 130)),
            BorderFactory.createEmptyBorder(12, 10, 12, 10)
        ));
        setLayout(new GridLayout(0, 1, 2, 4));

        add(titre("📊 Statistiques"));
        add(separateur());

        String j1 = controller.getJeu().getJoueur1().getNom();
        String j2 = controller.getJeu().getJoueur2().getNom();

        add(sousTitre(j1 + " (Blanc)"));
        add(ligne("Coups joués",   0));
        add(ligne("Captures",      1));
        add(ligne("Dames créées",  2));
        add(separateur());
        add(sousTitre(j2 + " (Noir)"));
        add(ligne("Coups joués",   3));
        add(ligne("Captures",      4));
        add(ligne("Dames créées",  5));
        add(separateur());
        add(sousTitre("Partie"));
        add(ligne("Total coups",   6));
        add(ligne("Durée",         7));
        add(ligne("Chrono Blanc",  8));
        add(ligne("Chrono Noir",   9));
    }

    // ── Construction des lignes ───────────────────────────────────────────────

    private JLabel titre(String t) {
        JLabel l = new JLabel(t, SwingConstants.CENTER);
        l.setFont(new Font("Arial", Font.BOLD, 13));
        l.setForeground(new Color(180, 180, 255));
        return l;
    }

    private JLabel sousTitre(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Arial", Font.BOLD, 11));
        l.setForeground(new Color(140, 180, 255));
        return l;
    }

    private JSeparator separateur() {
        JSeparator s = new JSeparator();
        s.setForeground(new Color(60, 60, 100));
        return s;
    }

    private JPanel ligne(String label, int idx) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(20, 20, 35));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.PLAIN, 11));
        lbl.setForeground(new Color(160, 170, 190));
        valeurs[idx] = new JLabel("0", SwingConstants.RIGHT);
        valeurs[idx].setFont(new Font("Arial", Font.BOLD, 11));
        valeurs[idx].setForeground(new Color(220, 230, 255));
        p.add(lbl, BorderLayout.WEST);
        p.add(valeurs[idx], BorderLayout.EAST);
        return p;
    }

    // ── Mise à jour ───────────────────────────────────────────────────────────

    public void actualiser() {
        StatsPartie st = controller.getJeu().getStats();
        valeurs[0].setText(String.valueOf(st.getCoupsJ1()));
        valeurs[1].setText(String.valueOf(st.getCapturesJ1()));
        valeurs[2].setText(String.valueOf(st.getDamesJ1()));
        valeurs[3].setText(String.valueOf(st.getCoupsJ2()));
        valeurs[4].setText(String.valueOf(st.getCapturesJ2()));
        valeurs[5].setText(String.valueOf(st.getDamesJ2()));
        valeurs[6].setText(String.valueOf(st.getTotalCoups()));
        valeurs[7].setText(st.getDureeFormatee());
        valeurs[8].setText(controller.getChrono().formater(Piece.Couleur.BLANC));
        valeurs[9].setText(controller.getChrono().formater(Piece.Couleur.NOIR));

        // Alerte rouge chrono
        boolean critB = controller.getChrono().estCritique(Piece.Couleur.BLANC);
        boolean critN = controller.getChrono().estCritique(Piece.Couleur.NOIR);
        valeurs[8].setForeground(critB ? new Color(255, 80, 80) : new Color(220, 230, 255));
        valeurs[9].setForeground(critN ? new Color(255, 80, 80) : new Color(220, 230, 255));
    }
}
