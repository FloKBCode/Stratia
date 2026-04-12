#!/bin/bash
echo "============================================="
echo "  Stratia - Jeu de Dames - Compilation"
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
    src/model/Bot.java \
    src/model/Chrono.java \
    src/model/StatsPartie.java \
    src/model/Theme.java \
    src/sound/SoundCache.java \
    src/sound/SoundManager.java \
    src/controller/JeuController.java \
    src/view/PlateauPanel.java \
    src/view/HistoriquePanel.java \
    src/view/InfoPanel.java \
    src/view/StatsPanel.java \
    src/view/EcranResultat.java \
    src/view/FenetreJeu.java \
    src/view/EcranAccueil.java \
    src/view/EcranChargement.java
    src/util/IconLoader.java \

if [ $? -eq 0 ]; then
    echo ""
    echo " [OK] Compilation reussie !"
    echo " Lancez : ./run.sh"
else
    echo ""
    echo " [ERREUR] Echec de compilation."
    echo " Verifiez que le JDK est installe : javac -version"
fi
