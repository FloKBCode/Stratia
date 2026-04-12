package view;

import controller.JeuController;
import model.Chrono;
import model.Piece;

import javax.swing.*;
import java.awt.*;

/**
 * InfoPanel : bandeau supérieur — joueur actuel, score, chrono.
 * Corrigé : suppression des emoji (rendu carre sur Windows).
 */
public class InfoPanel extends JPanel {

    private final JLabel joueurLabel, scoreLabel, messageLabel;
    private final JLabel chronoBlancLabel, chronoNoirLabel;
    private final JeuController controller;

    public InfoPanel(JeuController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(5, 0));
        setBackground(controller.getTheme().fondInfo);
        setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));

        chronoBlancLabel = chronoLabel();
        chronoNoirLabel  = chronoLabel();

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(controller.getTheme().fondInfo);
        JLabel lblB = new JLabel("Blanc");
        lblB.setForeground(new Color(160,160,180)); lblB.setFont(new Font("Arial",Font.PLAIN,10));
        leftPanel.add(lblB,              BorderLayout.NORTH);
        leftPanel.add(chronoBlancLabel,  BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(controller.getTheme().fondInfo);
        JLabel lblN = new JLabel("Noir", SwingConstants.RIGHT);
        lblN.setForeground(new Color(160,160,180)); lblN.setFont(new Font("Arial",Font.PLAIN,10));
        rightPanel.add(lblN,            BorderLayout.NORTH);
        rightPanel.add(chronoNoirLabel, BorderLayout.CENTER);

        JPanel centre = new JPanel(new GridLayout(3, 1, 0, 2));
        centre.setBackground(controller.getTheme().fondInfo);

        joueurLabel  = new JLabel("", SwingConstants.CENTER);
        joueurLabel.setFont(new Font("Arial", Font.BOLD, 16));

        scoreLabel   = new JLabel("", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        scoreLabel.setForeground(new Color(180, 190, 220));

        messageLabel = new JLabel("", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.ITALIC, 11));

        centre.add(joueurLabel);
        centre.add(scoreLabel);
        centre.add(messageLabel);

        add(leftPanel,  BorderLayout.WEST);
        add(centre,     BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    private JLabel chronoLabel() {
        JLabel l = new JLabel("2:00", SwingConstants.CENTER);
        l.setFont(new Font("Monospaced", Font.BOLD, 22));
        l.setForeground(new Color(190, 225, 190));
        l.setPreferredSize(new Dimension(80, 35));
        return l;
    }

    public void actualiser() {
        var jeu    = controller.getJeu();
        var chrono = controller.getChrono();

        int blancs = jeu.getPlateau().compterPieces(Piece.Couleur.BLANC);
        int noirs  = jeu.getPlateau().compterPieces(Piece.Couleur.NOIR);
        scoreLabel.setText(jeu.getJoueur1().getNom() + " : " + blancs
                         + "  |  " + jeu.getJoueur2().getNom() + " : " + noirs);

        boolean critB = chrono.estCritique(Piece.Couleur.BLANC);
        boolean critN = chrono.estCritique(Piece.Couleur.NOIR);
        chronoBlancLabel.setText(chrono.formater(Piece.Couleur.BLANC));
        chronoNoirLabel .setText(chrono.formater(Piece.Couleur.NOIR));
        chronoBlancLabel.setForeground(critB ? new Color(255,80,80) : new Color(190,225,190));
        chronoNoirLabel .setForeground(critN ? new Color(255,80,80) : new Color(190,225,190));

        if (jeu.isPartieTerminee()) {
            joueurLabel.setText("Victoire — " + jeu.getVainqueur().getNom() + " !");
            joueurLabel.setForeground(new Color(255, 195, 50));
            messageLabel.setText(""); return;
        }

        boolean estBlanc = jeu.getJoueurActuel().getCouleur() == Piece.Couleur.BLANC;
        boolean botTour  = controller.estTourDuBot();
        joueurLabel.setText("Tour : " + jeu.getJoueurActuel().getNom() + (estBlanc ? "  o" : "  •"));
        joueurLabel.setForeground(estBlanc ? new Color(240,240,200) : new Color(130,160,255));

        if (botTour) {
            messageLabel.setText("Le bot reflechit...");
            messageLabel.setForeground(new Color(200, 200, 80));
        } else if (jeu.getPieceEnCours() != null) {
            messageLabel.setText("Capture multiple obligatoire !");
            messageLabel.setForeground(new Color(255, 100, 80));
        } else {
            messageLabel.setText("Cliquez sur une piece surlignee");
            messageLabel.setForeground(new Color(170, 180, 150));
        }
    }

    public void appliquerTheme() {
        Color bg = controller.getTheme().fondInfo;
        setBackground(bg);
        for (Component c : getComponents())
            if (c instanceof JPanel panel) panel.setBackground(bg);
    }
}
