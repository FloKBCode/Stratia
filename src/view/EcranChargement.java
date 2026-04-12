package view;

import sound.SoundCache;
import sound.SoundManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

/**
 * EcranChargement : ecran de demarrage affiché pendant le preload audio.
 *
 * Séquence :
 *  1. Affiche l'ecran (undecorated, centré)
 *  2. Lance le preload dans un thread background
 *  3. Progresse sur la barre via SwingUtilities.invokeLater()
 *  4. A 100% -> pause 600ms puis transition vers EcranAccueil
 */
public class EcranChargement extends JWindow {

    // ── Couleurs ───────────────────────────────────────────────────────────────
    private static final Color BG       = new Color(10, 13, 24);
    private static final Color CARD     = new Color(18, 22, 38);
    private static final Color GOLD     = new Color(215, 170, 50);
    private static final Color GOLD_DIM = new Color(120, 95, 28);
    private static final Color ACCENT   = new Color(80, 110, 210);
    private static final Color WHITE    = Color.WHITE;
    private static final Color GREY     = new Color(140, 150, 180);
    private static final Color BAR_BG   = new Color(28, 32, 52);
    private static final Color BAR_FG   = new Color(80, 130, 220);
    private static final Color BAR_DONE = new Color(60, 190, 100);

    // ── Composants ────────────────────────────────────────────────────────────
    private final JLabel     lblEtape;
    private final JLabel     lblPct;
    private final JProgressBar progressBar;
    private final JLabel     lblTitre;
    private final JPanel     barreContainer;

    // ── Animation ─────────────────────────────────────────────────────────────
    private Timer pulseTimer;
    private float pulseAlpha = 1.0f;
    private boolean pulseDir = false;

    public EcranChargement() {
        setSize(480, 320);
        setLocationRelativeTo(null);
        // Coins arrondis sur Windows 11 (ignoré silencieusement si non supporté)
        try { setShape(new RoundRectangle2D.Float(0, 0, 480, 320, 18, 18)); } catch (Exception ignored) {}

        JPanel root = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Dégradé de fond
                GradientPaint gp = new GradientPaint(0, 0, BG, 0, getHeight(), new Color(14, 18, 36));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Bordure dorée
                g2.setColor(GOLD_DIM);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 16, 16);
                // Reflet haut
                g2.setColor(new Color(255, 255, 255, 12));
                g2.fillRoundRect(1, 1, getWidth()-2, 80, 16, 16);
            }
        };
        root.setOpaque(false);
        setContentPane(root);

        // ── Pièces décoratives ──────────────────────────────────────────────
        JLabel pieces = new JLabel("\u265F  \u265B  \u265F");
        pieces.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 32));
        pieces.setForeground(GOLD);
        pieces.setBounds(0, 30, 480, 45);
        pieces.setHorizontalAlignment(SwingConstants.CENTER);
        root.add(pieces);

        // ── Titre ────────────────────────────────────────────────────────────
        lblTitre = new JLabel("STRATIA");
        lblTitre.setFont(new Font("Arial", Font.BOLD, 36));
        lblTitre.setForeground(WHITE);
        lblTitre.setBounds(0, 82, 480, 46);
        lblTitre.setHorizontalAlignment(SwingConstants.CENTER);
        root.add(lblTitre);

        // ── Sous-titre ────────────────────────────────────────────────────────
        JLabel sub = new JLabel("Jeu de Dames — Edition 2026");
        sub.setFont(new Font("Arial", Font.ITALIC, 13));
        sub.setForeground(GREY);
        sub.setBounds(0, 130, 480, 22);
        sub.setHorizontalAlignment(SwingConstants.CENTER);
        root.add(sub);

        // ── Séparateur ────────────────────────────────────────────────────────
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(50, 55, 85));
        sep.setBounds(60, 162, 360, 2);
        root.add(sep);

        // ── Barre de progression ──────────────────────────────────────────────
        barreContainer = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Fond barre
                g2.setColor(BAR_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            }
        };
        barreContainer.setOpaque(false);
        barreContainer.setBounds(60, 185, 360, 12);
        root.add(barreContainer);

        progressBar = new JProgressBar(0, 100) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int filled = (int)((double) getValue() / getMaximum() * getWidth());
                // Fond
                g2.setColor(BAR_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                // Remplissage
                if (filled > 0) {
                    Color fg = getValue() >= 100 ? BAR_DONE : BAR_FG;
                    GradientPaint gp = new GradientPaint(0, 0, fg.brighter(), filled, 0, fg);
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, filled, getHeight(), getHeight(), getHeight());
                    // Reflet
                    g2.setColor(new Color(255, 255, 255, 40));
                    g2.fillRoundRect(2, 1, Math.max(filled - 4, 0), getHeight() / 2, 3, 3);
                }
            }
        };
        progressBar.setBorderPainted(false);
        progressBar.setOpaque(false);
        progressBar.setValue(0);
        progressBar.setBounds(0, 0, 360, 12);
        barreContainer.add(progressBar);

        // ── Étape courante ────────────────────────────────────────────────────
        lblEtape = new JLabel("Initialisation...");
        lblEtape.setFont(new Font("Arial", Font.PLAIN, 11));
        lblEtape.setForeground(GREY);
        lblEtape.setBounds(0, 204, 480, 18);
        lblEtape.setHorizontalAlignment(SwingConstants.CENTER);
        root.add(lblEtape);

        // ── Pourcentage ───────────────────────────────────────────────────────
        lblPct = new JLabel("0%");
        lblPct.setFont(new Font("Monospaced", Font.BOLD, 11));
        lblPct.setForeground(ACCENT);
        lblPct.setBounds(0, 222, 480, 16);
        lblPct.setHorizontalAlignment(SwingConstants.CENTER);
        root.add(lblPct);

        // ── Copyright ─────────────────────────────────────────────────────────
        JLabel copy = new JLabel("Florence  ·  Sarah  ·  Marly  —  Ynov Campus Paris B1");
        copy.setFont(new Font("Arial", Font.PLAIN, 10));
        copy.setForeground(new Color(70, 78, 110));
        copy.setBounds(0, 290, 480, 18);
        copy.setHorizontalAlignment(SwingConstants.CENTER);
        root.add(copy);

        // ── Animation pulse sur le titre ─────────────────────────────────────
        pulseTimer = new Timer(50, e -> {
            pulseAlpha += pulseDir ? 0.04f : -0.04f;
            if (pulseAlpha >= 1.0f) { pulseAlpha = 1.0f; pulseDir = false; }
            if (pulseAlpha <= 0.5f) { pulseAlpha = 0.5f; pulseDir = true;  }
            int a = (int)(pulseAlpha * 255);
            lblTitre.setForeground(new Color(255, 255, 255, a));
        });
        pulseTimer.start();
    }

    // ── Mise à jour de la barre (appeler depuis EDT) ───────────────────────────

    public void setProgression(String etape, int pct) {
        progressBar.setValue(pct);
        lblEtape.setText(etape);
        lblPct.setText(pct + "%");
        if (pct >= 100) {
            progressBar.setValue(100);
            lblPct.setForeground(BAR_DONE);
            lblEtape.setForeground(new Color(100, 220, 120));
        }
        repaint();
    }

    // ── Lancement complet ─────────────────────────────────────────────────────

    /**
     * Affiche l'écran, précharge les sons en background,
     * puis ouvre EcranAccueil et ferme l'écran de chargement.
     */
    public void demarrer() {
        setVisible(true);

        Thread worker = new Thread(() -> {
            SoundCache cache = new SoundCache();

            // Progression initiale
            SwingUtilities.invokeLater(() -> setProgression("Initialisation...", 5));
            pause(120);

            // Préchargement avec callbacks vers EDT
            cache.prechargerTout((etape, pct) ->
                SwingUtilities.invokeLater(() -> setProgression(etape, pct))
            );

            // Injecter le cache dans SoundManager
            SoundManager.setCache(cache);

            pause(600); // Laisser voir "100% — Prêt !"

            // Transition vers l'écran d'accueil
            SwingUtilities.invokeLater(() -> {
                pulseTimer.stop();
                new EcranAccueil().setVisible(true);
                dispose();
            });
        }, "preloader");
        worker.setDaemon(true);
        worker.start();
    }

    private void pause(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
