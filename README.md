Perfect! Now that I've read the actual `Game Description.pdf`, I can see the **Jackaroo** game is much more sophisticated than I initially thought. It's a **single-player vs. 3 CPU players** card-marble hybrid game with unique mechanics.

Here is the **corrected and detailed documentation** based on the actual game rules and the screenshot `Photo.png`:

---

# 🃏 Jackaroo Board Game

This repository contains a **Java-based strategic board/card game** — a unique single-player adaptation of the classic Middle Eastern game "Jackaroo" where you compete against 3 CPU players using marbles and a custom 102-card deck.

---

## 📂 Project Overview

| 🌟 Project | 📘 Course | 🛠️ Languages/Tools | 📎 Link |
|------------|-----------|--------------------|---------|
| 🎲 **Jackaroo Board Game** | Computer Programming Lab (Spring 2025) – GUC | Java, OOP, CSV Parsing | [Go to Project](https://github.com/FerialAbdulrahem/Jackaroo) |

---

## 🎲 Jackaroo: A New Game Spin
**Course:** Computer Programming Lab – Spring 2025  
**Instructors:** Prof. Dr. Slim Abdennadher, Assoc. Prof. Mervat Abu-ElKheir, Dr. Ahmed Abdelfattah  
**Languages/Tools:** Java, Object-Oriented Programming, CSV File Handling

📖 **Description:**  
Designed and implemented a **strategic single-player board/card game** where the human player controls 4 colored marbles against 3 CPU players. The game features a 100-cell track, special zones (Home, Safe, Base, Entry), trap cells, and a custom 102-card deck with 15 card types (including standard ranks Ace-King plus two wild cards: Burner and Saver). Players must strategically play cards to move marbles, swap positions, burn opponent marbles, save their own marbles, field new marbles from Home zone, or discard opponents' cards and skip their turns.

📄 [Read Game Description (PDF)](https://github.com/FerialAbdulrahem/Jackaroo/blob/main/Game%20Description.pdf)  
🖼️ [View Game Screenshot (Photo.png)](https://github.com/FerialAbdulrahem/Jackaroo/blob/main/Photo.png)

✅ **Outcome:** Delivered a complete Java implementation of a complex turn-based strategy game with 3 CPU opponents, special card effects, collision mechanics, trap cells, and win condition checking.

📎 [Go to Project Folder](https://github.com/FerialAbdulrahem/Jackaroo/tree/main/JackrooGame)

---

## 🎮 Game Rules Summary (Based on Actual Game Description)

### Game Setup
| Element | Description |
|---------|-------------|
| **Players** | 1 human vs 3 CPU players (CPU1, CPU2, CPU3 as seen in screenshot) |
| **Marbles** | 4 uniquely colored marbles per player |
| **Board** | 100-cell main track + Home Zones + Safe Zones + Base Cells + Entry Cells |
| **Deck** | 102 custom cards (15 types, including Ace to King + Burner + Saver) |
| **Fire Pit** | Discard pile where played cards go |
| **Trap Cells** | 8 randomly positioned cells that destroy marbles that land on them |

### Game Objective
**Be the first player to move all 4 of your marbles from your Home Zone into your Safe Zone.**

### Game Flow
1. **Each Round:** Every player receives 4 cards
2. **Each Turn:** Play 1 card from hand, execute its action
3. **CPU Turns:** Randomly select and play cards
4. **Rounds continue** until someone moves all marbles to Safe Zone

---

## 🃏 Card Actions (15 Card Types)

| Card | Code | Action Description |
|------|------|---------------------|
| **Ace** | 1 | Field a marble from Home Zone to Base cell OR act as standard card |
| **King** | 13 | Field a marble OR move 13 steps destroying ALL marbles in path (bypasses self-block, path blockage, safe zone entry rules) |
| **Queen** | 12 | Discard random card from random opponent's hand AND skip their turn |
| **Jack** | 11 | Swap one of your marbles with an opponent's marble on track (not in Base cell) |
| **Ten** | 10 | Discard random card from NEXT player's hand AND skip their turn |
| **Seven** | 7 | Split 7 steps between two of your own marbles (e.g., 3+4, 2+5) OR act as standard |
| **Five** | 5 | Move ANY marble on track 5 steps |
| **Four** | 4 | Move one of your marbles 4 steps BACKWARDS |
| **Burner** | 14 | Destroy an opponent's marble on track (not in Base/Safe/Home zones) → sends to Home |
| **Saver** | 15 | Send one of your marbles to a random empty Safe Zone cell |
| **Standard (2,3,6,8,9)** | 0 | Move your marble forward by card's rank number |

### Special Rules for Cards:
- **King bypasses:** Self-blocking, path blockage, and Safe Zone Entry blockage
- **Jack swap:** Only valid if both marbles on general track, neither in Base cell
- **Burner:** Cannot burn your own marble or marbles in Base/Safe/Home zones

---

## 🎯 Movement & Zone Rules

| Zone/Cell | Purpose | Key Rules |
|-----------|---------|------------|
| **Home Zone** | Starting area for marbles | Marbles inactive here; need Ace/King to field |
| **Base Cell** | Starting position on track | Ace/King places marble here. Own marble in Base blocks passage |
| **Safe Zone** | Goal area (immune zone) | Enter by exact count only. Cannot return to track. Immune to attacks |
| **Safe Zone Entry** | Cell before Safe Zone | Marble here blocks ALL players from entering Safe Zone |
| **Trap Cells** | 8 random positions | Landing here destroys marble → returns to Home Zone. Trap relocates after each destruction |
| **Track** | 100-cell main path | Normal, Base, or Entry cells. Move clockwise |

### Collision Rules
| Scenario | Result |
|----------|--------|
| Land on opponent's marble | Opponent's marble destroyed → returns to Home Zone |
| Land on your own marble | Invalid move (cannot self-block) |
| King move | ALL marbles in path (including target) destroyed |
| More than 1 marble blocking path | Movement invalid |

---


## 🛠️ Installation & Setup

### Prerequisites
- ☕ **Java JDK 8 or higher**
- 🐙 **Git** (optional)

### Quick Start

1. **Clone the repository**
   ```bash
   git clone https://github.com/FerialAbdulrahem/Jackaroo.git
   cd Jackaroo
   ```

2. **Navigate to game source**
   ```bash
   cd JackrooGame
   ```

3. **Compile Java files**
   ```bash
   javac *.java
   ```

4. **Run the game**
   ```bash
   java Main   # or the main class name
   ```

### Expected Data Files
The game likely reads:
- `Cards.csv` – Contains 102 cards with: Code, Frequency, Name, Description, Rank, Suit

---

## 🎮 How to Play

### Controls (Based on screenshot UI)
| Action | Method |
|--------|--------|
| Select card | Click on card from hand |
| Select marble | Click on your marble on board |
| Play card | Click PLAY button |
| Skip turn | Click SkipTurn button |
| View help | Click Rules&Help button |

### Turn Flow (Human Player)
1. **View your hand** (4 cards visible)
2. **Select a card** (e.g., King, Eight, Nine, or Three from screenshot)
3. **Select a marble** (if action requires it)
4. **Click PLAY** to execute action
5. **Card moves to Fire Pit**
6. **Next player (CPU) takes turn**

### CPU Behavior
- Randomly selects card from their hand (always 4 cards)
- Randomly selects marble if action requires it
- Executes card action automatically

### Winning Condition
**First player to move all 4 marbles from Home Zone → through Track → into Safe Zone wins.**

---

## 📁 Project Structure

```
Jackaroo/
├── JackrooGame/              # Main game source code (Java files)
├── .settings/                # Eclipse IDE settings
├── Game Description.pdf      # Official 10-page game rules document
├── Photo.png                 # Screenshot of game UI
├── Cards.csv                 # 102-card deck definition (referenced in PDF)
├── .classpath                # Eclipse build path
├── .project                  # Eclipse project file
└── README.md                 # Project documentation
```

---

## 🛠️ Tech Stack

| 🔧 Tool | 📌 Usage |
|---------|----------|
| ☕ Java | Core game logic, OOP, MVC pattern likely |
| 📄 CSV parsing | Reading 102-card deck |
| 🖥️ Eclipse IDE | Development environment |
| 🎨 Swing/JavaFX (likely) | GUI from screenshot (buttons, cards display) |



**🎉 Master the cards, outsmart the CPUs, and be the first to safe zone!**
