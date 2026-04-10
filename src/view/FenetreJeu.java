package view;

import controller.JeuController;
import model.*;
import sound.SoundManager;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;

/**
 * FenetreJeu : fenêtre principale du jeu (JFrame).
 * Contient : plateau, info, historique, stats (toggle), boutons, Swing Timer pour chrono.
 */
public class FenetreJeu extends JFrame {

    private final JeuController  controller;
    private final PlateauPanel   plateauPanel;
    private final HistoriquePanel historiquePanel;
    private final InfoPanel       infoPanel;
    private final StatsPanel      statsPanel;

    private boolean statsVisible = false;
    private JButton btnStats;
    private JButton btnMute;
    private JButton btnTheme;

    private Timer chronoTimer;

    // ── Constructeur ──────────────────────────────────────────────────────────

    public FenetreJeu(JeuController controller) {
        this.controller = controller;
        controller.setCallbacks(
            this::actualiserUI,
            () -> SwingUtilities.invokeLater(this::actualiserUI),
            () -> SwingUtilities.invokeLater(this::onTimeOut)
        );

        setTitle("Jeu de Dames");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { confirmerQuitter(); }
        });

        applyThemeBg();
        setLayout(new BorderLayout(0, 0));

        infoPanel = new InfoPanel(controller);
        infoPanel.setPreferredSize(new Dimension(0, 80));
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

        // Swing Timer chrono : tick toutes les secondes
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

    // ── Panneau de boutons bas ────────────────────────────────────────────────

    private JPanel creerPanneauBas() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        p.setBackground(controller.getTheme().fondApp);

        JButton btnNew  = btn("🔄 Nouvelle",   new Color(55,115,175));
        JButton btnHelp = btn("📖 Aide",        new Color(70,150,80));
        btnStats        = btn("📊 Stats ▶",     new Color(80,100,160));
        btnTheme        = btn("🎨 Thème",        new Color(100,80,140));
        btnMute         = btn(muteLabel(),       new Color(100,100,100));
        JButton btnQuit = btn("✖ Quitter",       new Color(160,55,55));

        btnNew.addActionListener(e  -> confirmerNouvelle());
        btnHelp.addActionListener(e -> afficherAide());
        btnStats.addActionListener(e-> toggleStats());
        btnTheme.addActionListener(e -> changerTheme());
        btnMute.addActionListener(e -> {
            SoundManager.setMuet(!SoundManager.isMuet());
            btnMute.setText(muteLabel());
        });
        btnQuit.addActionListener(e -> confirmerQuitter());

        for (JButton b : new JButton[]{btnNew,btnHelp,btnStats,btnTheme,btnMute,btnQuit})
            p.add(b);
        return p;
    }

    private JButton btn(String t, Color bg) {
        JButton b = new JButton(t);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(new Font("Arial",Font.BOLD,12));
        b.setFocusPainted(false); b.setBorderPainted(false); b.setOpaque(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(115,32));
        Color hov = bg.brighter();
        b.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){ b.setBackground(hov); }
            public void mouseExited(MouseEvent e) { b.setBackground(bg);  }
        });
        return b;
    }

    private String muteLabel() { return SoundManager.isMuet() ? "🔇 Son OFF" : "🔊 Son ON"; }

    // ── Actions boutons ───────────────────────────────────────────────────────

    private void toggleStats() {
        statsVisible = !statsVisible;
        statsPanel.setVisible(statsVisible);
        btnStats.setText(statsVisible ? "📊 Stats ◀" : "📊 Stats ▶");
        pack();
    }

    private void changerTheme() {
        Theme t = controller.getTheme() == Theme.CLASSIQUE ? Theme.OBSIDIAN : Theme.CLASSIQUE;
        controller.setTheme(t);
        applyThemeBg();
        infoPanel.appliquerTheme();
        actualiserUI();
    }

    private void applyThemeBg() {
        Color bg = controller.getTheme().fondApp;
        getContentPane().setBackground(bg);
    }

    private void confirmerNouvelle() {
        int r = JOptionPane.showConfirmDialog(this,
            "Démarrer une nouvelle partie ?","Nouvelle partie",
            JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            controller.recommencer();
            chronoTimer.restart();
            actualiserUI();
        }
    }

    private void confirmerQuitter() {
        int r = JOptionPane.showConfirmDialog(this,
            "Quitter le jeu ?","Quitter",JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) { chronoTimer.stop(); System.exit(0); }
    }

    private void onTimeOut() {
        actualiserUI();
        JOptionPane.showMessageDialog(this,
            "⏰ Temps écoulé !\n" + controller.getJeu().getVainqueur().getNom() + " gagne !",
            "Fin de partie", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Fenêtre d'aide ────────────────────────────────────────────────────────

    private void afficherAide() {
        JDialog dlg = new JDialog(this, "📖 Aide — Jeu de Dames", true);
        dlg.setSize(480, 500);
        dlg.setLocationRelativeTo(this);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(new Color(40,40,60));
        tabs.setForeground(Color.WHITE);

        tabs.addTab("♟ Règles",  ongletRegles());
        tabs.addTab("💡 Astuces", ongletAstuces());

        dlg.add(tabs);
        dlg.setVisible(true);
    }

    private JScrollPane ongletRegles() {
        JTextArea t = textArea(
            "RÈGLES DU JEU DE DAMES\n"
          + "══════════════════════════════════\n\n"
          + "PLATEAU\n"
          + "  • Grille 8×8, cases sombres uniquement\n"
          + "  • 12 pièces par joueur\n"
          + "  • Le joueur Blanc commence toujours\n\n"
          + "DÉPLACEMENTS\n"
          + "  • Pion (●) : 1 case en diagonale vers l'avant\n"
          + "  • Dame (♛) : N cases en diagonale, 4 directions\n\n"
          + "CAPTURES\n"
          + "  • La capture est OBLIGATOIRE si possible\n"
          + "  • On saute par-dessus la pièce adverse\n"
          + "  • Captures multiples enchaînées en un seul tour\n"
          + "  • Un pion peut capturer dans toutes les diagonales\n\n"
          + "PROMOTION\n"
          + "  • Un pion atteignant la dernière rangée adverse\n"
          + "    devient Dame (♛)\n"
          + "  • La promotion arrête les captures multiples\n\n"
          + "CHRONO\n"
          + "  • Chaque joueur a 2 minutes\n"
          + "  • Si le temps est épuisé, le joueur perd\n"
          + "  • Alerte rouge sous 20 secondes\n\n"
          + "VICTOIRE\n"
          + "  • Le joueur sans pièces ou sans coup valide perd\n"
          + "  • Ou le joueur dont le temps est écoulé perd"
        );
        return new JScrollPane(t);
    }

    private JScrollPane ongletAstuces() {
        JTextArea t = textArea(
            "ASTUCES STRATÉGIQUES\n"
          + "══════════════════════════════════\n\n"
          + "🔰 DÉBUTANT\n"
          + "  • Avancez vos pièces vers le centre\n"
          + "  • Gardez la rangée arrière pour protéger\n"
          + "    contre les captures de dames adverses\n"
          + "  • Favorisez les captures en chaîne\n\n"
          + "⚔️  INTERMÉDIAIRE\n"
          + "  • Forcez l'adversaire à se positionner\n"
          + "    sur vos cases de capture\n"
          + "  • Sacrifiez une pièce pour en capturer deux\n"
          + "  • Protégez vos pièces en binôme\n\n"
          + "♛  AVANCÉ\n"
          + "  • Obtenez une Dame le plus vite possible\n"
          + "  • Une Dame en bord de plateau est moins utile\n"
          + "  • En fin de partie : Dame + 2 pions bat Dame seule\n\n"
          + "🎯  CONTRE LE BOT\n"
          + "  • Facile  : le bot joue aléatoirement\n"
          + "  • Moyen   : le bot préfère les captures\n"
          + "  • Difficile : le bot utilise le minimax\n"
          + "    → profondeur 6, difficile à battre !"
        );
        return new JScrollPane(t);
    }

    private JTextArea textArea(String txt) {
        JTextArea t = new JTextArea(txt);
        t.setEditable(false);
        t.setFont(new Font("Monospaced",Font.PLAIN,12));
        t.setBackground(new Color(30,30,45));
        t.setForeground(new Color(210,215,230));
        t.setMargin(new Insets(8,8,8,8));
        return t;
    }

    // ── Mise à jour globale de l'UI ───────────────────────────────────────────

    public void actualiserUI() {
        SwingUtilities.invokeLater(() -> {
            infoPanel.actualiser();
            historiquePanel.actualiser();
            if (statsVisible) statsPanel.actualiser();
            plateauPanel.repaint();

            if (controller.getJeu().isPartieTerminee() && !controller.estTourDuBot()) {
                chronoTimer.stop();
                JOptionPane.showMessageDialog(this,
                    "🎉 " + controller.getJeu().getVainqueur().getNom() + " remporte la partie !",
                    "Fin de partie", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }
}
