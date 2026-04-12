package view;

import model.Jeu;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * HistoriquePanel : panneau latéral affichant l'historique des coups (ArrayList).
 * Corrigé : suppression des emoji.
 */
public class HistoriquePanel extends JPanel {

    private final JTextArea textArea;
    private final Jeu       jeu;

    public HistoriquePanel(Jeu jeu) {
        this.jeu = jeu;
        setLayout(new BorderLayout(0, 5));
        setPreferredSize(new Dimension(230, 0));
        setBackground(new Color(20, 22, 38));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 2, 0, 0, new Color(45, 50, 85)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JLabel titre = new JLabel("Historique des coups");
        titre.setForeground(new Color(160, 170, 230));
        titre.setFont(new Font("Arial", Font.BOLD, 13));
        add(titre, BorderLayout.NORTH);

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        textArea.setBackground(new Color(14, 15, 28));
        textArea.setForeground(new Color(195, 205, 225));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(5, 5, 5, 5));

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(50, 55, 90)));
        add(scroll, BorderLayout.CENTER);
    }

    public void actualiser() {
        ArrayList<String> hist = jeu.getHistorique();
        StringBuilder sb = new StringBuilder();
        for (String s : hist) sb.append(s).append("\n");
        textArea.setText(sb.toString());
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
}
