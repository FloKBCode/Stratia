package view;

import util.IconLoader;

import controller.JeuController;
import model.*;
import sound.SoundManager;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;

/**
 * FenetreJeu : fenetre principale du jeu (JFrame).
 * Corrigé : emoji remplacés, EcranResultat pour fin de partie.
 */
public class FenetreJeu extends JFrame {

    private final JeuController   controller;
    private final PlateauPanel    plateauPanel;
    private final HistoriquePanel historiquePanel;
    private final InfoPanel       infoPanel;
    private final StatsPanel      statsPanel;

    private boolean statsVisible = false;
    private JButton btnStats;
    private JButton btnMute;
    private boolean finAffichee = false;  // evite d'afficher 2x la fenetre de fin

    private Timer chronoTimer;

    public FenetreJeu(JeuController controller) {
        this.controller = controller;
        controller.setCallbacks(
            this::actualiserUI,
            () -> SwingUtilities.invokeLater(this::actualiserUI),
            () -> SwingUtilities.invokeLater(this::onTimeOut)
        );

        setTitle("Jeu de Dames");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        IconLoader.appliquer(this);
        setResizable(false);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { confirmerQuitter(); }
        });

        applyThemeBg();
        setLayout(new BorderLayout(0, 0));

        infoPanel = new InfoPanel(controller);
        infoPanel.setPreferredSize(new Dimension(0, 82));
        add(infoPanel, BorderLayout.NORTH);

        plateauPanel = new PlateauPanel(controller, this::actualiserUI);
        JPanel centreWrapper = new JPanel(new GridBagLayout());
        centreWrapper.setBackground(controller.getTheme().fondApp);
        centreWrapper.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        centreWrapper.add(plateauPanel);
        add(centreWrapper, BorderLayout.CENTER);

        historiquePanel = new HistoriquePanel(controller.getJeu());
        add(historiquePanel, BorderLayout.EAST);

        statsPanel = new StatsPanel(controller);
        statsPanel.setVisible(false);
        add(statsPanel, BorderLayout.WEST);

        add(creerPanneauBas(), BorderLayout.SOUTH);

        chronoTimer = new Timer(1000, e -> {
            controller.tickChrono();
            actualiserUI();
        });
        chronoTimer.start();
        controller.demarrerChrono();

        SoundManager.demarrerMusique();
        actualiserUI();
        pack();
        setLocationRelativeTo(null);
    }

    // ── Panneau boutons ───────────────────────────────────────────────────────

    private JPanel creerPanneauBas() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 9, 8));
        p.setBackground(controller.getTheme().fondApp);

        JButton btnNew  = btn("Nouvelle",  new Color(45, 105, 175));
        JButton btnHelp = btn("Aide",      new Color(55, 135, 70));
        btnStats        = btn("Stats  >",  new Color(65, 90, 160));
        JButton btnThem = btn("Theme",     new Color(85, 65, 145));
        btnMute         = btn("Son ON",    new Color(80, 85, 110));
        JButton btnQuit = btn("Quitter",   new Color(155, 45, 45));

        btnNew.addActionListener(e  -> confirmerNouvelle());
        btnHelp.addActionListener(e -> afficherAide());
        btnStats.addActionListener(e-> toggleStats());
        btnThem.addActionListener(e -> changerTheme());
        btnMute.addActionListener(e -> {
            SoundManager.setMuet(!SoundManager.isMuet());
            btnMute.setText(SoundManager.isMuet() ? "Son OFF" : "Son ON");
            btnMute.setBackground(SoundManager.isMuet() ? new Color(80,40,40) : new Color(80,85,110));
        });
        btnQuit.addActionListener(e -> confirmerQuitter());

        for (JButton b : new JButton[]{btnNew, btnHelp, btnStats, btnThem, btnMute, btnQuit})
            p.add(b);
        return p;
    }

    private JButton btn(String t, Color bg) {
        JButton b = new JButton(t);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(new Font("Arial", Font.BOLD, 12));
        b.setFocusPainted(false); b.setBorderPainted(false); b.setOpaque(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(100, 32));
        Color hov = bg.brighter();
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(hov); }
            public void mouseExited(MouseEvent e)  { b.setBackground(bg);  }
        });
        return b;
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void toggleStats() {
        statsVisible = !statsVisible;
        statsPanel.setVisible(statsVisible);
        btnStats.setText(statsVisible ? "Stats  <" : "Stats  >");
        pack();
    }

    private void changerTheme() {
        Theme t = controller.getTheme() == Theme.CLASSIQUE ? Theme.OBSIDIAN : Theme.CLASSIQUE;
        controller.setTheme(t);
        applyThemeBg();
        infoPanel.appliquerTheme();
        plateauPanel.repaint();
    }

    private void applyThemeBg() {
        getContentPane().setBackground(controller.getTheme().fondApp);
    }

    private void confirmerNouvelle() {
        int r = JOptionPane.showConfirmDialog(this,
            "Demarrer une nouvelle partie ?", "Nouvelle partie",
            JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            controller.recommencer();
            finAffichee = false;
            chronoTimer.restart();
            actualiserUI();
        }
    }

    private void confirmerQuitter() {
        int r = JOptionPane.showConfirmDialog(this,
            "Quitter le jeu ?", "Quitter", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) { chronoTimer.stop(); System.exit(0); }
    }

    private void onTimeOut() {
        actualiserUI();
        afficherFinPartie();
    }

    // ── Fenetre d'aide ────────────────────────────────────────────────────────

    private void afficherAide() {
        JDialog dlg = new JDialog(this, "Aide - Jeu de Dames", true);
        dlg.setSize(480, 500);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(new Color(22, 25, 42));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(new Color(30, 34, 55));
        tabs.setForeground(Color.WHITE);
        tabs.setFont(new Font("Arial", Font.BOLD, 12));

        tabs.addTab("Regles",   ongletRegles());
        tabs.addTab("Astuces",  ongletAstuces());

        dlg.add(tabs);
        dlg.setVisible(true);
    }

    private JScrollPane ongletRegles() {
        return scrollTA(
            "REGLES DU JEU DE DAMES\n"
          + "=================================\n\n"
          + "PLATEAU\n"
          + "  . Grille 8x8, cases sombres uniquement\n"
          + "  . 12 pieces par joueur\n"
          + "  . Le joueur Blanc commence toujours\n\n"
          + "DEPLACEMENTS\n"
          + "  . Pion : 1 case en diagonale vers l'avant\n"
          + "  . Dame : N cases en diagonale, 4 directions\n\n"
          + "CAPTURES\n"
          + "  . La capture est OBLIGATOIRE si possible\n"
          + "  . On saute par-dessus la piece adverse\n"
          + "  . Captures multiples en un seul tour\n\n"
          + "PROMOTION\n"
          + "  . Un pion atteignant la derniere rangee\n"
          + "    adverse devient Dame\n"
          + "  . La promotion arrete les captures multiples\n\n"
          + "CHRONO\n"
          + "  . Chaque joueur a 2 minutes\n"
          + "  . Alerte rouge sous 20 secondes\n\n"
          + "VICTOIRE\n"
          + "  . Sans pieces ou sans coup valide -> perd\n"
          + "  . Temps ecoule -> perd"
        );
    }

    private JScrollPane ongletAstuces() {
        return scrollTA(
            "ASTUCES STRATEGIQUES\n"
          + "=================================\n\n"
          + "DEBUTANT\n"
          + "  . Avancez vos pieces vers le centre\n"
          + "  . Gardez la rangee arriere pour proteger\n"
          + "  . Favorisez les captures en chaine\n\n"
          + "INTERMEDIAIRE\n"
          + "  . Forcez l'adversaire sur vos cases de capture\n"
          + "  . Sacrifiez une piece pour en capturer deux\n"
          + "  . Protegez vos pieces en binome\n\n"
          + "AVANCE\n"
          + "  . Obtenez une Dame le plus vite possible\n"
          + "  . Une Dame en bord de plateau est moins utile\n"
          + "  . Dame + 2 pions bat Dame seule en fin de partie\n\n"
          + "CONTRE LE BOT\n"
          + "  . Facile   : le bot joue aleatoirement\n"
          + "  . Moyen    : le bot prefere les captures\n"
          + "  . Difficile: minimax profondeur 6, dur a battre !"
        );
    }

    private JScrollPane scrollTA(String txt) {
        JTextArea t = new JTextArea(txt);
        t.setEditable(false);
        t.setFont(new Font("Monospaced", Font.PLAIN, 12));
        t.setBackground(new Color(18, 20, 38));
        t.setForeground(new Color(205, 210, 230));
        t.setMargin(new Insets(10, 10, 10, 10));
        return new JScrollPane(t);
    }

    // ── Mise a jour UI ────────────────────────────────────────────────────────

    public void actualiserUI() {
        SwingUtilities.invokeLater(() -> {
            infoPanel.actualiser();
            historiquePanel.actualiser();
            if (statsVisible) statsPanel.actualiser();
            plateauPanel.repaint();

            if (controller.getJeu().isPartieTerminee()
                    && !controller.estTourDuBot()
                    && !finAffichee) {
                finAffichee = true;
                chronoTimer.stop();
                // Léger délai pour laisser le repaint se terminer
                Timer t = new Timer(300, e -> afficherFinPartie());
                t.setRepeats(false); t.start();
            }
        });
    }

    private void afficherFinPartie() {
        EcranResultat.afficher(this, controller.getJeu(),
            () -> { // onRejouer
                controller.recommencer();
                finAffichee = false;
                chronoTimer.restart();
                actualiserUI();
            },
            () -> System.exit(0) // onQuitter
        );
    }
}
