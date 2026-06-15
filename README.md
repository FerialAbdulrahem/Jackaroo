# 🃏 Jackaroo Board Game

<div align="center">

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-007396?style=for-the-badge&logo=java&logoColor=white)
![Eclipse](https://img.shields.io/badge/Eclipse-2C2255?style=for-the-badge&logo=eclipse&logoColor=white)
![Status](https://img.shields.io/badge/Status-Complete-brightgreen?style=for-the-badge)

**A strategic single-player adaptation of the classic Middle Eastern board game — built in Java with a full GUI, 3 CPU opponents, and 15 unique card types.**

[🎮 View Screenshot](#-gameplay-screenshot) · [📖 Game Rules](#-game-rules) · [⚙️ Setup](#️-installation--setup) · [📁 Project Structure](#-project-structure)

</div>

---

## 📌 Table of Contents

- [About the Project](#-about-the-project)
- [Gameplay Screenshot](#-gameplay-screenshot)
- [Game Rules](#-game-rules)
  - [Board Layout & Zones](#board-layout--zones)
  - [Card Reference](#card-reference-15-types)
  - [Movement & Collision Rules](#movement--collision-rules)
- [Installation & Setup](#️-installation--setup)
- [How to Play](#-how-to-play)
- [Project Structure](#-project-structure)
- [Tech Stack](#️-tech-stack)
- [Contributors](#-contributors)

---

## 🎯 About the Project

**Jackaroo** is a competitive marble-and-card board game implemented as a Java desktop application. The human player controls **4 colored marbles** on a **100-cell circular track** and competes against **3 CPU opponents**, all trying to be the first to move all their marbles from the Home Zone into the Safe Zone.

| Detail | Info |
|--------|------|
| 📚 **Course** | Computer Programming Lab – Spring 2025, GUC |
| 👩‍💻 **Language** | Java (100%) |
| 🖥️ **GUI** | JavaFX |
| 🃏 **Deck** | 102 cards · 15 unique types |
| 🎲 **Players** | 1 Human vs. 3 CPU |
| 🏁 **Win Condition** | First to fill all 4 Safe Zone cells |

> 📄 **Full game specification:** [Game Description.pdf](./Game%20Description.pdf)

---

## 🖼️ Gameplay Screenshot

<div align="center">

[Jackaroo Game UI](https://github.com/FerialAbdulrahem/Jackaroo/blob/main/Photo.png)

*The main game board showing the circular marble track, player hands, CPU panels, and the Fire Pit discard pile.*

</div>

---

## 📖 Game Rules

### Board Layout & Zones

The board consists of a **100-cell circular track** plus special zones for each of the 4 players:

| Zone | Description | Rules |
|------|-------------|-------|
| 🏠 **Home Zone** | Where all 4 marbles begin | Marbles are inactive here. Requires an **Ace** or **King** to enter the track |
| 🔵 **Base Cell** | First cell on the track per player | Entry point from Home Zone. A friendly marble here **blocks passage** for that player |
| 🛡️ **Safe Zone** | Final destination (immune area) | Marbles here are **immune to all attacks**. Enter by exact step count only — cannot overshoot |
| 🚪 **Safe Zone Entry** | Cell immediately before Safe Zone | A marble parked here **blocks ALL players** from entering that Safe Zone |
| ⚠️ **Trap Cells** | 8 randomly placed cells on the track | Landing here **destroys your marble** (sends it back to Home). Trap **relocates** after each trigger |

---

### Card Reference (15 Types)

The deck contains **102 cards** across 15 types. Each player is dealt **4 cards** per round.

| Card | Rank | Action |
|------|------|--------|
| **Ace** | 1 | Field a marble from Home → Base Cell, **OR** move forward 1 step |
| **Two** | 2 | Move forward 2 steps |
| **Three** | 3 | Move forward 3 steps |
| **Four** | 4 | Move a marble **backwards** 4 steps |
| **Five** | 5 | Move **any marble on the track** (including opponents') 5 steps forward |
| **Six** | 6 | Move forward 6 steps |
| **Seven** | 7 | Split 7 steps between **two of your own marbles** (any combination: 1+6, 2+5, 3+4), **OR** move one marble 7 steps |
| **Eight** | 8 | Move forward 8 steps |
| **Nine** | 9 | Move forward 9 steps |
| **Ten** | 10 | **Discard** a random card from the **next player's** hand and **skip their turn** |
| **Jack** | 11 | **Swap** one of your marbles with an opponent's marble on the track (neither can be in a Base Cell) |
| **Queen** | 12 | **Discard** a random card from any **random opponent's** hand and **skip their turn** |
| **King** | 13 | Field a marble from Home **OR** move 13 steps, **destroying ALL marbles** in the path (bypasses blockage rules) |
| **Burner** | 14 | Destroy one opponent marble on the track → sends it to their Home Zone (cannot target Base/Safe/Home) |
| **Saver** | 15 | Teleport one of your marbles to a **random empty Safe Zone cell** |

> **Standard cards (2, 3, 6, 8, 9):** Move your marble forward by the card's rank value.

---

### Movement & Collision Rules

| Scenario | Result |
|----------|--------|
| Land on **opponent's marble** | Their marble is destroyed → returns to Home Zone |
| Attempt to land on **your own marble** | ❌ Invalid move |
| **King** moves through marbles | ALL marbles in the path are destroyed (including the destination) |
| **More than 1 marble** blocks your path | ❌ Movement is invalid |
| Land on a **Trap Cell** | Your marble destroyed → returns Home. Trap relocates |
| Enter **Safe Zone** with leftover steps | ❌ Must enter by exact count |

#### Special Card Exceptions
- **King** bypasses: own marble blocking, path blockage, and Safe Zone Entry blockage
- **Jack** swap: only valid if both marbles are on the general track and neither is in a Base Cell
- **Burner**: cannot target your own marbles or marbles in Base/Safe/Home zones
- **Five**: can move any marble — even CPU marbles — which adds a strategic layer

---

## ⚙️ Installation & Setup

### Prerequisites

- ☕ **Java JDK 8+** — [Download here](https://www.oracle.com/java/technologies/downloads/)
- 🖥️ **JavaFX SDK** (if not bundled with your JDK) — [Download here](https://openjfx.io/)
- 🐙 **Git** (optional)

### Clone & Run

```bash
# 1. Clone the repository
git clone https://github.com/FerialAbdulrahem/Jackaroo.git
cd Jackaroo

# 2. Navigate to the game source folder
cd JackrooGame

# 3. Compile all Java files
javac *.java

# 4. Run the game
java Main
```

### Running via Eclipse IDE

1. Open Eclipse → **File → Import → Existing Projects into Workspace**
2. Select the `Jackaroo/` root folder
3. Eclipse will auto-detect the `.project` and `.classpath` files
4. Right-click `Main.java` → **Run As → Java Application**

> **Note:** If JavaFX is not on your module path, add it under **Project → Properties → Java Build Path → Libraries → Add External JARs** and include all `.jar` files from your JavaFX `lib/` folder.

### Required Files

The game reads card data from a CSV file at startup:

```
Cards.csv   — 64 cards with fields: Code, Frequency, Name, Description, Rank, Suit
```

Ensure this file is present in the working directory or source folder before launching.

---

## 🎮 How to Play

### Turn Structure

```
Each Round:
  └── All 4 players receive 4 cards
      └── Each player's turn:
            1. Select a card from your hand
            2. Select a marble (if the card requires a target)
            3. Click PLAY to execute
            4. Card moves to Fire Pit
            5. Next player takes their turn
```

### Controls

| Action | How |
|--------|-----|
| Select a card | Click the card in your hand panel |
| Select a marble | Click the marble on the board |
| Execute action | Click **PLAY** button |
| Skip your turn | Click **SkipTurn** button |
| View rules | Click **Rules & Help** button |

### CPU Behavior

- Each CPU randomly selects a card from their hand
- If the card requires a marble target, the CPU picks one at random
- CPU turns execute automatically and immediately

### Winning

> 🏆 **First player to move all 4 marbles from their Home Zone into their Safe Zone wins the game.**

---

## 📁 Project Structure

```
Jackaroo/
│
├── JackrooGame/                  # 🎮 Core game source code (Java)
│   ├── Main.java                 #    Entry point — launches the JavaFX app
│   ├── MainScene.java            #    Primary game scene — board, UI, turn logic
│   ├── CircleGrid.java           #    Board rendering — marble positions, zones
│   └── *.java                    #    Supporting classes (Card, Player, CPU, etc.)
│
├── Description/                  # 📄 Assignment/specification files
├── .settings/                    # ⚙️  Eclipse IDE settings
│
├── Game Description.pdf          # 📖 Official 10-page game rules document
├── Photo.png                     # 🖼️  Screenshot of the game UI
├── Cards.csv                     # 🃏 Card deck definition (102 cards)
├── .classpath                    # Eclipse build path config
├── .project                      # Eclipse project metadata
└── README.md                     # 📚 This file
```

---

## 🛠️ Tech Stack

| Tool | Purpose |
|------|---------|
| ☕ **Java** | Core game logic, OOP architecture |
| 🎨 **JavaFX** | GUI — board rendering, card panels, dialogs |
| 📄 **CSV Parsing** | Loading 102-card deck from `Cards.csv` |
| 🔵 **Eclipse IDE** | Development environment |
| 🎲 **Custom Game Engine** | Turn management, collision detection, win checking |




This project was developed as part of a university course assignment at **GUC (German University in Cairo)**. It is intended for educational purposes.

---

<div align="center">

**🎴 Master the cards. Outsmart the CPUs. Be the first to the Safe Zone.**

⭐ If you found this project helpful, consider giving it a star!

</div>
