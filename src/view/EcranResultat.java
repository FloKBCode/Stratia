package view;

import util.IconLoader;

import model.*;
import sound.SoundManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * EcranResultat : fenetre de fin de partie (victoire ou defaite vs bot).
 * Affichée à la place du JOptionPane basique.
 */
public class EcranResultat extends JDialog {

    public enum TypeResultat { VICTOIRE, DEFAITE, EGALITE }

    private static final Color BG_WIN  = new Color(10, 25, 15);
    private static final Color BG_LOSE = new Color(25, 10, 12);
    private static final Color BG_DRAW = new Color(15, 18, 30);
    private static final Color GOLD    = new Color(215, 170, 50);
    private static final Color WHITE   = Color.WHITE;
    private static final Color GREY    = new Color(160, 170, 195);

    private Runnable onRejouer;
    private Runnable onQuitter;

    public EcranResultat(JFrame parent, TypeResultat type, String nomVainqueur,
                         StatsPartie stats, Runnable onRejouer, Runnable onQuitter) {
        super(parent, "Fin de partie", true);
        this.onRejouer = onRejouer;
        this.onQuitter = onQuitter;

        setUndecorated(true);
        IconLoader.appliquer((java.awt.Window) this);
        setSize(400, 460);
        setLocationRelativeTo(parent);
        setResizable(false);

        Color bgColor = type == TypeResultat.VICTOIRE ? BG_WIN
                      : type == TypeResultat.DEFAITE  ? BG_LOSE : BG_DRAW;

        JPanel main = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                // Dégradé de fond
                GradientPaint gp = new GradientPaint(
                    0, 0, bgColor,
                    0, getHeight(), bgColor.darker().darker()
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        main.setOpaque(false);
        main.setBorder(BorderFactory.createLineBorder(
            type == TypeResultat.VICTOIRE ? new Color(80, 200, 80)
          : type == TypeResultat.DEFAITE  ? new Color(200, 60, 60)
          : new Color(80, 100, 200), 2
        ));

        main.add(creerEntete(type, nomVainqueur), BorderLayout.NORTH);
        main.add(creerStats(stats, parent), BorderLayout.CENTER);
        main.add(creerBoutons(type), BorderLayout.SOUTH);

        setContentPane(main);

        // Sons
        if (type == TypeResultat.VICTOIRE)    SoundManager.playVictoire();
        else if (type == TypeResultat.DEFAITE) SoundManager.playDefaite();
        else                                   SoundManager.playFinPartie();
    }

    // ── En-tête ───────────────────────────────────────────────────────────────

    private JPanel creerEntete(TypeResultat type, String nom) {
        JPanel p = new JPanel(new GridLayout(3, 1, 0, 6));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(30, 20, 15, 20));

        // Grande icone symbolique (pas emoji - juste texte unicode stable)
        String symbole = type == TypeResultat.VICTOIRE ? "\u265B"  // dame noire
                       : type == TypeResultat.DEFAITE  ? "\u2639"  // visage triste (unicode basique)
                       : "=";
        JLabel ico = new JLabel(symbole, SwingConstants.CENTER);
        ico.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 52));
        ico.setForeground(type == TypeResultat.VICTOIRE ? GOLD
                        : type == TypeResultat.DEFAITE  ? new Color(230, 80, 80)
                        : new Color(150, 160, 220));

        String titre = type == TypeResultat.VICTOIRE ? "VICTOIRE !"
                     : type == TypeResultat.DEFAITE  ? "DEFAITE"
                     : "MATCH NUL";
        JLabel lblTitre = new JLabel(titre, SwingConstants.CENTER);
        lblTitre.setFont(new Font("Arial", Font.BOLD, 28));
        lblTitre.setForeground(type == TypeResultat.VICTOIRE ? GOLD
                              : type == TypeResultat.DEFAITE  ? new Color(230, 80, 80)
                              : new Color(150, 160, 220));

        JLabel lblNom = new JLabel(nom + " remporte la partie", SwingConstants.CENTER);
        lblNom.setFont(new Font("Arial", Font.ITALIC, 14));
        lblNom.setForeground(GREY);

        p.add(ico); p.add(lblTitre); p.add(lblNom);
        return p;
    }

    // ── Stats ─────────────────────────────────────────────────────────────────

    private JPanel creerStats(StatsPartie stats, JFrame parent) {
        String j1 = ((FenetreJeu) parent) != null ? "" : "";

        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        outer.setBorder(BorderFactory.createEmptyBorder(5, 25, 5, 25));

        JPanel card = new JPanel(new GridLayout(0, 2, 8, 6));
        card.setBackground(new Color(255, 255, 255, 18));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 90, 130), 1),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        String[][] lignes = {
            {"Coups Blanc",    String.valueOf(stats.getCoupsJ1())},
            {"Coups Noir",     String.valueOf(stats.getCoupsJ2())},
            {"Captures Blanc", String.valueOf(stats.getCapturesJ1())},
            {"Captures Noir",  String.valueOf(stats.getCapturesJ2())},
            {"Dames Blanc",    String.valueOf(stats.getDamesJ1())},
            {"Dames Noir",     String.valueOf(stats.getDamesJ2())},
            {"Total coups",    String.valueOf(stats.getTotalCoups())},
            {"Duree",          stats.getDureeFormatee()},
        };

        for (String[] l : lignes) {
            JLabel lbl = new JLabel(l[0]);
            lbl.setFont(new Font("Arial", Font.PLAIN, 12));
            lbl.setForeground(GREY);

            JLabel val = new JLabel(l[1], SwingConstants.RIGHT);
            val.setFont(new Font("Arial", Font.BOLD, 12));
            val.setForeground(WHITE);

            card.add(lbl); card.add(val);
        }

        outer.add(card, BorderLayout.CENTER);
        return outer;
    }

    // ── Boutons ───────────────────────────────────────────────────────────────

    private JPanel creerBoutons(TypeResultat type) {
        JPanel p = new JPanel(new GridLayout(1, 2, 12, 0));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(15, 25, 25, 25));

        JButton btnRejouer = btnStyle("Rejouer",  new Color(40, 110, 60));
        JButton btnQuitter = btnStyle("Quitter",  new Color(120, 40, 40));

        btnRejouer.addActionListener(e -> { dispose(); if (onRejouer != null) onRejouer.run(); });
        btnQuitter.addActionListener(e -> { dispose(); if (onQuitter != null) onQuitter.run(); });

        p.add(btnRejouer); p.add(btnQuitter);
        return p;
    }

    private JButton btnStyle(String txt, Color bg) {
        JButton b = new JButton(txt);
        b.setBackground(bg); b.setForeground(WHITE);
        b.setFont(new Font("Arial", Font.BOLD, 14));
        b.setFocusPainted(false); b.setBorderPainted(false); b.setOpaque(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(0, 42));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(bg.brighter()); }
            public void mouseExited(MouseEvent e)  { b.setBackground(bg); }
        });
        return b;
    }

    // ── Factory static ────────────────────────────────────────────────────────

    /** Affiche l'écran résultat depuis FenetreJeu. */
    public static void afficher(JFrame parent, model.Jeu jeu,
                                 Runnable onRejouer, Runnable onQuitter) {
        if (!jeu.isPartieTerminee()) return;

        Joueur vainqueur = jeu.getVainqueur();
        if (vainqueur == null) return;

        // Victoire = vainqueur est J1 (humain) ou J2 (humain), défaite = vainqueur est le bot
        // On détecte si le nom contient "Bot"
        boolean vainqueurEstBot = vainqueur.getNom().startsWith("Bot");
        TypeResultat type = vainqueurEstBot ? TypeResultat.DEFAITE : TypeResultat.VICTOIRE;

        EcranResultat dlg = new EcranResultat(
            parent, type, vainqueur.getNom(), jeu.getStats(), onRejouer, onQuitter
        );
        dlg.setVisible(true);
    }
}
