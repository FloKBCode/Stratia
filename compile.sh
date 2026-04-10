#!/bin/bash
echo "============================================="
echo "  Jeu de Dames - Compilation Java"
echo "============================================="

mkdir -p out

javac -encoding UTF-8 -d out -sourcepath src \
    src/Main.java \
    src/model/Piece.java \
    src/model/Pion.java \
    src/model/Dame.java \
    src/model/Case.java \
    src/model/Plateau.java \
    src/model/Coup.java \
    src/model/Joueur.java \
    src/model/Jeu.java \
    src/controller/JeuController.java \
    src/view/PlateauPanel.java \
    src/view/HistoriquePanel.java \
    src/view/InfoPanel.java \
    src/view/FenetreJeu.java

if [ $? -eq 0 ]; then
    echo ""
    echo " [OK] Compilation reussie !"
    echo " Lancez : ./run.sh"
else
    echo ""
    echo " [ERREUR] Echec de compilation."
    echo " Verifiez que le JDK est installe : javac -version"
fi
