# ♟️ Stratia (Jeu De Dame) — Projet Java B1

> Jeu de dames à deux joueurs, développé en Java avec interface graphique **Swing**.

---

## 👥 Équipe

| Membre | Rôle principal |
|--------|----------------|
| Florence | Modèle (model/) + logique de jeu |
| Sarah | Vue principale (view/) + PlateauPanel |
| Marly | Contrôleur + intégration + README |

---

## 🎮 Fonctionnalités

- ♟ Plateau 8×8 avec règles classiques des dames
- ♛ Promotion automatique en Dame
- ⚡ Captures multiples (prise en chaîne obligatoire)
- 📋 Historique complet des coups (ArrayList)
- 🎨 Interface graphique Swing avec surlignage des coups possibles
- 🔄 Bouton « Nouvelle partie »
- 📖 Fenêtre de règles intégrée

---

## 🛠 Installation & Compilation

### Prérequis
- **Java JDK 11 ou supérieur** installé et accessible dans le PATH
- Vérifier : `java -version` et `javac -version`

### Compiler (Windows)
```batch
compile.bat
```

### Compiler (Linux / macOS)
```bash
chmod +x compile.sh run.sh
./compile.sh
```

### Lancer le jeu
```batch
run.bat          ← Windows
./run.sh         ← Linux/macOS
```

### Manuellement
```bash
# Compilation
javac -d out -sourcepath src src/Main.java src/model/*.java src/view/*.java src/controller/*.java

# Exécution
java -cp out Main
```

---

## 📁 Structure du projet

```
JeuDeDames/
├── src/
│   ├── Main.java                  ← Point d'entrée
│   ├── model/                     ← Logique métier (MVC — Modèle)
│   │   ├── Piece.java             ← Classe abstraite (encapsulation, polymorphisme)
│   │   ├── Pion.java              ← Hérite de Piece
│   │   ├── Dame.java              ← Hérite de Piece (pièce promue)
│   │   ├── Case.java              ← Représente une case du plateau
│   │   ├── Plateau.java           ← Grille 8×8 + gestion des pièces
│   │   ├── Joueur.java            ← Informations d'un joueur
│   │   ├── Coup.java              ← Représentation d'un déplacement
│   │   └── Jeu.java               ← Chef d'orchestre du jeu (règles, historique)
│   ├── view/                      ← Interface graphique (MVC — Vue)
│   │   ├── FenetreJeu.java        ← Fenêtre principale (JFrame)
│   │   ├── PlateauPanel.java      ← Dessin du plateau (JPanel custom)
│   │   ├── HistoriquePanel.java   ← Panneau historique (JTextArea scrollable)
│   │   └── InfoPanel.java         ← Bandeau d'informations
│   └── controller/                ← Gestion des événements (MVC — Contrôleur)
│       └── JeuController.java     ← Reçoit clics → exécute coups → notifie vue
├── out/                           ← Classes compilées (généré automatiquement)
├── compile.bat                    ← Script de compilation Windows
├── run.bat                        ← Script de lancement Windows
├── compile.sh                     ← Script de compilation Linux/macOS
├── run.sh                         ← Script de lancement Linux/macOS
└── README.md                      ← Ce fichier
```

---

## 🖼️ Aperçu de l'interface

*(Ajouter une capture d'écran ici avant la soutenance)*

---

## 🎨 Justification du choix de l'interface graphique : **Swing**

Après comparaison des trois principales solutions Java pour les GUI :

| Critère | Swing | JavaFX | AWT |
|---------|-------|--------|-----|
| Intégré au JDK | ✅ Oui | ❌ Non (depuis Java 11) | ✅ Oui |
| Prise en main | ✅ Simple | ⚠️ Courbe d'apprentissage | ❌ Limité |
| Documentation | ✅ Abondante | ✅ Moderne | ⚠️ Ancienne |
| Adapté B1 | ✅ Oui | ⚠️ Complexe (FXML, Maven) | ❌ Déprécié |
| Rendu custom | ✅ paintComponent | ✅ Canvas | ⚠️ Lourd |

**Choix retenu : Swing**

Swing est inclus nativement dans le JDK sans dépendance externe — la compilation
se fait avec un simple `javac`. Sa hiérarchie de classes (`JFrame → JPanel → JComponent`)
s'aligne parfaitement avec les notions de POO vues en cours (héritage, polymorphisme).
La méthode `paintComponent(Graphics g)` offre un contrôle total sur le rendu du plateau,
ce qui était indispensable pour dessiner les cases, les pièces et les surlignages.

---

## 📐 Concepts POO utilisés

| Concept | Où |
|---------|-----|
| **Encapsulation** | Tous les attributs `private`/`protected`, accès via getters/setters |
| **Héritage** | `Pion extends Piece`, `Dame extends Piece`, `FenetreJeu extends JFrame`, `PlateauPanel extends JPanel` |
| **Polymorphisme** | `estDame()` abstraite dans `Piece`, redéfinie dans `Pion` et `Dame` |
| **Collections** | `ArrayList<String>` pour l'historique, `List<Coup>` pour les coups valides |
| **Interface** | `ActionListener`, `MouseAdapter` pour la gestion des événements |
| **Architecture MVC** | `model/` ↔ `controller/` ↔ `view/` strictement séparés |

---

## 📅 Planning prévisionnel (Gantt simplifié)

```
Jour 1 : Conception (classes, architecture MVC, règles),  Implémentation modèle (Piece, Plateau, Jeu, règles),Implémentation vue Swing (plateau graphique, événements)
Jour 2 : Intégration contrôleur + tests + corrections, Finalisation (README, soutenance, nettoyage code)
```

---

## ⚠️ Règles du jeu rappel

- **Pion** : déplacement diagonal vers l'avant uniquement (1 case)
- **Dame** : déplacement diagonal dans toutes les directions (N cases)
- **Capture obligatoire** : si une prise est possible, elle doit être jouée
- **Capture multiple** : une même pièce peut enchaîner plusieurs prises en un tour
- **Promotion** : un pion atteignant la dernière rangée adverse devient Dame (♛)
- **Victoire** : le joueur sans pièces ou sans coup valide perd la partie
