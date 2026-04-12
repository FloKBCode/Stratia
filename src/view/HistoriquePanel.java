package view;

import model.Jeu;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * HistoriquePanel : panneau latéral affichant l'historique des coups (ArrayList).
 * Répond à l'exigence du cahier des charges : historique avec collections Java.
 */
public class HistoriquePanel extends JPanel {

    private final JTextArea textArea;
    private final Jeu       jeu;

    public HistoriquePanel(Jeu jeu) {
        this.jeu = jeu;
        setLayout(new BorderLayout(0, 5));
        setPreferredSize(new Dimension(230, 0));
        setBackground(new Color(35, 35, 55));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 2, 0, 0, new Color(60, 60, 90)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        // Titre
        JLabel titre = new JLabel("📋 Historique des coups");
        titre.setForeground(new Color(180, 180, 255));
        titre.setFont(new Font("Arial", Font.BOLD, 13));
        add(titre, BorderLayout.NORTH);

        // Zone de texte scrollable
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        textArea.setBackground(new Color(25, 25, 40));
        textArea.setForeground(new Color(200, 210, 230));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(4, 4, 4, 4));

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 110)));
        scroll.getVerticalScrollBar().setBackground(new Color(50, 50, 80));
        add(scroll, BorderLayout.CENTER);
    }

    /** Met à jour l'affichage depuis l'ArrayList de l'historique. */
    public void actualiser() {
        ArrayList<String> hist = jeu.getHistorique();
        StringBuilder sb = new StringBuilder();
        for (String s : hist) sb.append(s).append("\n");
        textArea.setText(sb.toString());
        // Scroll vers le bas automatiquement
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
}
