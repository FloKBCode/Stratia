import view.EcranAccueil;
import javax.swing.*;

/**
 * Main : point d'entrée — lance l'écran d'accueil sur l'EDT Swing.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new EcranAccueil().setVisible(true);
        });
    }
}
