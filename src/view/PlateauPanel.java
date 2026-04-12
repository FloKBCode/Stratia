package view;

import controller.JeuController;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * PlateauPanel : dessin du plateau (JPanel avec paintComponent custom).
 * Supporte les thèmes via JeuController.getTheme().
 */
public class PlateauPanel extends JPanel {

    public static final int CELL_SIZE = 75;

    private static final Color SEL_COL  = new Color( 80, 200,  80, 190);
    private static final Color MOV_COL  = new Color(255, 200,   0, 170);
    private static final Color DST_COL  = new Color(  0, 200, 100, 170);
    private static final Color MUL_COL  = new Color(255,  80,  80, 190);

    private final JeuController controller;
    private final Runnable      onStateChange;

    public PlateauPanel(JeuController controller, Runnable onStateChange) {
        this.controller   = controller;
        this.onStateChange = onStateChange;
        setPreferredSize(new Dimension(CELL_SIZE * Plateau.TAILLE, CELL_SIZE * Plateau.TAILLE));
        setBorder(BorderFactory.createLineBorder(new Color(40, 30, 15), 3));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int col   = e.getX() / CELL_SIZE;
                int ligne = e.getY() / CELL_SIZE;
                if (controller.getJeu().getPlateau().estValide(ligne, col)) {
                    controller.handleCellClick(ligne, col);
                    repaint();
                    if (onStateChange != null) onStateChange.run();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Theme       theme       = controller.getTheme();
        List<Coup>  dispos      = controller.getCoupsDisponibles();
        List<int[]> jouables    = controller.getJeu().getPiecesJouables();
        int         selL        = controller.getSelectedLigne();
        int         selC        = controller.getSelectedColonne();
        boolean     multiCapt   = controller.getJeu().getPieceEnCours() != null;

        for (int i = 0; i < Plateau.TAILLE; i++) {
            for (int j = 0; j < Plateau.TAILLE; j++) {
                int x = j * CELL_SIZE, y = i * CELL_SIZE;

                // Fond de case
                g2.setColor((i+j)%2==0 ? theme.caseClair : theme.caseFonce);
                g2.fillRect(x, y, CELL_SIZE, CELL_SIZE);

                // Pièces jouables
                for (int[] pos : jouables)
                    if (pos[0]==i && pos[1]==j && !(i==selL && j==selC)) {
                        g2.setColor(multiCapt ? MUL_COL : MOV_COL);
                        g2.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                    }

                // Pièce sélectionnée
                if (i==selL && j==selC) { g2.setColor(SEL_COL); g2.fillRect(x,y,CELL_SIZE,CELL_SIZE); }

                // Destinations possibles
                for (Coup c : dispos) {
                    if (c.getToLigne()==i && c.getToColonne()==j) {
                        g2.setColor(DST_COL); g2.fillRect(x,y,CELL_SIZE,CELL_SIZE);
                        g2.setColor(new Color(0,130,60,220));
                        int d=CELL_SIZE/4;
                        g2.fillOval(x+CELL_SIZE/2-d/2, y+CELL_SIZE/2-d/2, d, d);
                    }
                }

                // Coordonnées
                g2.setColor(new Color(100,80,60,110));
                g2.setFont(new Font("Arial",Font.PLAIN,9));
                if (j==0) g2.drawString(String.valueOf(i), x+2, y+11);
                if (i==Plateau.TAILLE-1) g2.drawString(String.valueOf(j), x+CELL_SIZE-10, y+CELL_SIZE-2);

                // Pièce
                Piece p = controller.getJeu().getPlateau().getPiece(i,j);
                if (p != null) dessinePiece(g2, p, x, y, theme);
            }
        }
    }

    private void dessinePiece(Graphics2D g2, Piece p, int x, int y, Theme theme) {
        final int margin = 7, size = CELL_SIZE - 2*margin;
        int px = x+margin, py = y+margin;
        boolean blanc = p.getCouleur() == Piece.Couleur.BLANC;

        // Ombre
        g2.setColor(new Color(0,0,0,70));
        g2.fillOval(px+4, py+4, size, size);

        // Corps
        g2.setColor(blanc ? theme.pieceBlanche : theme.pieceNoire);
        g2.fillOval(px, py, size, size);

        // Reflet
        g2.setColor(blanc ? new Color(255,255,255,120) : new Color(150,100,100,70));
        g2.fillOval(px+size/4, py+size/5, size/3, size/4);

        // Contour
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(blanc ? new Color(160,150,120) : new Color(10,10,10));
        g2.drawOval(px, py, size, size);
        g2.setStroke(new BasicStroke(1f));

        // Couronne dame
        if (p.estDame()) {
            g2.setFont(new Font("Serif",Font.BOLD,24));
            g2.setColor(new Color(255,195,0));
            FontMetrics fm = g2.getFontMetrics();
            String crown = "♛";
            g2.drawString(crown, x+CELL_SIZE/2-fm.stringWidth(crown)/2,
                          y+CELL_SIZE/2+fm.getAscent()/2-4);
        }
    }
}
