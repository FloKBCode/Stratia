package view;

import util.IconLoader;

import controller.JeuController;
import model.*;
import sound.SoundManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * EcranAccueil : fenêtre de démarrage — saisie noms, mode, niveau, thème.
 * Corrigé : suppression des emoji (rendu □ sur Windows), formatage des noms.
 */
public class EcranAccueil extends JFrame {

    private JTextField nomJ1Field, nomJ2Field;
    private JRadioButton rbHumain, rbFacile, rbMoyen, rbDifficile;
    private JRadioButton rbClassique, rbObsidian;
    private JButton btnMute;

    private static final Color BG      = new Color(13, 17, 30);
    private static final Color CARD_BG = new Color(22, 28, 45);
    private static final Color CARD_BD = new Color(45, 55, 90);
    private static final Color ACCENT  = new Color(100, 130, 220);
    private static final Color GOLD    = new Color(215, 170, 50);
    private static final Color WHITE   = Color.WHITE;
    private static final Color GREY    = new Color(160, 170, 195);

    public EcranAccueil() {
        setTitle("Jeu de Dames — Accueil");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        IconLoader.appliquer(this);
        setResizable(false);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BG);

        add(creerEntete(),     BorderLayout.NORTH);
        add(creerFormulaire(), BorderLayout.CENTER);
        add(creerBasPage(),    BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(460, 580));
        setLocationRelativeTo(null);
        SoundManager.demarrerMusique();
    }

    // ── En-tête ───────────────────────────────────────────────────────────────

    private JPanel creerEntete() {
        JPanel p = new JPanel(new GridLayout(3, 1, 0, 4));
        p.setBackground(BG);
        p.setBorder(BorderFactory.createEmptyBorder(28, 20, 10, 20));

        // Pièces Unicode (♟♛♟ sont supportés par la plupart des polices Windows)
        JLabel logo = new JLabel("\u265F\u265B\u265F", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 38));
        logo.setForeground(GOLD);

        JLabel titre = new JLabel("JEU DE DAMES", SwingConstants.CENTER);
        titre.setFont(new Font("Arial", Font.BOLD, 28));
        titre.setForeground(WHITE);

        JLabel sousTitre = new JLabel("Stratia — Edition 2026", SwingConstants.CENTER);
        sousTitre.setFont(new Font("Arial", Font.ITALIC, 13));
        sousTitre.setForeground(new Color(130, 145, 190));

        p.add(logo); p.add(titre); p.add(sousTitre);
        return p;
    }

    // ── Formulaire ────────────────────────────────────────────────────────────

    private JPanel creerFormulaire() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG);
        p.setBorder(BorderFactory.createEmptyBorder(10, 28, 10, 28));

        p.add(carte(creerSectionJoueurs()));
        p.add(Box.createVerticalStrut(10));
        p.add(carte(creerSectionMode()));
        p.add(Box.createVerticalStrut(10));
        p.add(carte(creerSectionTheme()));
        return p;
    }

    private JPanel carte(JPanel contenu) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BD, 1),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        card.add(contenu);
        return card;
    }

    private JPanel creerSectionJoueurs() {
        JPanel p = new JPanel(new GridLayout(0, 1, 0, 8));
        p.setBackground(CARD_BG);
        p.add(sectionTitre("Joueurs"));

        JPanel ligneJ1 = new JPanel(new BorderLayout(10, 0));
        ligneJ1.setBackground(CARD_BG);
        JLabel lJ1 = new JLabel("Joueur 1 (Blanc) :");
        lJ1.setForeground(WHITE); lJ1.setFont(new Font("Arial", Font.PLAIN, 12));
        nomJ1Field = champ("Joueur 1");
        ligneJ1.add(lJ1, BorderLayout.WEST);
        ligneJ1.add(nomJ1Field, BorderLayout.CENTER);

        JPanel ligneJ2 = new JPanel(new BorderLayout(10, 0));
        ligneJ2.setBackground(CARD_BG);
        JLabel lJ2 = new JLabel("Joueur 2 (Noir)  :");
        lJ2.setForeground(new Color(170, 175, 225)); lJ2.setFont(new Font("Arial", Font.PLAIN, 12));
        nomJ2Field = champ("Joueur 2");
        ligneJ2.add(lJ2, BorderLayout.WEST);
        ligneJ2.add(nomJ2Field, BorderLayout.CENTER);

        p.add(ligneJ1); p.add(ligneJ2);
        return p;
    }

    private JPanel creerSectionMode() {
        JPanel p = new JPanel(new GridLayout(0, 1, 0, 6));
        p.setBackground(CARD_BG);
        p.add(sectionTitre("Mode de jeu"));

        ButtonGroup bg = new ButtonGroup();
        rbHumain    = radio("Humain vs Humain",  bg, true);
        rbFacile    = radio("Bot  —  Facile",    bg, false);
        rbMoyen     = radio("Bot  —  Moyen",     bg, false);
        rbDifficile = radio("Bot  —  Difficile", bg, false);

        ActionListener l = e -> nomJ2Field.setEnabled(rbHumain.isSelected());
        rbHumain.addActionListener(l); rbFacile.addActionListener(l);
        rbMoyen.addActionListener(l);  rbDifficile.addActionListener(l);

        p.add(rbHumain); p.add(rbFacile); p.add(rbMoyen); p.add(rbDifficile);
        return p;
    }

    private JPanel creerSectionTheme() {
        JPanel p = new JPanel(new GridLayout(0, 1, 0, 6));
        p.setBackground(CARD_BG);
        p.add(sectionTitre("Theme visuel"));

        ButtonGroup bg = new ButtonGroup();
        rbClassique = radio("Classique (bois chaud)", bg, true);
        rbObsidian  = radio("Obsidian (sombre)",      bg, false);

        p.add(rbClassique); p.add(rbObsidian);
        return p;
    }

    // ── Bas de page ───────────────────────────────────────────────────────────

    private JPanel creerBasPage() {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setBackground(BG);
        p.setBorder(BorderFactory.createEmptyBorder(10, 28, 22, 28));

        btnMute = new JButton("Son ON");
        btnMute.setBackground(new Color(60, 65, 100));
        btnMute.setForeground(WHITE);
        btnMute.setFont(new Font("Arial", Font.BOLD, 12));
        btnMute.setFocusPainted(false); btnMute.setBorderPainted(false); btnMute.setOpaque(true);
        btnMute.setPreferredSize(new Dimension(95, 44));
        btnMute.addActionListener(e -> {
            SoundManager.setMuet(!SoundManager.isMuet());
            btnMute.setText(SoundManager.isMuet() ? "Son OFF" : "Son ON");
            btnMute.setBackground(SoundManager.isMuet() ? new Color(80, 40, 40) : new Color(60, 65, 100));
        });

        JButton btnJouer = new JButton("JOUER");
        btnJouer.setBackground(new Color(40, 140, 70));
        btnJouer.setForeground(WHITE);
        btnJouer.setFont(new Font("Arial", Font.BOLD, 17));
        btnJouer.setFocusPainted(false); btnJouer.setBorderPainted(false); btnJouer.setOpaque(true);
        btnJouer.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnJouer.addActionListener(e -> lancerPartie());
        // Hover
        btnJouer.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnJouer.setBackground(new Color(50, 170, 90)); }
            public void mouseExited(MouseEvent e)  { btnJouer.setBackground(new Color(40, 140, 70)); }
        });

        p.add(btnMute,   BorderLayout.WEST);
        p.add(btnJouer,  BorderLayout.CENTER);
        return p;
    }

    // ── Lancement ─────────────────────────────────────────────────────────────

    private void lancerPartie() {
        String nom1 = formaterNom(nomJ1Field.getText());
        String nom2;
        Bot bot = null;

        if (rbHumain.isSelected()) {
            nom2 = formaterNom(nomJ2Field.getText());
        } else {
            Bot.Niveau niv = rbFacile.isSelected()    ? Bot.Niveau.FACILE
                           : rbMoyen.isSelected()     ? Bot.Niveau.MOYEN
                           : Bot.Niveau.DIFFICILE;
            bot  = new Bot(niv);
            nom2 = "Bot (" + niv.label + ")";
        }

        Theme theme = rbClassique.isSelected() ? Theme.CLASSIQUE : Theme.OBSIDIAN;

        Jeu           jeu        = new Jeu(nom1, nom2);
        JeuController controller = new JeuController(jeu, bot, theme);
        FenetreJeu    fenetre    = new FenetreJeu(controller);

        fenetre.setVisible(true);
        dispose();
    }

    /** Capitalise la première lettre et trim. Ex: "flo" -> "Flo" */
    private String formaterNom(String s) {
        if (s == null) return "Joueur";
        s = s.trim();
        if (s.isEmpty()) return "Joueur";
        // Ignorer si c'est le placeholder par défaut
        if (s.equals("Joueur 1") || s.equals("Joueur 2")) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }

    // ── Utilitaires UI ────────────────────────────────────────────────────────

    private JLabel sectionTitre(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Arial", Font.BOLD, 13));
        l.setForeground(ACCENT);
        return l;
    }

    private JTextField champ(String placeholder) {
        JTextField f = new JTextField(placeholder);
        f.setBackground(new Color(32, 38, 60));
        f.setForeground(WHITE);
        f.setCaretColor(WHITE);
        f.setFont(new Font("Arial", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BD),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (f.getText().equals(placeholder)) { f.setText(""); f.setForeground(WHITE); }
            }
            public void focusLost(FocusEvent e) {
                if (f.getText().isEmpty()) { f.setText(placeholder); f.setForeground(GREY); }
            }
        });
        f.setForeground(GREY);
        return f;
    }

    private JRadioButton radio(String txt, ButtonGroup bg, boolean selected) {
        JRadioButton r = new JRadioButton(txt, selected);
        r.setBackground(CARD_BG);
        r.setForeground(new Color(210, 215, 235));
        r.setFont(new Font("Arial", Font.PLAIN, 12));
        bg.add(r);
        return r;
    }
}
