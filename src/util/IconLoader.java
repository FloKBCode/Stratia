package util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * IconLoader : charge le logo Stratia depuis assets/icon.png.
 *
 * Fournit une liste de tailles (16, 32, 48, 64, 128, 256 px) pour que
 * Windows choisisse automatiquement la meilleure résolution selon le contexte
 * (barre des tâches, Alt+Tab, raccourci bureau...).
 *
 * Utilisation dans n'importe quelle JFrame :
 *   frame.setIconImages(IconLoader.getIcones());
 */
public class IconLoader {

    private static final String ICON_PATH = "assets/icon.png";
    private static List<Image> icones = null;

    /**
     * Retourne la liste des icônes à toutes les tailles.
     * Chargement unique (mis en cache pour éviter les relectures disque).
     */
    public static List<Image> getIcones() {
        if (icones != null) return icones;

        icones = new ArrayList<>();
        try {
            BufferedImage source = ImageIO.read(new File(ICON_PATH));
            if (source == null) return icones;

            int[] sizes = {16, 24, 32, 48, 64, 128, 256};
            for (int s : sizes) {
                Image scaled = source.getScaledInstance(s, s, Image.SCALE_SMOOTH);
                // Convertir en BufferedImage pour meilleur rendu
                BufferedImage bi = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = bi.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawImage(scaled, 0, 0, null);
                g2.dispose();
                icones.add(bi);
            }
        } catch (Exception ignored) {
            // Si le fichier est absent, on retourne une liste vide
            // → Java garde son icône par défaut, pas de crash
        }
        return icones;
    }

    /**
     * Applique l'icône à une JFrame en une ligne.
     * Exemple : IconLoader.appliquer(this);
     */
    public static void appliquer(JFrame frame) {
        List<Image> imgs = getIcones();
        if (!imgs.isEmpty()) frame.setIconImages(imgs);
    }

    /**
     * Version pour JDialog (hérite l'icône de son parent JFrame si null).
     */
    public static void appliquer(JDialog dialog) {
        List<Image> imgs = getIcones();
        if (!imgs.isEmpty()) dialog.setIconImages(imgs);
    }

    /**
     * Version pour JWindow (écran de chargement).
     * JWindow n'affiche pas d'icône dans la barre des tâches,
     * mais certains OS la lisent quand même.
     */
    public static void appliquer(Window window) {
        List<Image> imgs = getIcones();
        if (!imgs.isEmpty()) window.setIconImages(imgs);
    }
}
