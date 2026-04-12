import view.EcranChargement;
import javax.swing.*;

/**
 * Main : point d'entree.
 * Lance EcranChargement qui precharge tous les sons en memoire
 * puis ouvre EcranAccueil.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new EcranChargement().demarrer();
        });
    }
}
