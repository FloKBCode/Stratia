package model;

import java.awt.Color;

/**
 * Theme : enumération des thèmes visuels disponibles.
 * Chaque thème définit sa palette de couleurs complète.
 */
public enum Theme {

    CLASSIQUE("Classique ☕",
        new Color(240, 217, 181),  // case claire
        new Color(181, 136,  99),  // case foncée
        new Color( 40,  40,  65),  // fond application
        new Color( 50,  50,  80),  // fond panneau info
        new Color( 25,  25,  40),  // fond historique
        new Color(245, 245, 235),  // pièce blanche
        new Color( 45,  35,  35)), // pièce noire

    OBSIDIAN("Obsidian 🌑",
        new Color( 75,  85, 105),  // case claire
        new Color( 22,  22,  32),  // case foncée
        new Color( 10,  10,  18),  // fond application
        new Color( 18,  18,  28),  // fond panneau info
        new Color( 12,  12,  20),  // fond historique
        new Color(220, 230, 255),  // pièce blanche (bleutée)
        new Color( 80,  20,  20)); // pièce noire (bordeaux)

    public final String  label;
    public final Color   caseClair, caseFonce;
    public final Color   fondApp, fondInfo, fondHisto;
    public final Color   pieceBlanche, pieceNoire;

    Theme(String label,
          Color clair, Color fonce,
          Color fondApp, Color fondInfo, Color fondHisto,
          Color blanc, Color noir) {
        this.label        = label;
        this.caseClair    = clair;
        this.caseFonce    = fonce;
        this.fondApp      = fondApp;
        this.fondInfo     = fondInfo;
        this.fondHisto    = fondHisto;
        this.pieceBlanche = blanc;
        this.pieceNoire   = noir;
    }
}
