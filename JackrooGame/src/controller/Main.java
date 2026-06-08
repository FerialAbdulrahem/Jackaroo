package controller;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import engine.Game;
import engine.board.Cell;
import engine.board.SafeZone;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;

import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.Colour;
import model.card.Card;
import model.card.Deck;
import model.card.standard.Standard;
import model.card.standard.Suit;
import model.player.Marble;
import model.player.Player;

public class Main extends Application implements EventHandler<ActionEvent> {

    private AnchorPane an;
    private Scene playerNameScene;

    private TextField nameField;
    private Button startGame;

    // ─── Round tracking ───────────────────────────────────────────────────────────
   
    private static int lastHandSize   = 4;
    // ─── Skip tracking for Ten/Queen discard effect ───────────────────────────────
    private static final java.util.Set<Colour> skippedPlayers = new java.util.HashSet<>();
    // ─── Inline info panel reference ─────────────────────────────────────────────
    private static MainScene activeMs = null;

    // ─── showInfo: display non-error messages inline instead of popup ─────────────
    private static void showInfo(String title, String message) {
        if (activeMs != null && activeMs.miscLabel != null) {
            Platform.runLater(() ->
                activeMs.miscLabel.setText("ℹ " + title + "\n" + message)
            );
        }
    }

    // ─── start ────────────────────────────────────────────────────────────────────

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle("Jackaroo");
        an = new AnchorPane();
        playerNameScene = new Scene(an, 480, 340);
        primaryStage.setScene(playerNameScene);
        primaryStage.show();

        // Rich walnut-board background gradient matching the game aesthetic
        an.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #2C1A0A, #3D2510, #2C1A0A);"
        );

        // Decorative outer card/panel
        javafx.scene.shape.Rectangle panel = new javafx.scene.shape.Rectangle(40, 30, 400, 270);
        panel.setArcWidth(22); panel.setArcHeight(22);
        panel.setFill(javafx.scene.paint.Color.web("#1C0F05", 0.92));
        panel.setStroke(javafx.scene.paint.Color.web("#C4A97A")); panel.setStrokeWidth(2.5);
        javafx.scene.effect.DropShadow ps = new javafx.scene.effect.DropShadow();
        ps.setColor(javafx.scene.paint.Color.web("#000000", 0.7)); ps.setRadius(22); ps.setOffsetX(4); ps.setOffsetY(4);
        panel.setEffect(ps);
        an.getChildren().add(panel);

        // Suit decorations top corners
        Label suitTL = new Label("♠ ♥"); suitTL.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        suitTL.setTextFill(javafx.scene.paint.Color.web("#C4A97A", 0.7));
        suitTL.setLayoutX(52); suitTL.setLayoutY(38); an.getChildren().add(suitTL);
        Label suitTR = new Label("♦ ♣"); suitTR.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        suitTR.setTextFill(javafx.scene.paint.Color.web("#C4A97A", 0.7));
        suitTR.setLayoutX(370); suitTR.setLayoutY(38); an.getChildren().add(suitTR);

        // Title
        Label welcomeLabel = new Label("JACKAROO");
        welcomeLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 32));
        welcomeLabel.setTextFill(javafx.scene.paint.Color.web("#E8C97A"));
        welcomeLabel.setAlignment(Pos.CENTER);
        javafx.scene.effect.DropShadow titleGlow = new javafx.scene.effect.DropShadow();
        titleGlow.setColor(javafx.scene.paint.Color.web("#C4A97A", 0.8)); titleGlow.setRadius(12);
        welcomeLabel.setEffect(titleGlow);
        addControl(welcomeLabel, 40, 46, 400, 46);

        // Divider
        javafx.scene.shape.Rectangle divider = new javafx.scene.shape.Rectangle(100, 108, 280, 2);
        divider.setFill(javafx.scene.paint.Color.web("#C4A97A", 0.45)); an.getChildren().add(divider);

        // Subtitle
        Label subLabel = new Label("Enter Your Name to Begin");
        subLabel.setFont(Font.font("Georgia", FontWeight.NORMAL, 13));
        subLabel.setTextFill(javafx.scene.paint.Color.web("#C4A97A", 0.75));
        subLabel.setAlignment(Pos.CENTER);
        addControl(subLabel, 40, 118, 400, 24);

        // Name field with themed styling
        nameField = new TextField();
        nameField.setPromptText("Your name...");
        nameField.setStyle(
            "-fx-background-color: #2C1A0A;" +
            "-fx-text-fill: #F5E6C8;" +
            "-fx-prompt-text-fill: derive(#C4A97A, -40%);" +
            "-fx-border-color: #C4A97A;" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-font-size: 16px;" +
            "-fx-font-family: 'Georgia';" +
            "-fx-padding: 8 14;"
        );
        addControl(nameField, 80, 155, 320, 42);

        // Start button — rich green matching the play button
        startGame = new Button("▶  Start Game");
        startGame.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #4CAF50, #2E7D32);" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-family: 'Georgia';" +
            "-fx-font-size: 16px;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: #A5D6A7;" +
            "-fx-border-radius: 10;" +
            "-fx-border-width: 1.5;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 8, 0, 2, 3);"
        );
        startGame.setOnMouseEntered(e2 -> startGame.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #66BB6A, #388E3C);" +
            "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-family: 'Georgia'; -fx-font-size: 16px;" +
            "-fx-background-radius: 10; -fx-border-color: #C8E6C9; -fx-border-radius: 10; -fx-border-width: 1.5;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 10, 0, 2, 4);"
        ));
        startGame.setOnMouseExited(e2 -> startGame.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #4CAF50, #2E7D32);" +
            "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-family: 'Georgia'; -fx-font-size: 16px;" +
            "-fx-background-radius: 10; -fx-border-color: #A5D6A7; -fx-border-radius: 10; -fx-border-width: 1.5;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 8, 0, 2, 3);"
        ));
        startGame.setOnAction(this);
        addControl(startGame, 80, 215, 320, 46);

        // Bottom suit decoration
        Label suitBot = new Label("♣  ♦  ♥  ♠");
        suitBot.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        suitBot.setTextFill(javafx.scene.paint.Color.web("#C4A97A", 0.5));
        suitBot.setAlignment(Pos.CENTER);
        addControl(suitBot, 40, 278, 400, 20);

        nameField.setOnAction(e -> startGame.fire());

        startGame.setOnMouseClicked(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                String playerName = nameField.getText().trim();
                if (playerName.isEmpty()) {
                    displayAlert2("Error", "Please enter your name to start the game.");
                    return;
                }

                Game game;
                try {
                    game = new Game(playerName);
                } catch (IOException e1) {
                    displayAlert2("Error", "Failed to load cards: " + e1.getMessage());
                    return;
                }

                MainScene ms = new MainScene();
                activeMs = ms;
                Scene scene2 = ms.createGameScene();

                int[] uiToEngine = new int[4];
                for (int uiP = 0; uiP < 4; uiP++) {
                    Colour uiColour = game.getPlayers().get(uiP).getColour();
                    for (int eP = 0; eP < 4; eP++) {
                        if (game.getBoard().getSafeZones().get(eP).getColour() == uiColour) {
                            uiToEngine[uiP] = eP;
                            break;
                        }
                    }
                }
                for (int uiP = 0; uiP < 4; uiP++) {
                    for (int c = 0; c < 4; c++) {
                        Cell engineCell = game.getBoard().getSafeZones().get(uiToEngine[uiP]).getCells().get(c);
                        ms.c.safeCellPairs.get(uiP * 4 + c).setCell(engineCell);
                    }
                }

                // Link track cells
                for (int i = 0; i < ms.c.circleCellPairs.size(); i++) {
                    ms.c.circleCellPairs.get(i).setCell(game.getBoard().getTrack().get(i));
                }
                // Resolve FX colours per player order
                Color[] fxColors = new Color[4];
                for (int i = 0; i < 4; i++) {
                    fxColors[i] = colourToFX(game.getPlayers().get(i).getColour());
                }
                Color humanFX = fxColors[0];

                // Paint home zone circles with player colours
                for (int p = 0; p < 4; p++) {
                    for (int m = 0; m < 4; m++) {
                        ms.c.homeCellPairs.get(p * 4 + m).getCircle().setFill(fxColors[p]);
                    }
                }

                // Initialize safe zone circles with player color (faded) — match by colour order
                for (int uiP = 0; uiP < 4; uiP++) {
                    Color playerFaded = fxColors[uiP].deriveColor(0, 0.5, 1.5, 0.35);
                    for (int c = 0; c < 4; c++) {
                        javafx.scene.shape.Circle circle = ms.c.safeCellPairs.get(uiP * 4 + c).getCircle();
                        circle.setFill(playerFaded);
                        circle.setStroke(Color.BLACK);
                        circle.setStrokeWidth(1.0);
                    }
                }

                // Colour markers
                ms.miscCircles[2].setFill(humanFX);
                ms.miscCircles[3].setFill(fxColors[1]);
                ms.miscCircles[0].setFill(fxColors[2]);
                ms.miscCircles[1].setFill(fxColors[3]);

                // Player name labels
                ms.humanLabel.setText(playerName);
                ms.cpu1Label.setText(game.getPlayers().get(1).getName());
                ms.cpu2Label.setText(game.getPlayers().get(2).getName());
                ms.cpu3Label.setText(game.getPlayers().get(3).getName());

                refreshCardPanels(ms, game.getPlayers().get(0));
                updateGameUI(ms, game, fxColors);

                ms.c.setPlayerColor(humanFX);
                ms.c.makeMarbleSelectable();

                syncBoardFromEngine(ms, game, fxColors);
                setupCardHandlers(ms, game, fxColors, humanFX);

                ms.playButton.setOnAction(e -> handleHumanTurn(ms, game, fxColors, humanFX));

                // Skip button: discard a card from the human's hand and advance the turn
                ms.skipButton.setOnAction(e -> handleSkipTurn(ms, game, fxColors));

                // Initial CPU card row sync
                syncCpuCardRows(ms, game);

                // Keyboard shortcuts (1-4 to field marbles)
                scene2.setOnKeyPressed(e -> {
                    int key = -1;
                    switch (e.getCode()) {
                        case DIGIT1: case NUMPAD1: key = 0; break;
                        case DIGIT2: case NUMPAD2: key = 1; break;
                        case DIGIT3: case NUMPAD3: key = 2; break;
                        case DIGIT4: case NUMPAD4: key = 3; break;
                        case ESCAPE:
                            ms.c.clearMarbleSelections();
                            ms.actionPanel.setVisible(false);
                            deselectAllCards(ms);
                            break;
                        default: break;
                    }
                    if (key >= 0) {
                        CircleGrid.CircleCellPair pair = ms.c.homeCellPairs.get(key);
                        if (pair.getCircle().getFill().equals(humanFX)) {
                            ms.c.clearMarbleSelections();
                            pair.getCircle().setStroke(Color.WHITE);
                            pair.getCircle().setStrokeWidth(3);
                            ms.c.getSelectedMarbles().add(pair);
                        }
                    }
                });

                primaryStage.setTitle("Jackaroo");
                primaryStage.setScene(scene2);
                primaryStage.setMaximized(true);
                primaryStage.setMinWidth(800);
                primaryStage.setMinHeight(600);

                primaryStage.setOnShown(we -> {
                    double w = primaryStage.getWidth(), h = primaryStage.getHeight();
                    ms.screenWidth.set(w); ms.screenHeight.set(h);
                    ms.updateLayout(); ms.updateAllComponents();
                    ms.c.updateDimensions(w, h);
                });
                primaryStage.widthProperty().addListener((ob, ov, nv) -> {
                    ms.screenWidth.set(nv.doubleValue());
                    ms.updateLayout(); ms.updateAllComponents();
                });
                primaryStage.heightProperty().addListener((ob, ov, nv) -> {
                    ms.screenHeight.set(nv.doubleValue());
                    ms.updateLayout(); ms.updateAllComponents();
                });
                primaryStage.show();
            }
        });
    }

    // ─── SKIP TURN ────────────────────────────────────────────────────────────────

    private static void handleSkipTurn(MainScene ms, Game game, Color[] fxColors) {
        Player human = game.getPlayers().get(0);
        if (human.getHand().isEmpty()) {
            showInfo("Skip", "No cards to discard — wait for next deal.");
            return;
        }
        // Discard the first card (or selected card if one is chosen)
        int cardIdx = getSelectedCardIndex(ms);
        Card cardToDiscard = cardIdx >= 0 && cardIdx < human.getHand().size()
                ? human.getHand().get(cardIdx)
                : human.getHand().get(0);
        int[] handsBefore = new int[4];
        for (int pi = 0; pi < 4; pi++) {
            handsBefore[pi] = game.getPlayers().get(pi).getHand().size();
        }
        try {
            game.selectCard(cardToDiscard);
            game.endPlayerTurn();   // ends without playing — card is removed from hand
        } catch (Exception ex) {
            try { game.deselectAll(); } catch (Exception ignored) {}
            // Fallback: just end turn
            try { game.endPlayerTurn(); } catch (Exception ignored) {}
        }
        String discardedName = cardToDiscard.getName();
        if (discardedName.toUpperCase().contains("TEN") || discardedName.toUpperCase().contains("QUEEN")) {
            for (int pi = 1; pi < 4; pi++) {
                if (game.getPlayers().get(pi).getHand().size() < handsBefore[pi]) {
                    skippedPlayers.add(game.getPlayers().get(pi).getColour());
                    break;
                }
            }
        }
        syncBoardFromEngine(ms, game, fxColors);
        deselectAllCards(ms);
        ms.c.clearMarbleSelections();
        ms.actionPanel.setVisible(false);
        updateFirePit(ms, game, discardedName);
        refreshCardPanels(ms, human);
        updateGameUI(ms, game, fxColors);
        syncCpuCardRows(ms, game);
        ms.c.makeMarbleSelectable();
        showInfo("Turn Skipped", "You discarded " + discardedName + " and skipped your turn.");
        handleCPUTurns(ms, game, fxColors);
    }

    /** Syncs the face-down CPU card rows with actual hand sizes. */
    private static void syncCpuCardRows(MainScene ms, Game game) {
        if (ms.cpu1CardRow != null) ms.updateCpuCardRow(ms.cpu1CardRow, game.getPlayers().get(1).getHand().size());
        if (ms.cpu2CardRow != null) ms.updateCpuCardRow(ms.cpu2CardRow, game.getPlayers().get(2).getHand().size());
        if (ms.cpu3CardRow != null) ms.updateCpuCardRow(ms.cpu3CardRow, game.getPlayers().get(3).getHand().size());
    }

    // ─── HUMAN TURN ───────────────────────────────────────────────────────────────

    private static void handleHumanTurn(MainScene ms, Game game, Color[] fxColors, Color humanFX) {
        Colour humanColour = game.getPlayers().get(0).getColour();
        if (skippedPlayers.contains(humanColour)) {
            skippedPlayers.remove(humanColour);
            // Ten/Queen already removed a card from our hand.
            // endPlayerTurn() with null selectedCard: hand.remove(null) → no-op,
            // then currentPlayerIndex advances past us. ✓
            try { game.endPlayerTurn(); } catch (Exception ignored) {}
            syncBoardFromEngine(ms, game, fxColors);
            updateGameUI(ms, game, fxColors);
            refreshCardPanels(ms, game.getPlayers().get(0));
            syncCpuCardRows(ms, game);
            ms.c.makeMarbleSelectable();
            showInfo("Turn Skipped", "Your turn was skipped by Ten/Queen!");
            handleCPUTurns(ms, game, fxColors);   // ← was missing: CPUs must still play
            return;
        }

        int cardIdx = getSelectedCardIndex(ms);
        if (cardIdx == -1) {
            showInfo("No Card", "Please select a card to play.");
            return;
        }

        Player human = game.getPlayers().get(0);
        Card card = human.getHand().get(cardIdx);
        String cardName = card.getName().toUpperCase();

        ArrayList<CircleGrid.CircleCellPair> uiSelected = ms.c.getSelectedMarbles();
        ArrayList<Marble> engineMarbles = resolveEngineMarbles(ms, game, uiSelected);

        try {
            game.selectCard(card);

            if (cardName.contains("SEVEN") && engineMarbles.size() == 2) {
                game.editSplitDistance(ms.getSplitDistance());
            }

            for (Marble m : engineMarbles) {
                game.selectMarble(m);
            }

            int[] handsBefore = new int[4];
            for (int pi = 0; pi < 4; pi++) handsBefore[pi] = game.getPlayers().get(pi).getHand().size();

            game.playPlayerTurn();
            String playedName = card.getName();
            game.endPlayerTurn();

            // Detect which opponent lost a card due to Ten/Queen
            if (cardName.contains("TEN") || cardName.contains("QUEEN")) {
                for (int pi = 1; pi < 4; pi++) {
                    if (game.getPlayers().get(pi).getHand().size() < handsBefore[pi]) {
                        skippedPlayers.add(game.getPlayers().get(pi).getColour());
                        break;
                    }
                }
            }

            syncBoardFromEngine(ms, game, fxColors);
            deselectAllCards(ms);
            ms.c.clearMarbleSelections();
            ms.actionPanel.setVisible(false);
            updateFirePit(ms, game, playedName);
            refreshCardPanels(ms, human);
            updateGameUI(ms, game, fxColors);
            syncCpuCardRows(ms, game);
            ms.c.makeMarbleSelectable();
            showInfo("Played", playedName);

            Colour winner = game.checkWin();
            if (winner != null) {
                displayAlert2("Game Over!", winner + " wins the game!");
                return;
            }

            handleCPUTurns(ms, game, fxColors);

        } catch (Exception ex) {
            // Fully reset engine + UI so the game is never stuck
            try { game.deselectAll(); } catch (Exception ignored2) {}
            // Reset card selection visuals
            deselectAllCards(ms);
            ms.c.clearMarbleSelections();
            ms.actionPanel.setVisible(false);
            // Re-render hand in case indices shifted (e.g. card was removed before exception)
            refreshCardPanels(ms, game.getPlayers().get(0));
            syncBoardFromEngine(ms, game, fxColors);
            updateGameUI(ms, game, fxColors);
            syncCpuCardRows(ms, game);
            ms.c.makeMarbleSelectable();
            String errMsg = ex.getMessage();
            displayAlert2("Invalid Move", errMsg != null && !errMsg.isEmpty() ? errMsg : ex.getClass().getSimpleName());
        }
    }

    // ─── CPU TURNS ────────────────────────────────────────────────────────────────

    private static void handleCPUTurns(MainScene ms, Game game, Color[] fxColors) {
        Thread t = new Thread(() -> {
            for (int i = 1; i <= 3; i++) {
                try { Thread.sleep(1200); } catch (InterruptedException ex) { ex.printStackTrace(); }

                final int pi = i;
                Platform.runLater(() -> {
                    Player cpu = game.getPlayers().get(pi);

                    // ── Ten/Queen skip: the card was ALREADY removed by the engine.
                    // We only need to advance past this CPU's turn without touching their hand.
                    if (skippedPlayers.contains(cpu.getColour())) {
                        skippedPlayers.remove(cpu.getColour());
                        // End turn without selecting any card — engine will advance the turn pointer
                        try {
                            game.endPlayerTurn();
                        } catch (Exception ignored) {}
                        syncBoardFromEngine(ms, game, fxColors);
                        updateGameUI(ms, game, fxColors);
                        syncCpuCardRows(ms, game);
                        showInfo("Turn Skipped", cpu.getName() + "'s turn was skipped (Ten/Queen)!");
                        return;
                    }

                    String playedName = "";
                    try {
                        // Force field if CPU has marbles at home and an Ace/King
                        if (!cpu.getMarbles().isEmpty()) {
                            Card fieldCard = null;
                            for (Card c : cpu.getHand()) {
                                String n = c.getName().toUpperCase();
                                if (n.contains("ACE") || n.contains("KING")) { fieldCard = c; break; }
                            }
                            if (fieldCard != null) {
                                try {
                                    game.selectCard(fieldCard);
                                    game.playPlayerTurn();
                                    playedName = fieldCard.getName();
                                    game.endPlayerTurn();
                                    syncBoardFromEngine(ms, game, fxColors);
                                    updateFirePit(ms, game, playedName);
                                    updateGameUI(ms, game, fxColors);
                                    showInfo(cpu.getName() + " played", playedName);
                                    return;
                                } catch (Exception ex) {
                                    game.deselectAll();
                                }
                            }
                        }

                        // Normal CPU play — snapshot hands to detect Ten/Queen skip
                        int[] handsBeforeCPU = new int[4];
                        for (int hp = 0; hp < 4; hp++) handsBeforeCPU[hp] = game.getPlayers().get(hp).getHand().size();

                        game.playPlayerTurn();
                        Card played = cpu.getSelectedCard();
                        if (played != null) {
                            playedName = played.getName();
                        }
                        game.endPlayerTurn();

                        // Register skip targets: find which opponent had a card removed
                        if (played != null) {
                            String pn = playedName.toUpperCase();
                            if (pn.contains("TEN") || pn.contains("QUEEN")) {
                                for (int hp = 0; hp < 4; hp++) {
                                    if (hp != pi && game.getPlayers().get(hp).getHand().size() < handsBeforeCPU[hp]) {
                                        skippedPlayers.add(game.getPlayers().get(hp).getColour());
                                        break;
                                    }
                                }
                            }
                        }

                    } catch (Exception ex) {
                        // CPU can't play — discard first card, no skip for next
                        try {
                            if (cpu.getSelectedCard() == null && !cpu.getHand().isEmpty()) {
                                try { game.selectCard(cpu.getHand().get(0)); } catch (Exception ignored) {}
                            }
                            game.endPlayerTurn();
                        } catch (Exception e2) { /* ignore */ }
                    }

                    syncBoardFromEngine(ms, game, fxColors);
                    syncCpuCardRows(ms, game);
                    if (!playedName.isEmpty()) {
                        updateFirePit(ms, game, playedName);
                        showInfo(cpu.getName() + " played", playedName);
                    }
                    updateGameUI(ms, game, fxColors);

                    Colour winner = game.checkWin();
                    if (winner != null) { displayAlert2("Game Over!", winner + " wins!"); return; }
                });

                try { Thread.sleep(400); } catch (InterruptedException ex) { ex.printStackTrace(); }
            }

            Platform.runLater(() -> {
                Player human = game.getPlayers().get(0);
                int currentHandSize = human.getHand().size();
                if (currentHandSize == 4 && lastHandSize < 4) {
                    // New round — cards were dealt. Show notification every round.
                    updateFirePit(ms, game, "🃏 New Cards");
                    showInfo("New Round!", "New cards dealt to all players!");
                }
                lastHandSize = currentHandSize;
                refreshCardPanels(ms, human);
                syncCpuCardRows(ms, game);
                ms.c.makeMarbleSelectable();

                if (processSkippedHumanTurn(ms, game, fxColors)) {
                    return;
                }

                showInfo("Your Turn", "CPUs have played. Select a card and marble!");
            });
        });
        t.setDaemon(true);
        t.start();
    }

    private static boolean processSkippedHumanTurn(MainScene ms, Game game, Color[] fxColors) {
        Colour humanColour = game.getPlayers().get(0).getColour();
        if (!skippedPlayers.contains(humanColour)) {
            return false;
        }

        skippedPlayers.remove(humanColour);
        try { game.endPlayerTurn(); } catch (Exception ignored) {}
        syncBoardFromEngine(ms, game, fxColors);
        updateGameUI(ms, game, fxColors);
        refreshCardPanels(ms, game.getPlayers().get(0));
        syncCpuCardRows(ms, game);
        ms.c.makeMarbleSelectable();
        showInfo("Turn Skipped", "Your turn was skipped by a discard effect.");
        handleCPUTurns(ms, game, fxColors);
        return true;
    }

    private static void syncBoardFromEngine(MainScene ms, Game game, Color[] fxColors) {
        // Load the wood texture image once
        Image woodTexture = new Image("file:woodd.jpg");
        ImagePattern emptyPattern = new ImagePattern(woodTexture);

        ArrayList<Cell> track = game.getBoard().getTrack();
        for (int i = 0; i < ms.c.circleCellPairs.size(); i++) {
            Cell cell = track.get(i);
            Marble marble = cell.getMarble();
            javafx.scene.shape.Circle circle = ms.c.circleCellPairs.get(i).getCircle();
            circle.setFill(marble != null ? colourToFX(marble.getColour()) : emptyPattern);
            circle.setStroke(Color.BLACK);
            circle.setStrokeWidth(1.0);
        }

        // Safe zones — match UI player index to engine safe zone by colour
        for (int uiP = 0; uiP < 4; uiP++) {
            Colour uiColour = game.getPlayers().get(uiP).getColour();
            SafeZone sz = null;
            for (SafeZone zone : game.getBoard().getSafeZones()) {
                if (zone.getColour() == uiColour) { sz = zone; break; }
            }
            if (sz == null) continue;
            Color playerFaded = fxColors[uiP].deriveColor(0, 0.5, 1.5, 0.35);
            for (int c = 0; c < 4; c++) {
                Marble marble = sz.getCells().get(c).getMarble();
                javafx.scene.shape.Circle circle = ms.c.safeCellPairs.get(uiP * 4 + c).getCircle();
                if (marble != null) {
                    circle.setFill(colourToFX(marble.getColour()));
                } else {
                    circle.setFill(playerFaded);
                }
                circle.setStroke(Color.BLACK);
                circle.setStrokeWidth(1.0);
            }
        }

        // Home zones
        for (int p = 0; p < 4; p++) {
            int inHome = game.getPlayers().get(p).getMarbles().size();
            for (int m = 0; m < 4; m++) {
                javafx.scene.shape.Circle circle = ms.c.homeCellPairs.get(p * 4 + m).getCircle();
                if (m < inHome) {
                    circle.setFill(fxColors[p]);
                } else {
                    circle.setFill(emptyPattern);
                }
            }
        }
    }

    // ─── Resolve engine Marble objects from UI circle selections ─────────────────

    private static ArrayList<Marble> resolveEngineMarbles(MainScene ms, Game game,
                                                           ArrayList<CircleGrid.CircleCellPair> uiSelected) {
        ArrayList<Marble> result = new ArrayList<>();
        for (CircleGrid.CircleCellPair pair : uiSelected) {
            if (ms.c.homeCellPairs.contains(pair)) continue;

            Cell cell = pair.getCell();
            if (cell != null && cell.getMarble() != null) {
                result.add(cell.getMarble());
                continue;
            }

            for (CircleGrid.CircleCellPair sp : ms.c.safeCellPairs) {
                if (sp == pair && sp.getCell() != null && sp.getCell().getMarble() != null) {
                    result.add(sp.getCell().getMarble());
                    break;
                }
            }
        }
        return result;
    }

    // ─── Card panel & action button setup ────────────────────────────────────────

    private static void setupCardHandlers(MainScene ms, Game game, Color[] fxColors, Color humanFX) {
        for (int i = 0; i < ms.cardPanels.length; i++) {
            final int idx = i;
            ms.cardPanels[i].setOnMouseClicked(e -> {
                if (!ms.cardPanels[idx].isVisible()) return;
                // Guard: index must still be valid in the current hand
                if (idx >= game.getPlayers().get(0).getHand().size()) {
                    // Hand shrank (e.g. card just played) — hide stale panel and reset
                    ms.hideCardPanel(idx);
                    deselectAllCards(ms);
                    ms.c.clearMarbleSelections();
                    ms.actionPanel.setVisible(false);
                    return;
                }
                boolean wasSelected = ms.checkBoxes[idx].isSelected();
                deselectAllCards(ms);
                ms.c.clearMarbleSelections();
                ms.actionPanel.setVisible(false);
                if (!wasSelected) {
                    ms.checkBoxes[idx].setSelected(true);
                    ms.highlightCard(idx, true);
                    onCardSelected(ms, game, idx, humanFX);
                }
            });
        }

        // Discard button (Ten / Queen / any card when no marbles are out)
        ms.discardButton.setOnAction(e -> {
            int idx = getSelectedCardIndex(ms);
            if (idx == -1) { displayAlert2("Error", "Select a card first."); return; }
            Player human = game.getPlayers().get(0);
            Card card = human.getHand().get(idx);
            String name = card.getName().toUpperCase();
            long onTrack = ms.c.getCircleCellPairs().stream()
                    .filter(p -> p.getCircle().getFill().equals(humanFX)).count();
            // Also count marbles the human has in their safe zone
            Colour humanColourDiscard = game.getPlayers().get(0).getColour();
            long inSafeZone = game.getBoard().getSafeZones().stream()
                    .filter(sz -> sz.getColour() == humanColourDiscard)
                    .flatMap(sz -> sz.getCells().stream())
                    .filter(cell -> cell.getMarble() != null)
                    .count();
            boolean noMarblesOut = onTrack == 0 && inSafeZone == 0 && human.getMarbles().size() == 4;
            try {
                game.selectCard(card);
                if (noMarblesOut && !name.contains("TEN") && !name.contains("QUEEN")) {
                    // Non-playable discard: just end the turn (no skip added per rules)
                    game.endPlayerTurn();
                } else {
                    // Track hands before for Ten/Queen victim detection (exclude human player 0)
                    int[] handsBefore = new int[4];
                    for (int pi = 0; pi < 4; pi++) handsBefore[pi] = game.getPlayers().get(pi).getHand().size();

                    game.playPlayerTurn();
                    game.endPlayerTurn();

                    // Detect which opponent lost a card (Ten/Queen discards from opponent hand)
                    if (name.contains("TEN") || name.contains("QUEEN")) {
                        for (int pi = 1; pi < 4; pi++) {
                            if (game.getPlayers().get(pi).getHand().size() < handsBefore[pi]) {
                                skippedPlayers.add(game.getPlayers().get(pi).getColour());
                                break;
                            }
                        }
                    }
                }
                String playedName = card.getName();
                syncBoardFromEngine(ms, game, fxColors);
                deselectAllCards(ms);
                ms.actionPanel.setVisible(false);
                updateFirePit(ms, game, playedName);
                refreshCardPanels(ms, human);
                updateGameUI(ms, game, fxColors);
                syncCpuCardRows(ms, game);
                ms.c.makeMarbleSelectable();
                showInfo("Discarded", playedName);
                handleCPUTurns(ms, game, fxColors);
            } catch (Exception ex) {
                try { game.deselectAll(); } catch (Exception ignored2) {}
                deselectAllCards(ms);
                ms.c.clearMarbleSelections();
                ms.actionPanel.setVisible(false);
                refreshCardPanels(ms, human);
                syncBoardFromEngine(ms, game, fxColors);
                updateGameUI(ms, game, fxColors);
                syncCpuCardRows(ms, game);
                ms.c.makeMarbleSelectable();
                String errMsg = ex.getMessage();
                displayAlert2("Cannot Discard", errMsg != null && !errMsg.isEmpty() ? errMsg : ex.getClass().getSimpleName());
            }
        });

        // Split button (Seven with 2 marbles selected)
        ms.splitButton.setOnAction(e -> {
            ArrayList<CircleGrid.CircleCellPair> sel = ms.c.getSelectedMarbles();
            if (sel.size() != 2) {
                displayAlert2("Split Error", "Select exactly 2 of your marbles on the track, then press Apply Split.");
                return;
            }
            int idx = getSelectedCardIndex(ms);
            if (idx == -1) {
                displayAlert2("Error", "Select the Seven card first, then select 2 marbles.");
                return;
            }

            Player human = game.getPlayers().get(0);
            if (idx >= human.getHand().size()) {
                displayAlert2("Error", "Card index out of range — please reselect the Seven card.");
                return;
            }
            Card card = human.getHand().get(idx);
            ArrayList<Marble> engineMarbles = resolveEngineMarbles(ms, game, sel);

            if (engineMarbles.size() < 2) {
                displayAlert2("Split Error", "Both selected marbles must be on the main track (not home or safe zone).");
                return;
            }

            // splitDist1 = steps for marble-1; 7 - splitDist1 = steps for marble-2
            int dist1 = ms.getSplitDistance();          // 1–6
            int dist2 = 7 - dist1;                      // 6–1
            if (dist1 < 1 || dist1 > 6 || dist2 < 1) {
                displayAlert2("Split Error", "Split distances must each be between 1 and 6 (total = 7).");
                return;
            }

            try {
                game.selectCard(card);
                // Engine expects the split distance for marble 1 — marble 2 gets 7 - dist1 automatically
                game.editSplitDistance(dist1);
                game.selectMarble(engineMarbles.get(0));
                game.selectMarble(engineMarbles.get(1));
                game.playPlayerTurn();
                String playedName = card.getName();
                game.endPlayerTurn();

                syncBoardFromEngine(ms, game, fxColors);
                deselectAllCards(ms);
                ms.c.clearMarbleSelections();
                ms.actionPanel.setVisible(false);
                updateFirePit(ms, game, playedName);
                refreshCardPanels(ms, human);
                updateGameUI(ms, game, fxColors);
                syncCpuCardRows(ms, game);
                ms.c.makeMarbleSelectable();
                showInfo("Played", playedName + " (split " + dist1 + "+" + dist2 + ")");

                Colour winner = game.checkWin();
                if (winner != null) {
                    displayAlert2("Game Over!", winner + " wins the game!");
                    return;
                }
                handleCPUTurns(ms, game, fxColors);
            } catch (Exception ex) {
                try { game.deselectAll(); } catch (Exception ignored2) {}
                deselectAllCards(ms);
                ms.c.clearMarbleSelections();
                ms.actionPanel.setVisible(false);
                refreshCardPanels(ms, human);
                syncBoardFromEngine(ms, game, fxColors);
                updateGameUI(ms, game, fxColors);
                syncCpuCardRows(ms, game);
                ms.c.makeMarbleSelectable();
                String errMsg = ex.getMessage();
                displayAlert2("Invalid Split", errMsg != null && !errMsg.isEmpty() ? errMsg : ex.getClass().getSimpleName());
            }
        });
    }

    private static void onCardSelected(MainScene ms, Game game, int cardIdx, Color humanFX) {
        if (cardIdx >= game.getPlayers().get(0).getHand().size()) return;
        Card card = game.getPlayers().get(0).getHand().get(cardIdx);
        String name = card.getName().toUpperCase();
        long onTrack = ms.c.getCircleCellPairs().stream()
                .filter(p -> p.getCircle().getFill().equals(humanFX)).count();
        // Also count marbles the human has in their safe zone
        Colour humanColourCard = game.getPlayers().get(0).getColour();
        long inSafeZoneCard = game.getBoard().getSafeZones().stream()
                .filter(sz -> sz.getColour() == humanColourCard)
                .flatMap(sz -> sz.getCells().stream())
                .filter(cell -> cell.getMarble() != null)
                .count();
        boolean noMarblesOut = onTrack == 0 && inSafeZoneCard == 0 && game.getPlayers().get(0).getMarbles().size() == 4;

        // noMarblesOut: all 4 marbles still in home, none on track or safe zone.
        // Only force-discard if the card truly can't do anything (not Ace/King/TEN/QUEEN/JACK/BURNER/SAVER/FIVE/SEVEN).
        // For all other cases let the engine validate — don't pre-empt.

        if (noMarblesOut && !name.contains("ACE") && !name.contains("KING")
                && !name.contains("TEN") && !name.contains("QUEEN")
                && !name.contains("JACK") && !name.contains("SEVEN")
                && !name.contains("BURNER") && !name.contains("SAVER")
                && !name.contains("FIVE")) {
            ms.c.makeMarbleSelectable();
            ms.showActionPanel("DISCARD_ANY", false);
            showInfo(card.getName(), "No marbles on track — card will be discarded.");
        } else if (name.contains("JACK")) {
            ms.c.makeAllMarblesSelectable();
            ms.showActionPanel("", false);
            showInfo("Jack", "Select your marble then an opponent's marble to swap.");
        } else if (name.contains("SEVEN")) {
            long sevenOnTrack = ms.c.getCircleCellPairs().stream()
                    .filter(p -> p.getCircle().getFill().equals(humanFX)).count();
            if (sevenOnTrack == 0) {
                // No marbles on track — can't move, must discard
                ms.c.makeMarbleSelectable();
                ms.showActionPanel("DISCARD_ANY", false);
                showInfo("Seven", "No marbles on track — card will be discarded.");
            } else {
                ms.c.makeMarbleSelectable();
                boolean canSplit = sevenOnTrack >= 2;
                // Reset the split picker to 3+4 each time Seven is selected
                ms.splitDist1 = 3;
                ms.adjustSplit(1, 0);
                ms.showActionPanel("SEVEN", canSplit);
                // Auto-select the single marble if only one is on track
                if (sevenOnTrack == 1) {
                    for (CircleGrid.CircleCellPair p : ms.c.getCircleCellPairs()) {
                        if (p.getCircle().getFill().equals(humanFX)) {
                            ms.c.getSelectedMarbles().add(p);
                            p.getCircle().setStroke(Color.WHITE);
                            p.getCircle().setStrokeWidth(3);
                            break;
                        }
                    }
                    showInfo("Seven", "Move 7 steps — marble auto-selected. Press Play.");
                } else {
                    showInfo("Seven", canSplit
                            ? "Select 1 marble + Play (7 steps).  OR  Select 2 marbles + set split + Apply Split."
                            : "Select your marble on track and press Play to move it 7 steps.");
                }
            }
        } else if (name.contains("BURNER")) {
            // Burner: select only opponent marbles on the main track (not base, not safe, not home)
            ms.c.makeOpponentTrackMarblesSelectable();
            ms.showActionPanel("", false);
            showInfo("MarbleBurner", "Select an opponent's marble on track to send home.");
        } else if (name.contains("SAVER")) {
            // Saver: select only the player's own marbles on track (including base)
            ms.c.makeMarbleSelectable();
            ms.showActionPanel("", false);
            showInfo("MarbleSaver", "Select one of your marbles on track to send to safe zone.");
        } else if (name.contains("TEN")) {
            ms.c.makeMarbleSelectable();
            ms.showActionPanel("TEN", false);
            showInfo("Ten", "Move a marble 10 steps, OR use Discard to skip the NEXT player's turn.");
        } else if (name.contains("QUEEN")) {
            ms.c.makeMarbleSelectable();
            ms.showActionPanel("QUEEN", false);
            showInfo("Queen", "Move a marble 12 steps, OR use Discard to skip a RANDOM player's turn.");
        } else if (name.contains("FIVE")) {
            ms.c.makeTrackMarblesSelectable();
            ms.showActionPanel("", false);
            showInfo("Five", "Select any marble on track (yours or opponent's) to move 5 steps.");
        } else {
            ms.c.makeMarbleSelectable();
            ms.showActionPanel("", false);
            showInfo(card.getName(), card.getDescription());
    
            long totalPlayerMarbles = ms.c.getCircleCellPairs().stream()
                    .filter(p -> p.getCircle().getFill().equals(humanFX)).count()
                + ms.c.getSafeCellPairs().stream()
                    .filter(p -> p.getCircle().getFill().equals(humanFX)).count();
            if (totalPlayerMarbles == 1) {
                for (CircleGrid.CircleCellPair p : ms.c.getCircleCellPairs()) {
                    if (p.getCircle().getFill().equals(humanFX)) {
                        ms.c.getSelectedMarbles().add(p);
                        p.getCircle().setStroke(Color.WHITE);
                        p.getCircle().setStrokeWidth(3);
                        break;
                    }
                }
                if (ms.c.getSelectedMarbles().isEmpty()) {
                    for (CircleGrid.CircleCellPair p : ms.c.getSafeCellPairs()) {
                        if (p.getCircle().getFill().equals(humanFX)) {
                            ms.c.getSelectedMarbles().add(p);
                            p.getCircle().setStroke(Color.WHITE);
                            p.getCircle().setStrokeWidth(3);
                            break;
                        }
                    }
                }
            }
        }
    }

    // ─── Card visual helpers ──────────────────────────────────────────────────────

    public static void refreshCardPanels(MainScene ms, Player human) {
        for (int i = 0; i < ms.cardPanels.length; i++) ms.hideCardPanel(i);
        for (int i = 0; i < Math.min(human.getHand().size(), 4); i++) {
            Card card = human.getHand().get(i);
            String rankStr, suitSymbol, desc = card.getDescription();
            Color suitColor;
            if (card instanceof Standard) {
                Standard sc = (Standard) card;
                rankStr    = rankToLabel(sc.getRank());
                suitSymbol = suitToSymbol(sc.getSuit());
                suitColor  = isRedSuit(sc.getSuit()) ? Color.CRIMSON : Color.BLACK;
            } else {
                rankStr = "W"; suitSymbol = "★"; suitColor = Color.DARKORANGE;
            }
            ms.updateCardPanel(i, rankStr, suitSymbol, suitColor, desc, card.getName());
        }
    }

    private static String rankToLabel(int r) {
        switch (r) { case 1: return "A"; case 11: return "J"; case 12: return "Q"; case 13: return "K"; default: return String.valueOf(r); }
    }
    private static String suitToSymbol(Suit s) {
        switch (s) { case HEART: return "♥"; case DIAMOND: return "♦"; case CLUB: return "♣"; case SPADE: return "♠"; default: return ""; }
    }
    private static boolean isRedSuit(Suit s) { return s == Suit.HEART || s == Suit.DIAMOND; }

    // ─── UI helpers ───────────────────────────────────────────────────────────────

    private static void updateGameUI(MainScene ms, Game game, Color[] fxColors) {
        ms.remainingCards1.setText(game.getPlayers().get(3).getName() + "\nCards: " + game.getPlayers().get(3).getHand().size());
        ms.remainingCards2.setText(game.getPlayers().get(1).getName() + "\nCards: " + game.getPlayers().get(1).getHand().size());
        ms.remainingCards3.setText(game.getPlayers().get(2).getName() + "\nCards: " + game.getPlayers().get(2).getHand().size());
        ms.deckRemainingLabel.setText("Rem: " + Deck.getPoolSize());
        ms.humanLabel.setText(game.getPlayers().get(0).getName());
        Colour active = game.getActivePlayerColour(), next = game.getNextPlayerColour();
        ms.currentPlayerLabel.setText("Current: " + active);
        ms.currentPlayerLabel.setTextFill(colourToFX(active));
        ms.nextPlayerLabel.setText("Next: " + next);
        ms.nextPlayerLabel.setTextFill(colourToFX(next));
    }

    private static int getSelectedCardIndex(MainScene ms) {
        for (int i = 0; i < ms.checkBoxes.length; i++) if (ms.checkBoxes[i].isSelected()) return i;
        return -1;
    }

    private static void deselectAllCards(MainScene ms) {
        for (int i = 0; i < ms.checkBoxes.length; i++) { ms.checkBoxes[i].setSelected(false); ms.highlightCard(i, false); }
    }

    private static void updateFirePit(MainScene ms, Game game, String cardName) {
        if (ms.firePitStack == null) return;
        java.util.List<model.card.Card> pit = game.getFirePit();
        if (pit.isEmpty() || cardName.isEmpty() || cardName.equals("🃏 New Cards")) {
            if (ms.firePitCardRank  != null) ms.firePitCardRank.setText("—");
            if (ms.firePitCardSuit  != null) ms.firePitCardSuit.setText("");
            if (ms.firePitCardName  != null) ms.firePitCardName.setText(cardName.isEmpty() ? "Empty" : cardName);
            return;
        }
        model.card.Card last = pit.get(pit.size() - 1);
        if (last == null) return;
        String rank = ""; String suit = ""; javafx.scene.paint.Color col = javafx.scene.paint.Color.web("#1a1a1a");
        if (last instanceof model.card.standard.Standard) {
            model.card.standard.Standard sc = (model.card.standard.Standard) last;
            int r = sc.getRank();
            switch(r) { case 1: rank="A"; break; case 11: rank="J"; break; case 12: rank="Q"; break; case 13: rank="K"; break; default: rank=String.valueOf(r); }
            switch(sc.getSuit()) {
                case HEART:   suit="♥"; col=javafx.scene.paint.Color.CRIMSON; break;
                case DIAMOND: suit="♦"; col=javafx.scene.paint.Color.CRIMSON; break;
                case CLUB:    suit="♣"; break;
                case SPADE:   suit="♠"; break;
            }
        } else {
            rank = "★"; suit = "Wild"; col = javafx.scene.paint.Color.DARKORANGE;
        }
        if (ms.firePitCardRank != null) { ms.firePitCardRank.setText(rank); ms.firePitCardRank.setTextFill(col); }
        if (ms.firePitCardSuit != null) { ms.firePitCardSuit.setText(suit); ms.firePitCardSuit.setTextFill(col); }
        if (ms.firePitCardName != null) ms.firePitCardName.setText(last.getName());
    }

    static Color colourToFX(Colour c) {
        switch (c) {
            case BLUE:   return Color.web("#1565C0");   // marbleblue
            case GREEN:  return Color.web("#2E7D32");   // marblegreen
            case YELLOW: return Color.web("#F9A825");   // marbleyellow
            case RED:    return Color.web("#C62828");   // marblered
            default:     return Color.BLACK;
        }
    }

    // ─── Start screen helpers ─────────────────────────────────────────────────────

    @Override
    public void handle(ActionEvent e) {
        if (e.getSource() == startGame && nameField.getText().isEmpty())
            try { displayAlert1("Error", "Enter your name to start the game."); }
            catch (FileNotFoundException e1) { displayAlert2("Error", "Enter your name to start the game."); }
    }

    public void addControl(Region c, int x, int y, int w, int h) {
        c.setLayoutX(x); c.setLayoutY(y); c.setPrefWidth(w); c.setPrefHeight(h);
        an.getChildren().add(c);
    }

    public void displayAlert1(String title, String message) throws FileNotFoundException {
        Stage window = new Stage();
        window.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        window.setResizable(false);
        VBox layout = buildAlertLayout();
        Label t = new Label(title);
        t.setFont(Font.font("Georgia", FontWeight.BOLD, 20));
        t.setTextFill(Color.web("#E8C97A"));
        Label m = new Label(message);
        m.setFont(Font.font("Georgia", 14));
        m.setTextFill(Color.web("#F5E6C8"));
        m.setWrapText(true); m.setMaxWidth(380);
        m.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        Button ok = new Button("OK");
        styleAlertButton(ok);
        ok.setOnAction(ev -> window.close());
        layout.getChildren().addAll(t, m, ok);
        window.setWidth(460); window.setHeight(220);
        window.setScene(new Scene(layout)); window.showAndWait();
    }

    public static void displayAlert2(String title, String message) {
        Stage window = new Stage();
        window.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        window.setResizable(false);
        VBox layout = buildAlertLayout();
        Label t = new Label(title);
        t.setFont(Font.font("Georgia", FontWeight.BOLD, 20));
        t.setTextFill(Color.web("#E8C97A"));
        Label m = new Label(message);
        m.setFont(Font.font("Georgia", 14));
        m.setTextFill(Color.web("#F5E6C8"));
        m.setWrapText(true); m.setMaxWidth(380);
        m.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        Button ok = new Button("OK");
        styleAlertButton(ok);
        ok.setOnAction(ev -> window.close());
        layout.getChildren().addAll(t, m, ok);
        window.setWidth(460); window.setHeight(220);
        window.setScene(new Scene(layout)); window.showAndWait();
    }

    private static void styleAlertButton(Button btn) {
        btn.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #4CAF50, #2E7D32);" +
            "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-family: 'Georgia'; -fx-font-size: 14px;" +
            "-fx-background-radius: 8; -fx-border-color: #A5D6A7; -fx-border-radius: 8; -fx-border-width: 1.5;" +
            "-fx-padding: 7 28; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 6, 0, 2, 2);"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #66BB6A, #388E3C);" +
            "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-family: 'Georgia'; -fx-font-size: 14px;" +
            "-fx-background-radius: 8; -fx-border-color: #C8E6C9; -fx-border-radius: 8; -fx-border-width: 1.5;" +
            "-fx-padding: 7 28; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 8, 0, 2, 3);"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #4CAF50, #2E7D32);" +
            "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-family: 'Georgia'; -fx-font-size: 14px;" +
            "-fx-background-radius: 8; -fx-border-color: #A5D6A7; -fx-border-radius: 8; -fx-border-width: 1.5;" +
            "-fx-padding: 7 28; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 6, 0, 2, 2);"
        ));
    }

    private static VBox buildAlertLayout() {
        VBox v = new VBox(18); v.setAlignment(Pos.CENTER); v.setPadding(new Insets(30, 36, 28, 36));
        v.setBackground(new Background(new BackgroundFill(Color.web("#1C0F05"), new CornerRadii(14), Insets.EMPTY)));
        v.setBorder(new Border(new BorderStroke(Color.web("#C4A97A"), BorderStrokeStyle.SOLID, new CornerRadii(14), new BorderWidths(2))));
        javafx.scene.effect.DropShadow ds = new javafx.scene.effect.DropShadow();
        ds.setColor(Color.web("#000000", 0.7)); ds.setRadius(20); ds.setOffsetX(4); ds.setOffsetY(4);
        v.setEffect(ds);
        return v;
    }

    public static void main(String[] args) { launch(args); }
}