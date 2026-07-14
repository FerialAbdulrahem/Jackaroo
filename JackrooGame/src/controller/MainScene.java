package controller;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import engine.board.Cell;
import engine.board.CellType;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
// Spinner removed — replaced with dual-circle split picker
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MainScene {
    // Screen dimensions for responsive layout
    public DoubleProperty screenWidth = new SimpleDoubleProperty();
    public DoubleProperty screenHeight = new SimpleDoubleProperty();
    private DoubleProperty scaleFactorX = new SimpleDoubleProperty();
    private DoubleProperty scaleFactorY = new SimpleDoubleProperty();
    private static final double REFERENCE_WIDTH = 1869.0;
    private static final double REFERENCE_HEIGHT = 954.0;

    // Main container
    public AnchorPane root;

    // Central octagon
    public Polygon octagon;

    // Consolidated array for all circle cells (100 elements)
    public Circle[] allCircleCells = new Circle[100];
    public int card;

    // Player groups and their components
    public static class PlayerGroup {
        public Group group;
        public GridPane[] circleGrids = new GridPane[4];
        public GridPane centerGrid;
        public Label label;
        public Circle extraCircle;
    }

    public PlayerGroup cpu1Group = new PlayerGroup();
    public PlayerGroup cpu2Group = new PlayerGroup();
    public PlayerGroup cpu3Group = new PlayerGroup();
    public PlayerGroup humanGroup = new PlayerGroup();

    // Additional groups
    public static class AdditionalGroup {
        public Group group;
        public GridPane grid1;
        public GridPane grid2;
    }

    public AdditionalGroup group1 = new AdditionalGroup();
    public AdditionalGroup group2 = new AdditionalGroup();
    public AdditionalGroup group3 = new AdditionalGroup();
    public AdditionalGroup group4 = new AdditionalGroup();

    // Grid panes with circles
    public GridPane[] gridPanes = new GridPane[3];

    // UI elements
    public Rectangle firePit, deckRect;
    public Label firePitLabel, deckLabel, deckRemainingLabel;
    // FirePit card display
    public StackPane firePitStack;
    public Label firePitCardRank, firePitCardSuit, firePitCardName;
    // Deck stack visual (3 offset rects)
    public Rectangle deckShadow1, deckShadow2;
    public Button playButton;
    public Button skipButton;
    public Button helpButton;
    public Label currentPlayerLabel, nextPlayerLabel;
    public Label remainingCards1, remainingCards2, remainingCards3, remainingCards4;
    public GridPane controlGrid;
    public CheckBox[] checkBoxes = new CheckBox[4];
    public Label[] controlLabels = new Label[4];
    // Card visual panels (one per card slot)
    public StackPane[] cardPanels = new StackPane[4];
    public Circle[] miscCircles = new Circle[4];
    public Label humanLabel, cpu3Label, cpu2Label, cpu1Label;
    public Label miscLabel;
    public CircleGrid c;
    // CPU face-down card rows (shown near each CPU label)
    public javafx.scene.layout.HBox cpu1CardRow, cpu2CardRow, cpu3CardRow;

    // Action panel shown below the card area
    public VBox actionPanel;
    public Button discardButton;   // for Ten / Queen discard action
    public Button splitButton;     // for Seven split action
    public HBox splitRow;          // the split row container (stored as field, not looked up)
    // Dual-circle split picker (replaces old Spinner)
    public Label splitLabel;
    public Label splitCircle1;     // shows steps for marble 1
    public Label splitCircle2;     // shows steps for marble 2
    public int splitDist1 = 1;     // steps assigned to marble 1 (1–6, total always 7)
    // Fired whenever the split +/- buttons change splitDist1, so callers (e.g.
    // Main's live landing-cell preview) can react immediately.
    public Runnable onSplitChanged = null;

    public void setOnSplitChanged(Runnable r) {
        this.onSplitChanged = r;
    }

    // Constructor to initialize screen dimensions
    public MainScene() {
        screenWidth.set(Screen.getPrimary().getVisualBounds().getWidth());
        screenHeight.set(Screen.getPrimary().getVisualBounds().getHeight());
        updateLayout();
    }

    private double calcX(double reference) {
        double centerOffset = 0;
        if ((screenWidth.get() / screenHeight.get()) > (REFERENCE_WIDTH / REFERENCE_HEIGHT)) {
            centerOffset = (screenWidth.get() - (REFERENCE_WIDTH * scaleFactorY.get())) / 2;
        }
        return reference * scaleFactorX.get() + centerOffset;
    }

    private double calcY(double reference) {
        double centerOffset = 0;
        if ((screenWidth.get() / screenHeight.get()) < (REFERENCE_WIDTH / REFERENCE_HEIGHT)) {
            centerOffset = (screenHeight.get() - (REFERENCE_HEIGHT * scaleFactorX.get())) / 2;
        }
        return reference * scaleFactorY.get() + centerOffset;
    }

    private double calcSize(double reference) {
        return reference * Math.min(scaleFactorX.get(), scaleFactorY.get());
    }

    private Font calcFont(double fontSize) {
        return Font.font(fontSize * Math.min(scaleFactorX.get(), scaleFactorY.get()));
    }

    private Font calcFont(String fontFamily, FontWeight weight, double fontSize) {
        return Font.font(fontFamily, weight, fontSize * Math.min(scaleFactorX.get(), scaleFactorY.get()));
    }

    public void updateLayout() {
        double scaleX = screenWidth.get() / REFERENCE_WIDTH;
        double scaleY = screenHeight.get() / REFERENCE_HEIGHT;
        if (scaleX > scaleY) {
            scaleFactorX.set(scaleY);
            scaleFactorY.set(scaleY);
        } else {
            scaleFactorX.set(scaleX);
            scaleFactorY.set(scaleX);
        }
    }

    public Scene createGameScene() {
        screenWidth.set(Screen.getPrimary().getVisualBounds().getWidth());
        screenHeight.set(Screen.getPrimary().getVisualBounds().getHeight());
        updateLayout();

        root = new AnchorPane();
        root.setLayoutX(0.0);
        root.setLayoutY(0.0);
        root.setPrefHeight(screenHeight.get());
        root.setPrefWidth(screenWidth.get());

        AnchorPane gameBoard = new AnchorPane();
     // Load light maple wood texture
        Image woodTexture = new Image("file:woodl.jpg");


        BackgroundImage bgImage = new BackgroundImage(
        	    woodTexture,
        	    BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
        	    BackgroundPosition.DEFAULT,
        	    new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, false)
        	);

        	gameBoard.setBackground(new Background(bgImage));
        	root.setBackground(new Background(bgImage));


        // Apply wood background
        gameBoard.setBackground(new Background(bgImage));
        root.setBackground(new Background(bgImage));


        // Apply wood background
        gameBoard.setBackground(new Background(bgImage));
        root.setBackground(new Background(bgImage));


        AnchorPane.setTopAnchor(gameBoard, 0.0);
        AnchorPane.setBottomAnchor(gameBoard, 0.0);
        AnchorPane.setLeftAnchor(gameBoard, 0.0);
        AnchorPane.setRightAnchor(gameBoard, 0.0);

        root.getChildren().add(gameBoard);
        root.setBackground(new Background(new BackgroundFill(Color.ANTIQUEWHITE, null, null)));

        // Board
        octagon = createOctagon();
        c = new CircleGrid();
        c.setMainScene(this);
        c.setScaleX(calcSize(1.4));
        c.setScaleY(calcSize(1));
        c.setTranslateX(calcX(120.0));
        c.setLayoutX(calcX(300.0));
        c.setLayoutY(calcY(160.0));
        gameBoard.getChildren().addAll(octagon, c);

        // Fire pit
        firePit = createFirePit();
        firePitLabel = createFirePitLabel();
        firePitStack = createFirePitStack();
        gameBoard.getChildren().addAll(firePit, firePitStack);

        // Player name labels
        humanLabel = createHumanLabel();
        cpu3Label = createCPU3Label();
        cpu2Label = createCPU2Label();
        cpu1Label = createCPU1Label();
        gameBoard.getChildren().addAll(humanLabel, cpu3Label, cpu2Label, cpu1Label);

        // CPU face-down card rows
        // CPU1 (left): vertical strip, left of board, mid-height
        cpu1CardRow = buildCpuCardRow(4, calcX(55.0), calcY(380.0), true);
        // CPU2 (top): horizontal row, centered above board
        cpu2CardRow = buildCpuCardRow(4, calcX(430.0), calcY(10.0), false);
        // CPU3 (right): vertical strip, right of board, mid-height
        cpu3CardRow = buildCpuCardRow(4, calcX(1130.0), calcY(380.0), true);
        gameBoard.getChildren().addAll(cpu1CardRow, cpu2CardRow, cpu3CardRow);

        // Card hand panel (replaces old controlGrid with checkboxes+labels)
        buildCardHandPanel(gameBoard);

        // Action panel (discard / split) – initially hidden
        buildActionPanel(gameBoard);

        // Remaining cards — CPU 3 = top-right, CPU 1 = bottom-left, CPU 2 = top-center
        remainingCards1 = createRemainingCardsLabel("", calcX(1120.0), calcY(14.0));  // CPU 3 top right
        remainingCards2 = createRemainingCardsLabel("", calcX(48.0),   calcY(850.0)); // CPU 1 bottom left
        remainingCards3 = createRemainingCardsLabel("", calcX(48.0),   calcY(14.0));  // CPU 2 top left
        remainingCards4 = createRemainingCardsLabel("", calcX(48.0),   calcY(18.0));  // unused placeholder
        gameBoard.getChildren().addAll(remainingCards1, remainingCards2, remainingCards3, remainingCards4);

        // Play button — properly sized and positioned below deck
        playButton = createPlayButton();
        initializePlayButtonHandler();
        gameBoard.getChildren().add(playButton);

        // Skip button
        skipButton = createSkipButton();
        // Help button (opens game description + Settings/Rules)
        helpButton = createHelpButton();
        gameBoard.getChildren().add(helpButton);
        gameBoard.getChildren().add(skipButton);

        // Turn labels — organized at top-right
        currentPlayerLabel = createCurrentPlayerLabel();
        nextPlayerLabel = createNextPlayerLabel();
        gameBoard.getChildren().addAll(currentPlayerLabel, nextPlayerLabel);

        // Deck
        deckShadow1 = createDeckShadow(calcSize(6), calcSize(6));
        deckShadow2 = createDeckShadow(calcSize(3), calcSize(3));
        deckRect = createDeckRect();
        deckLabel = createDeckLabel();
        deckRemainingLabel = createDeckRemainingLabel();
        gameBoard.getChildren().addAll(deckShadow1, deckShadow2, deckRect, deckLabel, deckRemainingLabel);

        // Misc
        miscLabel = createMiscLabel();
        miscCircles[0] = createMiscCircle(calcX(780.0), calcY(50.0));
        miscCircles[1] = createMiscCircle(calcX(1160.0), calcY(589.0));
        miscCircles[2] = createMiscCircle(calcX(850.0), calcY(950.0));
        miscCircles[3] = createMiscCircle(calcX(260.0), calcY(550.0));
        gameBoard.getChildren().addAll(miscLabel, miscCircles[0], miscCircles[1], miscCircles[2], miscCircles[3]);

        Scene scene = new Scene(root);
        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            screenWidth.set(newVal.doubleValue());
            updateLayout();
            updateAllComponents();
        });
        scene.heightProperty().addListener((obs, oldVal, newVal) -> {
            screenHeight.set(newVal.doubleValue());
            updateLayout();
            updateAllComponents();
        });

        return scene;
    }

    // ─── Card Hand Panel ────────────────────────────────────────────────────────

    /**
     * Builds the 4-card visual hand panel in the bottom-right area.
     * Each card is a StackPane styled like a playing card (rectangle + rank/suit/description).
     */
    private void buildCardHandPanel(AnchorPane gameBoard) {
        controlGrid = new GridPane(); // keep it but don't add to scene
        
        for (int i = 0; i < 4; i++) {
            checkBoxes[i] = new CheckBox();
            checkBoxes[i].setVisible(false);
            checkBoxes[i].setManaged(false);

            controlLabels[i] = new Label();
            controlLabels[i].setVisible(false);
            controlLabels[i].setManaged(false);

            cardPanels[i] = buildCardPanel(i);

            // Place each card directly in gameBoard at its own position
            double cardX = calcX(1080.0 + i * 138.0); // space them 138 apart horizontally
            double cardY = calcY(730.0);

            cardPanels[i].setLayoutX(cardX);
            cardPanels[i].setLayoutY(cardY);

            gameBoard.getChildren().add(cardPanels[i]);
        }
    }
    /**
     * Builds a single visual card StackPane for slot index.
     */
    public StackPane buildCardPanel(int index) {
        double cardW = calcSize(130);
        double cardH = calcSize(190);

        // Card background — crisp parchment with subtle gradient
        Rectangle bg = new Rectangle(cardW, cardH);
        bg.setArcWidth(calcSize(16)); bg.setArcHeight(calcSize(16));
        javafx.scene.paint.LinearGradient cardGrad = new javafx.scene.paint.LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0.00, Color.web("#FDFAF4")),
            new Stop(0.40, Color.web("#F0E6D0")),
            new Stop(1.00, Color.web("#D9C9A8")));
        bg.setFill(cardGrad);
        bg.setStroke(Color.web("#8B6914"));
        bg.setStrokeWidth(calcSize(2));
        javafx.scene.effect.DropShadow cardShadow = new javafx.scene.effect.DropShadow();
        cardShadow.setColor(Color.web("#000000", 0.50)); cardShadow.setRadius(calcSize(14));
        cardShadow.setOffsetX(calcSize(3)); cardShadow.setOffsetY(calcSize(4));
        bg.setEffect(cardShadow);

        // Decorative inner frame
        Rectangle inner = new Rectangle(cardW - calcSize(12), cardH - calcSize(14));
        inner.setArcWidth(calcSize(10)); inner.setArcHeight(calcSize(10));
        inner.setFill(Color.TRANSPARENT);
        inner.setStroke(Color.web("#C4A97A", 0.40));
        inner.setStrokeWidth(calcSize(1.2));
        StackPane.setAlignment(inner, Pos.CENTER);

        // Corner rank label — top-left
        Label rankLabel = new Label("?");
        rankLabel.setFont(calcFont("Georgia", FontWeight.BOLD, 20));
        rankLabel.setTextFill(Color.web("#3A1F0A"));
        StackPane.setAlignment(rankLabel, Pos.TOP_LEFT);
        rankLabel.setTranslateX(calcSize(9)); rankLabel.setTranslateY(calcSize(7));

        // Bottom-right rank (rotated 180° effect — just another label)
        Label rankLabelBR = new Label("?");
        rankLabelBR.setFont(calcFont("Georgia", FontWeight.BOLD, 20));
        rankLabelBR.setTextFill(Color.web("#3A1F0A"));
        StackPane.setAlignment(rankLabelBR, Pos.BOTTOM_RIGHT);
        rankLabelBR.setTranslateX(calcSize(-9)); rankLabelBR.setTranslateY(calcSize(-7));
        rankLabelBR.setRotate(180);

        // Large centre suit symbol
        Label suitLabel = new Label("♠");
        suitLabel.setFont(Font.font("Georgia", FontWeight.BOLD, calcSize(42)));
        suitLabel.setTextFill(Color.web("#3A1F0A"));
        StackPane.setAlignment(suitLabel, Pos.CENTER);
        suitLabel.setTranslateY(calcSize(-8));

        // Card name at top centre
        Label cardNumLabel = new Label("Card " + (index + 1));
        cardNumLabel.setFont(calcFont("Georgia", FontWeight.BOLD, 10));
        cardNumLabel.setTextFill(Color.web("#7A5230", 0.85));
        StackPane.setAlignment(cardNumLabel, Pos.TOP_CENTER);
        cardNumLabel.setTranslateY(calcSize(7));

        // Description at bottom
        Label descLabel = new Label("");
        descLabel.setFont(calcFont("Georgia", FontWeight.NORMAL, 9));
        descLabel.setTextFill(Color.web("#4A2C10"));
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(cardW - calcSize(14));
        descLabel.setTextAlignment(TextAlignment.CENTER);
        StackPane.setAlignment(descLabel, Pos.BOTTOM_CENTER);
        descLabel.setTranslateY(calcSize(-30));

        StackPane panel = new StackPane(bg, inner, rankLabel, rankLabelBR, suitLabel, cardNumLabel, descLabel);
        panel.setPrefSize(cardW, cardH);
        panel.setMaxSize(cardW, cardH);
        panel.setUserData(new Label[]{rankLabel, suitLabel, descLabel, cardNumLabel, rankLabelBR});

        final int idx = index;
        panel.setOnMouseClicked(e -> {
            if (!panel.isVisible()) return;
            boolean wasSelected = checkBoxes[idx].isSelected();
            for (int k = 0; k < 4; k++) { checkBoxes[k].setSelected(false); highlightCard(k, false); }
            if (!wasSelected) { checkBoxes[idx].setSelected(true); highlightCard(idx, true); }
        });
        panel.setOnMouseEntered(e -> {
            if (panel.isVisible() && !checkBoxes[idx].isSelected()) {
                panel.setTranslateY(calcSize(-6));
                bg.setStroke(Color.web("#b0977d"));
                bg.setStrokeWidth(calcSize(3));
                javafx.scene.effect.DropShadow glow = new javafx.scene.effect.DropShadow();
                glow.setColor(Color.web("#b0977d", 0.7)); glow.setRadius(calcSize(16));
                glow.setOffsetX(0); glow.setOffsetY(calcSize(2));
                bg.setEffect(glow);
            }
        });
        panel.setOnMouseExited(e -> {
            if (!checkBoxes[idx].isSelected()) {
                panel.setTranslateY(0);
                bg.setStroke(Color.web("#8B6914")); bg.setStrokeWidth(calcSize(2));
                bg.setEffect(cardShadow);
            }
        });
        return panel;
    }

    /** Highlights or un-highlights the card panel at the given index. */
    public void highlightCard(int index, boolean selected) {
        if (cardPanels[index] == null) return;
        StackPane panel = cardPanels[index];
        Rectangle bg = (Rectangle) panel.getChildren().get(0);
        if (selected) {
            panel.setTranslateY(calcSize(-10));
            bg.setStroke(Color.web("#b1976f"));
            bg.setStrokeWidth(calcSize(3.5));
            javafx.scene.paint.LinearGradient selGrad = new javafx.scene.paint.LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#FFFDE7")),
                new Stop(1.0, Color.web("#FFF9C4")));
            bg.setFill(selGrad);
            javafx.scene.effect.DropShadow selGlow = new javafx.scene.effect.DropShadow();
            selGlow.setColor(Color.web("#b0977d", 0.85)); selGlow.setRadius(calcSize(20));
            selGlow.setSpread(0.15);
            bg.setEffect(selGlow);
        } else {
            panel.setTranslateY(0);
            javafx.scene.paint.LinearGradient normalGrad = new javafx.scene.paint.LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.00, Color.web("#FDFAF4")),
                new Stop(0.40, Color.web("#F0E6D0")),
                new Stop(1.00, Color.web("#D9C9A8")));
            bg.setFill(normalGrad);
            bg.setStroke(Color.web("#8B6914")); bg.setStrokeWidth(calcSize(2));
            javafx.scene.effect.DropShadow normalShadow = new javafx.scene.effect.DropShadow();
            normalShadow.setColor(Color.web("#000000", 0.50)); normalShadow.setRadius(calcSize(14));
            normalShadow.setOffsetX(calcSize(3)); normalShadow.setOffsetY(calcSize(4));
            bg.setEffect(normalShadow);
        }
    }

 
    public void updateCardPanel(int index, String rankStr, String suitSymbol,
            Color suitColor, String description, String cardTitle) {
        if (cardPanels[index] == null) return;
        Label[] labels = (Label[]) cardPanels[index].getUserData();
        labels[0].setText(rankStr);
        labels[0].setTextFill(suitColor);
        labels[1].setText(suitSymbol);
        labels[1].setTextFill(suitColor);
        labels[2].setText(description);
        labels[3].setText(cardTitle);
        labels[3].setTextFill(suitColor.equals(Color.RED) || suitColor.equals(Color.CRIMSON)
                ? Color.web("#8B0000") : Color.web("#1A237E"));
        // Sync bottom-right rank label (index 4)
        if (labels.length > 4 && labels[4] != null) {
            labels[4].setText(rankStr);
            labels[4].setTextFill(suitColor);
        }
        cardPanels[index].setVisible(true);
        cardPanels[index].setOpacity(1.0);
        checkBoxes[index].setSelected(false);
        highlightCard(index, false);
    }

    /** Hides a card panel (card has been played). */
    public void hideCardPanel(int index) {
        if (cardPanels[index] != null) {
            cardPanels[index].setVisible(false);
            cardPanels[index].setOpacity(0);
            checkBoxes[index].setSelected(false);
        }
    }


    private void buildActionPanel(AnchorPane gameBoard) {
        actionPanel = new VBox(calcSize(10));
        actionPanel.setLayoutX(calcX(1500.0));
        actionPanel.setLayoutY(calcY(510.0));
        actionPanel.setAlignment(Pos.CENTER_LEFT);
        actionPanel.setPadding(new Insets(calcSize(12)));
        actionPanel.setBackground(new Background(new BackgroundFill(
                Color.web("#1C0F05", 0.93), new CornerRadii(calcSize(12)), Insets.EMPTY)));
        actionPanel.setBorder(new Border(new BorderStroke(
                Color.web("#C4A97A"), BorderStrokeStyle.SOLID,
                new CornerRadii(calcSize(12)), new BorderWidths(calcSize(1.5)))));
        javafx.scene.effect.DropShadow panelShadow = new javafx.scene.effect.DropShadow();
        panelShadow.setColor(Color.web("#000000", 0.65));
        panelShadow.setRadius(calcSize(14));
        panelShadow.setOffsetX(calcSize(2));
        panelShadow.setOffsetY(calcSize(3));
        actionPanel.setEffect(panelShadow);
        actionPanel.setVisible(false);

        // ── Discard button (Ten / Queen / no-marbles-out) ──────────────────────
        discardButton = new Button("Discard Card");
        discardButton.setPrefHeight(calcSize(44));
        discardButton.setPrefWidth(calcSize(270));
        String discBase =
                "-fx-background-color: linear-gradient(to bottom, #4A2F1A, #2C1810);" +
                "-fx-text-fill: #E8D5B5; -fx-font-weight: bold; -fx-font-family: 'Georgia';" +
                "-fx-font-size: " + (int)calcSize(18) + "px; -fx-background-radius: 26;" +
                "-fx-border-color: #f1dbb3; -fx-border-radius: 26; -fx-border-width: 2;" +
                "-fx-effect: dropshadow(gaussian, rgba(212, 163, 115, 0.3), 10, 0, 0, 2);";
        String discHover =
                "-fx-background-color: linear-gradient(to bottom, #6B4226, #3E2720);" +
                "-fx-text-fill: #FFF2DF; -fx-font-weight: bold; -fx-font-family: 'Georgia';" +
                "-fx-font-size: " + (int)calcSize(18) + "px; -fx-background-radius: 26;" +
                "-fx-border-color: #f1dbb3; -fx-border-radius: 26; -fx-border-width: 2.5;" +
                "-fx-effect: dropshadow(gaussian, rgba(240, 198, 138, 0.85), 25, 0, 0, 8);";
        discardButton.setStyle(discBase);
        discardButton.setOnMouseEntered(e -> discardButton.setStyle(discHover));
        discardButton.setOnMouseExited(e -> discardButton.setStyle(discBase));
        discardButton.setVisible(false);
        discardButton.setManaged(false);

        // ── Seven split: header label ──────────────────────────────────────────
        splitLabel = new Label("Split 7 steps between 2 marbles:");
        splitLabel.setFont(calcFont("Georgia", FontWeight.BOLD, 12));
        splitLabel.setTextFill(Color.web("#E8C97A"));
        splitLabel.setVisible(false);
        splitLabel.setManaged(false);

        // ── Build the two circle columns ───────────────────────────────────────
        splitDist1 = 1;
        javafx.scene.layout.VBox marble1Col = buildSplitColumn(1);
        javafx.scene.layout.VBox marble2Col = buildSplitColumn(2);

        // "+" divider between circles
        Label vsLabel = new Label("+");
        vsLabel.setFont(calcFont("Georgia", FontWeight.BOLD, 20));
        vsLabel.setTextFill(Color.web("#C4A97A"));
        vsLabel.setAlignment(Pos.CENTER);

    splitButton = new Button("✂ Split");
    String splitBase =
        "-fx-background-color: linear-gradient(to bottom, #4A2F1A, #2C1810);" +
        "-fx-text-fill: #E8D5B5;" +
        "-fx-font-weight: bold;" +
        "-fx-font-family: 'Georgia';" +
        "-fx-font-size: " + (int) calcSize(12) + "px;" +
        "-fx-background-radius: 26;" +
        "-fx-border-color: #f1dbb3;" +
        "-fx-border-radius: 26;" +
        "-fx-border-width: 1.5;" +
        "-fx-effect: dropshadow(gaussian, rgba(212,163,115,0.3), 8, 0, 0, 2);";
    String splitHover =
        "-fx-background-color: linear-gradient(to bottom, #6B4226, #3E2720);" +
        "-fx-text-fill: #FFF2DF;" +
        "-fx-font-weight: bold;" +
        "-fx-font-family: 'Georgia';" +
        "-fx-font-size: " + (int) calcSize(12) + "px;" +
        "-fx-background-radius: 26;" +
        "-fx-border-color: #f1dbb3;" +
        "-fx-border-radius: 26;" +
        "-fx-border-width: 2;" +
        "-fx-effect: dropshadow(gaussian, rgba(240,198,138,0.8), 18, 0, 0, 6);";
    splitButton.setStyle(splitBase);
    splitButton.setOnMouseEntered(e -> splitButton.setStyle(splitHover));
    splitButton.setOnMouseExited(e -> splitButton.setStyle(splitBase));
    splitButton.setPrefHeight(calcSize(34));
    splitButton.setPrefWidth(calcSize(100));

        // picker: [col1] [+] [col2]   and apply button below
        HBox circleRow = new HBox(calcSize(10), marble1Col, vsLabel, marble2Col);
        circleRow.setAlignment(Pos.CENTER_LEFT);

        // splitRow wraps [circleRow + splitButton] so we can hide/show everything together
        splitRow = new HBox(calcSize(12), circleRow, splitButton);
        splitRow.setAlignment(Pos.CENTER_LEFT);
        splitRow.setVisible(false);
        splitRow.setManaged(false);

        actionPanel.getChildren().addAll(discardButton, splitLabel, splitRow);
        gameBoard.getChildren().add(actionPanel);
    }

    /** Builds one marble column: header, [−] circle [+] */
    private javafx.scene.layout.VBox buildSplitColumn(int slot) {
        int initVal = (slot == 1) ? splitDist1 : (7 - splitDist1);
        Label circle = new Label(String.valueOf(initVal));
        circle.setAlignment(Pos.CENTER);
        circle.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        double cSize = calcSize(52);
        circle.setMinSize(cSize, cSize);
        circle.setMaxSize(cSize, cSize);
        circle.setPrefSize(cSize, cSize);
        circle.setFont(calcFont("Georgia", FontWeight.BOLD, 24));
        circle.setTextFill(Color.web("#FAF3E0"));
        circle.setBackground(new Background(new BackgroundFill(
                Color.web("#341805"), new CornerRadii(cSize / 2), Insets.EMPTY)));
        circle.setBorder(new Border(new BorderStroke(
                Color.web("#4a2d1b"), BorderStrokeStyle.SOLID,
                new CornerRadii(cSize / 2), new BorderWidths(calcSize(2.5)))));
        javafx.scene.effect.DropShadow cs = new javafx.scene.effect.DropShadow();
        cs.setColor(Color.web("#000000", 0.55)); cs.setRadius(calcSize(9));
        circle.setEffect(cs);

        if (slot == 1) splitCircle1 = circle;
        else           splitCircle2 = circle;

        Button minus = new Button("−");
        styleCircleStepBtn(minus);
        minus.setOnAction(ev -> adjustSplit(slot, -1));

        Button plus = new Button("+");
        styleCircleStepBtn(plus);
        plus.setOnAction(ev -> adjustSplit(slot, +1));

        HBox controls = new HBox(calcSize(4), minus, circle, plus);
        controls.setAlignment(Pos.CENTER);

        Label header = new Label("Marble " + slot);
        header.setFont(calcFont("Georgia", FontWeight.BOLD, 10));
        header.setTextFill(Color.web("#C4A97A"));
        header.setAlignment(Pos.CENTER);
        header.setMinWidth(calcSize(120));

        javafx.scene.layout.VBox col = new javafx.scene.layout.VBox(calcSize(4), header, controls);
        col.setAlignment(Pos.CENTER);
        return col;
    }

    private void styleWalnutButton(Button btn, String darkColor, String darkerColor, String borderColor) {
        String base =
            "-fx-background-color: linear-gradient(to bottom, " + darkColor + ", " + darkerColor + ");" +
            "-fx-text-fill: #FAF3E0;" +
            "-fx-font-weight: bold;" +
            "-fx-font-family: 'Georgia';" +
            "-fx-font-size: " + (int) calcSize(14) + "px;" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: " + borderColor + ";" +
            "-fx-border-radius: 14;" +
            "-fx-border-width: 1.5;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.60), 10, 0, 2, 4);";
        String hover =
            "-fx-background-color: linear-gradient(to bottom, " + borderColor + ", " + darkColor + ");" +
            "-fx-text-fill: #FFFDE7;" +
            "-fx-font-weight: bold;" +
            "-fx-font-family: 'Georgia';" +
            "-fx-font-size: " + (int) calcSize(14) + "px;" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: #D7CCC8;" +
            "-fx-border-radius: 14;" +
            "-fx-border-width: 2;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.75), 14, 0, 2, 6);";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
    }

    private void styleCircleStepBtn(Button btn) {
        double s = calcSize(28);
        btn.setMinSize(s, s); btn.setMaxSize(s, s); btn.setPrefSize(s, s);
        String base =
            "-fx-background-color: linear-gradient(to bottom,#2C4A6A,#0D2240);" +
            "-fx-text-fill:#FAF3E0;-fx-font-weight:bold;-fx-font-family:Georgia;" +
            "-fx-font-size:" + (int) calcSize(17) + "px;" +
            "-fx-background-radius:" + (int)(s/2) + ";" +
            "-fx-border-color:#5A9BD4;-fx-border-radius:" + (int)(s/2) + ";-fx-border-width:1.5;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.55),6,0,1,2);";
        String hover =
            "-fx-background-color: linear-gradient(to bottom,#5A9BD4,#2C4A6A);" +
            "-fx-text-fill:#FFFDE7;-fx-font-weight:bold;-fx-font-family:Georgia;" +
            "-fx-font-size:" + (int) calcSize(17) + "px;" +
            "-fx-background-radius:" + (int)(s/2) + ";" +
            "-fx-border-color:#D7CCC8;-fx-border-radius:" + (int)(s/2) + ";-fx-border-width:2;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.7),8,0,1,3);";
        btn.setStyle(base);
        btn.setOnMouseEntered(ev -> btn.setStyle(hover));
        btn.setOnMouseExited(ev -> btn.setStyle(base));
    }

    /** Adjusts the split. slot=1 adjusts marble-1, slot=2 adjusts marble-2 (mirror). Total always = 7. */
    public void adjustSplit(int slot, int delta) {
        int newDist1 = (slot == 1) ? (splitDist1 + delta) : (splitDist1 - delta);
        newDist1 = Math.max(1, Math.min(6, newDist1));
        splitDist1 = newDist1;
        if (splitCircle1 != null) splitCircle1.setText(String.valueOf(splitDist1));
        if (splitCircle2 != null) splitCircle2.setText(String.valueOf(7 - splitDist1));
        if (onSplitChanged != null) onSplitChanged.run();
    }

    /** Returns how many steps marble-1 gets in the split (marble-2 gets 7 minus this). */
    public int getSplitDistance() {
        return splitDist1;
    }

    public void showActionPanel(String cardType, boolean canSplit) {
        // Reset everything
        discardButton.setVisible(false);
        discardButton.setManaged(false);
        splitLabel.setVisible(false);
        splitLabel.setManaged(false);
        splitRow.setVisible(false);
        splitRow.setManaged(false);
        actionPanel.setVisible(false);

        switch (cardType) {
            case "TEN":
                discardButton.setText("Discard Next Opponent");
                discardButton.setVisible(true);
                discardButton.setManaged(true);
                actionPanel.setVisible(true);
                break;
            case "QUEEN":
                discardButton.setText("Discard Random Opponent");
                discardButton.setVisible(true);
                discardButton.setManaged(true);
                actionPanel.setVisible(true);
                break;
            case "DISCARD_ANY":
                discardButton.setText("Discard This Card");
                discardButton.setVisible(true);
                discardButton.setManaged(true);
                actionPanel.setVisible(true);
                break;
            case "SEVEN":
                if (canSplit) {
                    splitLabel.setVisible(true);
                    splitLabel.setManaged(true);
                    splitRow.setVisible(true);
                    splitRow.setManaged(true);
                    actionPanel.setVisible(true);
                }
                // canSplit=false means only 1 marble on track → play via Play button, no panel needed
                // When canSplit=true the panel is shown so user can pick 2 marbles and apply split
                break;
            default:
                // leave everything hidden
                break;
        }
    }


    // ─── updateAllComponents ────────────────────────────────────────────────────

    public void updateAllComponents() {
        if (root != null) {
            root.setPrefWidth(screenWidth.get());
            root.setPrefHeight(screenHeight.get());
        }

        octagon.setLayoutX(calcX(748.0));
        octagon.setLayoutY(calcY(680.0));
        octagon.setScaleX(calcSize(1.5));
        octagon.setScaleY(calcSize(1.5));
        octagon.setTranslateX(calcX(120.0));

        c.updateDimensions(screenWidth.get(), screenHeight.get());
        c.setScaleX(calcSize(1.4));
        c.setScaleY(calcSize(1.3));
        c.setTranslateX(calcX(120.0));
        c.setLayoutX(calcX(300.0));
        c.setLayoutY(calcY(170.0));

        firePit.setX(calcX(648.0));
        firePit.setY(calcY(362.0));
        firePit.setWidth(calcSize(152.0));
        firePit.setHeight(calcSize(206.0));
        firePitLabel.setLayoutX(calcX(648.0));
        firePitLabel.setLayoutY(calcY(362.0));
        firePitLabel.setPrefWidth(calcSize(152.0));
        firePitLabel.setPrefHeight(calcSize(206.0));
        firePitLabel.setFont(calcFont("Arial Bold", FontWeight.BOLD, 18.0));
        if (firePitStack != null) {
            firePitStack.setLayoutX(calcX(648.0));
            firePitStack.setLayoutY(calcY(362.0));
            firePitStack.setPrefWidth(calcSize(152.0));
            firePitStack.setPrefHeight(calcSize(206.0));
            if (firePitCardRank != null) firePitCardRank.setFont(Font.font("Georgia", FontWeight.BOLD, calcSize(42)));
            if (firePitCardSuit != null) firePitCardSuit.setFont(Font.font("Arial", FontWeight.BOLD, calcSize(22)));
            if (firePitCardRank != null) firePitCardRank.setTranslateY(calcSize(-10));
            if (firePitCardSuit != null) firePitCardSuit.setTranslateY(calcSize(22));
            if (firePitCardName != null) firePitCardName.setTranslateY(calcSize(-8));
        }

        humanLabel.setLayoutX(calcX(500.0));
        humanLabel.setTranslateX(calcX(120.0));
        humanLabel.setLayoutY(calcY(900.0));
        humanLabel.setFont(calcFont(51.0));

        cpu3Label.setLayoutX(calcX(1080.0));
        cpu3Label.setLayoutY(calcY(450.0));
        cpu3Label.setFont(calcFont(42.0));

        cpu1Label.setLayoutX(calcX(180.0));
        cpu1Label.setLayoutY(calcY(450.0));
        cpu1Label.setFont(calcFont(42.0));

        cpu2Label.setLayoutX(calcX(500.0));
        cpu2Label.setTranslateX(calcX(120.0));
        cpu2Label.setLayoutY(calcY(20.0));
        cpu2Label.setFont(calcFont(42.0));

        controlGrid.setLayoutX(calcX(430.0));
        controlGrid.setLayoutY(calcY(820.0));
        controlGrid.setHgap(calcSize(12));

        actionPanel.setLayoutX(calcX(1500.0));
        actionPanel.setLayoutY(calcY(510.0));

        remainingCards1.setLayoutX(calcX(1120.0));
        remainingCards1.setLayoutY(calcY(14.0));
        remainingCards1.setFont(calcFont(22.0));

        remainingCards2.setLayoutX(calcX(48.0));
        remainingCards2.setLayoutY(calcY(850.0));
        remainingCards2.setFont(calcFont(22.0));

        remainingCards3.setLayoutX(calcX(48.0));
        remainingCards3.setLayoutY(calcY(14.0));
        remainingCards3.setFont(calcFont(22.0));

        // ── Keep all 4 card panels fixed at their original positions ──
        for (int i = 0; i < 4; i++) {
            if (cardPanels[i] != null) {
                cardPanels[i].setLayoutX(calcX(1080.0 + i * 138.0));
                cardPanels[i].setLayoutY(calcY(730.0));
            }
        }

        playButton.setLayoutX(calcX(1500.0));
        playButton.setLayoutY(calcY(430.0));
        playButton.setPrefHeight(calcSize(50.0));
        playButton.setPrefWidth(calcSize(145.0));
        playButton.setFont(calcFont("Georgia", FontWeight.BOLD, 16.5));

        if (skipButton != null) {
            skipButton.setLayoutX(calcX(1667.0));
            skipButton.setLayoutY(calcY(430.0));
            skipButton.setPrefHeight(calcSize(50.0));
            skipButton.setPrefWidth(calcSize(145.0));
            skipButton.setFont(calcFont("Georgia", FontWeight.BOLD, 16.5));
        }

        if (cpu1CardRow != null) { cpu1CardRow.setLayoutX(calcX(55.0));   cpu1CardRow.setLayoutY(calcY(450.0)); }
        if (cpu2CardRow != null) { cpu2CardRow.setLayoutX(calcX(595.0));  cpu2CardRow.setLayoutY(calcY(-50.0)); }
        if (cpu3CardRow != null) { cpu3CardRow.setLayoutX(calcX(1130.0)); cpu3CardRow.setLayoutY(calcY(450.0)); }

        currentPlayerLabel.setLayoutX(calcX(1500.0));
        currentPlayerLabel.setLayoutY(calcY(150.0));
        currentPlayerLabel.setPrefWidth(calcSize(310.0));
        currentPlayerLabel.setFont(calcFont("Georgia", FontWeight.BOLD, 16));

        nextPlayerLabel.setLayoutX(calcX(1500.0));
        nextPlayerLabel.setLayoutY(calcY(196.0));
        nextPlayerLabel.setPrefWidth(calcSize(310.0));
        nextPlayerLabel.setFont(calcFont("Georgia", FontWeight.BOLD, 16));

        if (deckShadow1 != null) {
            deckShadow1.setX(calcX(1500.0) + calcSize(6));
            deckShadow1.setY(calcY(196.0) + calcSize(6));
            deckShadow1.setWidth(calcSize(310.0));
            deckShadow1.setHeight(calcSize(200.0));
        }
        if (deckShadow2 != null) {
            deckShadow2.setX(calcX(1500.0) + calcSize(3));
            deckShadow2.setY(calcY(200.0) + calcSize(3));
            deckShadow2.setWidth(calcSize(310.0));
            deckShadow2.setHeight(calcSize(200.0));
        }

        deckRect.setX(calcX(1500.0));
        deckRect.setY(calcY(200.0));
        deckRect.setWidth(calcSize(310.0));
        deckRect.setHeight(calcSize(200.0));

        deckLabel.setLayoutX(calcX(1500.0));
        deckLabel.setLayoutY(calcY(200.0));
        deckLabel.setPrefWidth(calcSize(310.0));
        deckLabel.setPrefHeight(calcSize(130.0));
        deckLabel.setFont(calcFont("Georgia", FontWeight.BOLD, 18.0));

        deckRemainingLabel.setLayoutX(calcX(1500.0));
        deckRemainingLabel.setLayoutY(calcY(300.0));
        deckRemainingLabel.setPrefWidth(calcSize(310.0));
        deckRemainingLabel.setFont(calcFont("Georgia", FontWeight.BOLD, 18.0));

        if (helpButton != null) {
            helpButton.setLayoutX(calcX(1500.0));
            helpButton.setLayoutY(calcY(30.0));
            helpButton.setPrefHeight(calcSize(48.0));
            helpButton.setPrefWidth(calcSize(310.0));
            helpButton.setFont(calcFont("Georgia", FontWeight.BOLD, 15.0));
        }

        miscLabel.setLayoutX(calcX(1500.0));
        miscLabel.setLayoutY(calcY(92.0));
        miscLabel.setFont(calcFont("Georgia", FontWeight.BOLD, 12.0));
        miscLabel.setPrefWidth(calcSize(310));
        miscLabel.setPrefHeight(calcSize(50));
        miscLabel.setPadding(new Insets(calcSize(8), calcSize(12), calcSize(8), calcSize(12)));

        miscCircles[0].setCenterX(calcX(780.0));
        miscCircles[0].setCenterY(calcY(50.0));
        miscCircles[0].setRadius(calcSize(16.0));

        miscCircles[1].setCenterX(calcX(1160.0));
        miscCircles[1].setCenterY(calcY(589.0));
        miscCircles[1].setRadius(calcSize(16.0));

        miscCircles[2].setCenterX(calcX(850.0));
        miscCircles[2].setCenterY(calcY(950.0));
        miscCircles[2].setRadius(calcSize(16.0));

        miscCircles[3].setCenterX(calcX(260.0));
        miscCircles[3].setCenterY(calcY(550.0));
        miscCircles[3].setRadius(calcSize(16.0));
    }
    // ─── Rest of helpers (unchanged from original) ───────────────────────────────

    private Polygon createOctagon() {
        Polygon polygon = new Polygon();

        // Load the same wood texture image you used for the board
        Image woodTexture = new Image("file:wood.jpg");

        // Apply the image as fill for the octagon
        polygon.setFill(new javafx.scene.paint.ImagePattern(woodTexture));

        // Optional: keep an outline so the octagon stands out
        polygon.setStroke(Color.web("#372412"));
        polygon.setStrokeType(javafx.scene.shape.StrokeType.INSIDE);
        polygon.setStrokeWidth(calcSize(7.0));

        // Drop shadow for depth
        javafx.scene.effect.DropShadow woodShadow = new javafx.scene.effect.DropShadow();
        woodShadow.setColor(Color.web("#2A1005", 0.7));
        woodShadow.setRadius(calcSize(20));
        woodShadow.setOffsetX(calcSize(4));
        woodShadow.setOffsetY(calcSize(4));
        polygon.setEffect(woodShadow);

        // Define the octagon points
        polygon.getPoints().addAll(
            151.45, -175.73, 63.58, 36.40, -148.55, 124.27, -360.68, 36.40,
            -448.55, -175.73, -360.68, -387.86, -148.55, -475.73, 63.58, -387.86
        );

        polygon.setRotate(67.5);
        polygon.setScaleX(calcSize(1.5));
        polygon.setScaleY(calcSize(1.5));
        polygon.setTranslateX(calcX(120.0));
        polygon.setLayoutX(calcX(748.0));
        polygon.setLayoutY(calcY(680.0));

        return polygon;
    }


    private Rectangle createFirePit() {
        Rectangle rect = new Rectangle(calcX(648.0), calcY(362.0), calcSize(152.0), calcSize(206.0));
        rect.setArcHeight(calcSize(16.0));
        rect.setArcWidth(calcSize(16.0));

        // Load image with error check
        Image firePitImage = new Image("file:woodm.jpg");
        if (firePitImage.isError()) {
            System.out.println("Image not found! Error: " + firePitImage.getException());
            // Fallback to original gradient
            rect.setFill(Color.web("#F5E6C8"));
        } else {
            rect.setFill(new javafx.scene.paint.ImagePattern(firePitImage));
        }
       
        rect.setStroke(Color.web("#372412"));
        rect.setStrokeType(javafx.scene.shape.StrokeType.INSIDE);
        rect.setStrokeWidth(calcSize(4.0));
        javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
        shadow.setColor(Color.web("#5A3015", 0.55));
        shadow.setRadius(calcSize(16));
        shadow.setOffsetX(calcSize(3));
        shadow.setOffsetY(calcSize(3));
        rect.setEffect(shadow);
        return rect;
    }

    private Label createFirePitLabel() {
        // Kept for API compatibility but hidden — firePitStack is used instead
        Label label = new Label("");
        label.setVisible(false);
        return label;
    }

    private StackPane createFirePitStack() {
        StackPane stack = new StackPane();
        stack.setLayoutX(calcX(648.0));
        stack.setLayoutY(calcY(362.0));
        stack.setPrefWidth(calcSize(152.0));
        stack.setPrefHeight(calcSize(206.0));

        // 🔥 title at the top
        Label titleLbl = new Label("🔥  FirePit");
        titleLbl.setFont(calcFont("Georgia", FontWeight.BOLD, 12.0));
        titleLbl.setTextFill(Color.web("#9E7D5C"));
        StackPane.setAlignment(titleLbl, Pos.TOP_CENTER);
        titleLbl.setTranslateY(calcSize(7));

        // Divider line visual (thin rect)
        javafx.scene.shape.Rectangle divider = new javafx.scene.shape.Rectangle(calcSize(110), calcSize(1.5));
        divider.setFill(Color.web("#372412"));
        StackPane.setAlignment(divider, Pos.TOP_CENTER);
        divider.setTranslateY(calcSize(30));

        // Big rank in center
        firePitCardRank = new Label("—");
        firePitCardRank.setFont(Font.font("Georgia", FontWeight.BOLD, calcSize(44)));
        firePitCardRank.setTextFill(Color.web("#2C1A0E"));
        StackPane.setAlignment(firePitCardRank, Pos.CENTER);
        firePitCardRank.setTranslateY(calcSize(-12));

        // Suit symbol below rank
        firePitCardSuit = new Label("");
        firePitCardSuit.setFont(Font.font("Arial", FontWeight.BOLD, calcSize(24)));
        firePitCardSuit.setTextFill(Color.web("#2C1A0E"));
        StackPane.setAlignment(firePitCardSuit, Pos.CENTER);
        firePitCardSuit.setTranslateY(calcSize(22));

        // Card name at bottom
        firePitCardName = new Label("Empty");
        firePitCardName.setFont(calcFont("Georgia", FontWeight.BOLD, 10.0));
        firePitCardName.setTextFill(Color.web("#7A5A3C"));
        firePitCardName.setWrapText(true);
        firePitCardName.setTextAlignment(TextAlignment.CENTER);
        firePitCardName.setMaxWidth(calcSize(130));
        StackPane.setAlignment(firePitCardName, Pos.BOTTOM_CENTER);
        firePitCardName.setTranslateY(calcSize(-10));

        stack.getChildren().addAll(titleLbl, divider, firePitCardRank, firePitCardSuit, firePitCardName);
        return stack;
    }

    private Label createHumanLabel() {
        Label label = new Label("Human");
        label.setLayoutX(calcX(500.0));
        label.setTranslateX(calcX(120.0));
        label.setLayoutY(calcY(900.0));
        label.setPrefHeight(calcSize(99.0));
        label.setPrefWidth(calcSize(193.0));
        label.setFont(calcFont(51.0));
        label.setTextFill(Color.web("#5C3A1A"));
        return label;
    }

    private Label createCPU3Label() {
        Label label = new Label("CPU 3");
        label.setLayoutX(calcX(1080.0));
        label.setLayoutY(calcY(450));
        label.setPrefHeight(calcSize(62.0));
        label.setPrefWidth(calcSize(163.0));
        label.setRotate(90.0);
        label.setFont(calcFont(42.0));
        label.setTextFill(Color.web("#5C3A1A"));
        return label;
    }

    private Label createCPU1Label() {
        Label label = new Label("CPU 1");
        label.setLayoutX(calcX(180.0));
        label.setLayoutY(calcY(450.0));
        label.setPrefHeight(calcSize(62.0));
        label.setPrefWidth(calcSize(163.0));
        label.setRotate(90.0);
        label.setFont(calcFont(42.0));
        label.setTextFill(Color.web("#5C3A1A"));
        return label;
    }

    private Label createCPU2Label() {
        Label label = new Label("CPU 2");
        label.setLayoutX(calcX(500.0));
        label.setTranslateX(calcX(120.0));
        label.setLayoutY(calcY(20.0));
        label.setPrefHeight(calcSize(62.0));
        label.setPrefWidth(calcSize(163.0));
        label.setFont(calcFont(42.0));
        label.setTextFill(Color.web("#5C3A1A"));
        return label;
    }

    Label createRemainingCardsLabel(String s, double x, double y) {
        Label label = new Label(s);
        label.setLayoutX(x);
        label.setLayoutY(y);
        label.setFont(calcFont(25.0));
        label.setTextFill(Color.web("#5C3A1A"));
        return label;
    }

    public void initializePlayButtonHandler() {
        playButton.setOnMouseClicked(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                // Actual play logic is set in Main after game initialisation
            }
        });
    }

    /**
     * Builds an HBox of face-down cards for a CPU player.
     * @param count     initial number of cards
     * @param x         layout X
     * @param y         layout Y
     * @param vertical  if true, rotate the row 90 degrees (for side CPUs)
     */
    public javafx.scene.layout.HBox buildCpuCardRow(int count, double x, double y, boolean vertical) {
        javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(calcSize(4));
        row.setLayoutX(x);
        row.setLayoutY(y);
        if (vertical) row.setRotate(90);
        for (int i = 0; i < count; i++) {
            row.getChildren().add(buildCpuFaceDownCard(i));
        }
        return row;
    }

    /** Refreshes a CPU card row to show exactly `count` face-down cards. */
    public void updateCpuCardRow(javafx.scene.layout.HBox row, int count) {
        if (row == null) return;
        int total = row.getChildren().size();
        
        // If we need more cards than currently exist, rebuild to max (4)
        if (count > total) {
            row.getChildren().clear();
            for (int i = 0; i < 4; i++) {
                row.getChildren().add(buildCpuFaceDownCard(i));
            }
            total = 4;
        }
        
        // Show only `count` cards, hide the rest — never remove them
        for (int i = 0; i < total; i++) {
            javafx.scene.Node card = row.getChildren().get(i);
            if (i < count) {
                card.setVisible(true);
                card.setOpacity(1.0);
                card.setManaged(true);   // ← keeps space in HBox
            } else {
                card.setVisible(false);
                card.setOpacity(0);
                card.setManaged(true);   // ← KEEP managed=true so space is preserved
            }
        }
    }

    public Button createSkipButton() {
        Button button = new Button("▶▶ Skip Turn");
        button.setLayoutX(calcX(1667.0));
        button.setLayoutY(calcY(430.0));
        button.setMnemonicParsing(false);
        button.setPrefHeight(calcSize(50.0));
        button.setPrefWidth(calcSize(145.0));
        button.setFont(calcFont("Georgia", FontWeight.BOLD, 16.5));
        String base =
                "-fx-background-color: linear-gradient(to bottom, #4A2F1A, #2C1810);" +
                "-fx-text-fill: #E8D5B5; -fx-font-weight: bold; -fx-font-family: 'Georgia';" +
                "-fx-font-size: " + (int)calcSize(16.5) + "px; -fx-background-radius: 26;" +
                "-fx-border-color: #f1dbb3; -fx-border-radius: 26; -fx-border-width: 2;" +
                "-fx-effect: dropshadow(gaussian, rgba(212, 163, 115, 0.3), 10, 0, 0, 2);";
        String hover =
                "-fx-background-color: linear-gradient(to bottom, #6B4226, #3E2720);" +
                "-fx-text-fill: #FFF2DF; -fx-font-weight: bold; -fx-font-family: 'Georgia';" +
                "-fx-font-size: " + (int)calcSize(16.5) + "px; -fx-background-radius: 26;" +
                "-fx-border-color: #f1dbb3; -fx-border-radius: 26; -fx-border-width: 2.5;" +
                "-fx-effect: dropshadow(gaussian, rgba(240, 198, 138, 0.85), 25, 0, 0, 8);";
            button.setStyle(base);
            button.setOnMouseEntered(e -> button.setStyle(hover));
            button.setOnMouseExited(e -> button.setStyle(base));
            return button;
    }

    public Button createHelpButton() {
        Button button = new Button("Rules & Help");
        button.setLayoutX(calcX(1500.0));
        button.setLayoutY(calcY(30.0));
        button.setMnemonicParsing(false);
        button.setPrefHeight(calcSize(48.0));
        button.setPrefWidth(calcSize(310.0));
        button.setFont(calcFont("Georgia", FontWeight.BOLD, 15));

        String base =
                "-fx-background-color: linear-gradient(to bottom, #4A2F1A, #2C1810);" +
                "-fx-text-fill: #E8D5B5; -fx-font-weight: bold; -fx-font-family: 'Georgia';" +
                "-fx-font-size: " + (int)calcSize(18) + "px; -fx-background-radius: 26;" +
                "-fx-border-color: #f1dbb3; -fx-border-radius: 26; -fx-border-width: 2;" +
                "-fx-effect: dropshadow(gaussian, rgba(212, 163, 115, 0.3), 10, 0, 0, 2);";
        String hover =
                "-fx-background-color: linear-gradient(to bottom, #6B4226, #3E2720);" +
                "-fx-text-fill: #FFF2DF; -fx-font-weight: bold; -fx-font-family: 'Georgia';" +
                "-fx-font-size: " + (int)calcSize(18) + "px; -fx-background-radius: 26;" +
                "-fx-border-color: #f1dbb3; -fx-border-radius: 26; -fx-border-width: 2.5;" +
                "-fx-effect: dropshadow(gaussian, rgba(240, 198, 138, 0.85), 25, 0, 0, 8);";

        button.setStyle(base);
        button.setOnMouseEntered(e -> button.setStyle(hover));
        button.setOnMouseExited(e -> button.setStyle(base));

        button.setOnAction(e -> showHelpDialog());
        return button;
    }

    private void showHelpDialog() {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Jackaroo — Help");

        String desc = "Jackaroo — Quick Game Overview:\n\n" +
                "Move all 4 of your marbles clockwise: Home Zone → Base Cell → track → Safe Zone.\n" +
                "Field marbles using Ace or King. Base Cells sit 25 steps apart; Safe Zone Entry\n" +
                "cells are 2 steps before each Base and block entry when occupied.\n" +
                "8 hidden trap cells are scattered on the track — landing destroys your marble!\n\n" +
                "Ten skips the NEXT player; Queen skips a RANDOM player; King destroys all marbles\n" +
                "in its path; Seven splits 7 steps between two marbles; Five moves any marble on track.\n\n" +
                "Click 'Full Rules' below for the complete card-by-card reference.";

        Label descLbl = new Label(desc);
        descLbl.setWrapText(true);
        descLbl.setFont(calcFont("Georgia", null, 13));
        descLbl.setTextFill(Color.web("#F5E6C8"));
        descLbl.setMaxWidth(calcSize(440));

        Button rulesBtn = new Button("Full Rules");
        Button settingsBtn = new Button("Settings");
        styleWalnutButton(rulesBtn, "#5C3A1A", "#372412", "#C4A97A");
        styleWalnutButton(settingsBtn, "#3E2723", "#2A1A0E", "#8D6E63");
        rulesBtn.setPrefWidth(calcSize(170));
        settingsBtn.setPrefWidth(calcSize(140));
        rulesBtn.setOnAction(ev -> showRulesDialog());
        settingsBtn.setOnAction(ev -> showSettingsDialog());

        HBox btnRow = new HBox(calcSize(12), settingsBtn, rulesBtn);
        btnRow.setAlignment(Pos.CENTER);

        VBox v = new VBox(calcSize(16), descLbl, btnRow);
        v.setPadding(new Insets(calcSize(22), calcSize(24), calcSize(20), calcSize(24)));
        v.setAlignment(Pos.CENTER_LEFT);
        v.setBackground(new Background(new BackgroundFill(
                Color.web("#1C0F05"), new CornerRadii(calcSize(12)), Insets.EMPTY)));
        v.setBorder(new Border(new BorderStroke(
                Color.web("#C4A97A"), BorderStrokeStyle.SOLID,
                new CornerRadii(calcSize(12)), new BorderWidths(calcSize(1.5)))));
        javafx.scene.effect.DropShadow ds = new javafx.scene.effect.DropShadow();
        ds.setColor(Color.web("#000000", 0.7)); ds.setRadius(calcSize(18));
        v.setEffect(ds);

        Scene s = new Scene(v);
        s.setFill(Color.TRANSPARENT);
        dlg.setScene(s);
        dlg.setWidth(calcSize(500));
        dlg.setHeight(calcSize(360));
        dlg.showAndWait();
    }

    private void showRulesDialog() {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Full Rules — Jackaroo");

        String rules =
            "Objective: Move all 4 marbles from Home Zone → Safe Zone before opponents.\n\n" +
            "Board Zones:\n" +
            "- Home Zone: Marbles start here. Inactive — cannot move or be swapped.\n" +
            "- Base Cell: Starting position on the track (positions 0, 25, 50, 75). Fielding\n" +
            "  places your marble here. A marble on its own Base Cell blocks others passing.\n" +
            "- Safe Zone Entry: The cell 2 steps before a Base Cell. A marble here blocks\n" +
            "  all marbles from entering that player's Safe Zone.\n" +
            "- Safe Zone: 4 private cells. Entry requires exact count. Once in, cannot leave.\n" +
            "- Trap Cells: 8 hidden traps placed randomly. Landing destroys your marble and\n" +
            "  moves the trap to a new random cell.\n\n" +
            "Movement Rules:\n" +
            "- Marbles travel CLOCKWISE. Cannot bypass more than 1 marble in the path.\n" +
            "- Cannot bypass or land on your own marbles.\n" +
            "- Cannot pass a marble sitting on its own Base Cell.\n\n" +
            "Cards:\n" +
            "- Ace (1): Field a marble from Home OR move 1 step.\n" +
            "- Two–Three, Six, Eight–Nine: Standard move by rank.\n" +
            "- Four: Move 4 steps BACKWARDS.\n" +
            "- Five: Move ANY marble on track 5 steps (yours or opponent's).\n" +
            "- Seven: Move 7 steps, split between 2 of your marbles (1–6 + 6–1).\n" +
            "- Jack (11): Swap your marble with an opponent's on track (not Base cells).\n" +
            "  OR move 11 steps.\n" +
            "- Queen (12): Discard a card from a RANDOM opponent and skip their turn.\n" +
            "  OR move 12 steps.\n" +
            "- King (13): Field a marble from Home OR move 13 steps destroying ALL\n" +
            "  marbles in the path. Bypasses self-blocking, path blockage, and entry rules.\n" +
            "- Ten: Discard a card from the NEXT player and skip their turn. OR move 10.\n" +
            "- Burner (Wild): Send any opponent marble on track (not Base) back to Home.\n" +
            "- Saver (Wild): Send one of your own marbles on track to a random empty\n" +
            "  Safe Zone cell.\n\n" +
            "Winning: First player to fill all 4 Safe Zone cells wins.";

        Label rulesLbl = new Label(rules);
        rulesLbl.setWrapText(true);
        rulesLbl.setFont(calcFont("Georgia", null, 13));
        rulesLbl.setTextFill(Color.web("#F5E6C8"));
        rulesLbl.setMaxWidth(calcSize(590));

        javafx.scene.control.ScrollPane scroll = new javafx.scene.control.ScrollPane(rulesLbl);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #1C0F05; -fx-background-color: #1C0F05; -fx-border-color: transparent;");
        scroll.setPrefHeight(calcSize(430));

        VBox v = new VBox(calcSize(10), scroll);
        v.setPadding(new Insets(calcSize(18), calcSize(22), calcSize(18), calcSize(22)));
        v.setBackground(new Background(new BackgroundFill(
                Color.web("#1C0F05"), new CornerRadii(calcSize(12)), Insets.EMPTY)));
        v.setBorder(new Border(new BorderStroke(
                Color.web("#C4A97A"), BorderStrokeStyle.SOLID,
                new CornerRadii(calcSize(12)), new BorderWidths(calcSize(1.5)))));
        javafx.scene.effect.DropShadow dsr = new javafx.scene.effect.DropShadow();
        dsr.setColor(Color.web("#000000", 0.7)); dsr.setRadius(calcSize(18));
        v.setEffect(dsr);

        Scene s = new Scene(v);
        s.setFill(Color.TRANSPARENT);
        dlg.setScene(s);
        dlg.setWidth(calcSize(660));
        dlg.setHeight(calcSize(520));
        dlg.showAndWait();
    }

    private void showSettingsDialog() {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("⚙  Settings — Jackaroo");

        // ── Helpers ───────────────────────────────────────────────────────────────
        double pw = calcSize(420);

        // Themed toggle row builder
        java.util.function.BiFunction<String, Boolean, HBox> mkToggle = (text, sel) -> {
            javafx.scene.shape.Rectangle box = new javafx.scene.shape.Rectangle(calcSize(20), calcSize(20));
            box.setArcWidth(calcSize(5)); box.setArcHeight(calcSize(5));
            box.setFill(sel ? Color.web("#B8860B") : Color.web("#2C1A0A"));
            box.setStroke(Color.web("#C4A97A")); box.setStrokeWidth(calcSize(1.5));
            Label checkMark = new Label("✓");
            checkMark.setFont(Font.font("Georgia", FontWeight.BOLD, calcSize(13)));
            checkMark.setTextFill(Color.web("#FAF3E0"));
            checkMark.setVisible(sel);
            StackPane cbPane = new StackPane(box, checkMark);
            cbPane.setMinSize(calcSize(24), calcSize(24));
            cbPane.setMaxSize(calcSize(24), calcSize(24));
            cbPane.setUserData(sel);
            cbPane.setCursor(javafx.scene.Cursor.HAND);

            // Single shared toggle action — called from both the box and the label
            Runnable doToggle = () -> {
                boolean current = Boolean.TRUE.equals(cbPane.getUserData());
                boolean next = !current;
                cbPane.setUserData(next);
                box.setFill(next ? Color.web("#B8860B") : Color.web("#2C1A0A"));
                checkMark.setVisible(next);
            };

            // Clicking the box toggles; consume event so it doesn't bubble to row
            cbPane.setOnMouseClicked(e -> { doToggle.run(); e.consume(); });

            Label lbl = new Label(text);
            lbl.setFont(calcFont("Georgia", FontWeight.NORMAL, 13));
            lbl.setTextFill(Color.web("#F5E6C8"));
            lbl.setMaxWidth(pw - calcSize(60));
            lbl.setCursor(javafx.scene.Cursor.HAND);
            // Clicking the label also toggles
            lbl.setOnMouseClicked(e -> { doToggle.run(); e.consume(); });

            HBox row = new HBox(calcSize(12), cbPane, lbl);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(calcSize(3), 0, calcSize(3), 0));
            // Row itself does NOT toggle — only box and label do, to avoid double-firing
            return row;
        };

        // Section header builder
        java.util.function.Function<String, Label> mkHdr = text -> {
            Label h = new Label(text);
            h.setFont(calcFont("Georgia", FontWeight.BOLD, 11));
            h.setTextFill(Color.web("#C4A97A", 0.85));
            h.setPadding(new Insets(calcSize(8), 0, calcSize(2), 0));
            return h;
        };

        // Thin divider
        java.util.function.Supplier<javafx.scene.shape.Rectangle> mkDiv = () -> {
            javafx.scene.shape.Rectangle r = new javafx.scene.shape.Rectangle(pw, calcSize(1));
            r.setFill(Color.web("#C4A97A", 0.22)); return r;
        };

        // ── Title ─────────────────────────────────────────────────────────────────
        Label titleLbl = new Label("⚙  Game Settings");
        titleLbl.setFont(calcFont("Georgia", FontWeight.BOLD, 20));
        titleLbl.setTextFill(Color.web("#E8C97A"));
        titleLbl.setAlignment(Pos.CENTER); titleLbl.setMaxWidth(Double.MAX_VALUE);
        javafx.scene.effect.DropShadow titleGlow = new javafx.scene.effect.DropShadow();
        titleGlow.setColor(Color.web("#C4A97A", 0.6)); titleGlow.setRadius(calcSize(10));
        titleLbl.setEffect(titleGlow);

        javafx.scene.shape.Rectangle topDiv = new javafx.scene.shape.Rectangle(pw, calcSize(2));
        topDiv.setFill(Color.web("#C4A97A", 0.45));

        // ── GAMEPLAY section ─────────────────────────────────────────────────────
        HBox rowAutoSelect  = mkToggle.apply("Auto-select marble when only 1 available", Main.settingAutoSelectMarble);
        HBox rowMarblePath  = mkToggle.apply("Show marble move preview (path highlight)", Main.settingMarblePath);
        HBox rowTrapCellsEnabled = mkToggle.apply("Enable trap cells (game mechanic)", Main.settingEnableTrapCells);
        HBox rowTrapCells   = mkToggle.apply("Show trap cell locations on board", Main.settingShowTrapCells);

        // ── CPU SPEED section ─────────────────────────────────────────────────────
        // Slider: 0.3s – 30s (log-ish range)
        javafx.scene.control.Slider cpuSlider = new javafx.scene.control.Slider(0.3, 30.0, Main.settingCpuSpeedSeconds);
        cpuSlider.setShowTickLabels(false); cpuSlider.setShowTickMarks(false);
        cpuSlider.setPrefWidth(pw - calcSize(20));
        cpuSlider.setStyle(
            "-fx-control-inner-background: #2C1A0A;" +
            "-fx-background-color: transparent;" +
            "-fx-track-background: linear-gradient(to right, #B8860B, #7A5C1A);" +
            "-fx-track-color: #C4A97A;" +
            "-fx-thumb-effect: dropshadow(gaussian, rgba(200,169,122,0.6), 8, 0, 0, 2);"
        );

        Label cpuSpeedVal = new Label(String.format("%.1f s", Main.settingCpuSpeedSeconds));
        cpuSpeedVal.setFont(calcFont("Georgia", FontWeight.BOLD, 13));
        cpuSpeedVal.setTextFill(Color.web("#E8C97A"));
        cpuSpeedVal.setMinWidth(calcSize(55));

        cpuSlider.valueProperty().addListener((obs, ov, nv) -> {
            double v = nv.doubleValue();
            cpuSpeedVal.setText(String.format("%.1f s", v));
        });

        Label cpuSliderLbl = new Label("CPU turn delay:");
        cpuSliderLbl.setFont(calcFont("Georgia", FontWeight.NORMAL, 13));
        cpuSliderLbl.setTextFill(Color.web("#F5E6C8"));

        HBox cpuSpeedRow = new HBox(calcSize(10), cpuSliderLbl, cpuSpeedVal);
        cpuSpeedRow.setAlignment(Pos.CENTER_LEFT);
        HBox cpuSliderRow = new HBox(calcSize(8), cpuSlider);
        cpuSliderRow.setAlignment(Pos.CENTER_LEFT);

        // Quick presets
        Button presetFast   = makePresetBtn("Fast (0.5s)");
        Button presetNormal = makePresetBtn("Normal (1.2s)");
        Button presetSlow   = makePresetBtn("Slow (3s)");
        Button presetThink  = makePresetBtn("Thinking (10s)");
        presetFast.setOnAction(e -> cpuSlider.setValue(0.5));
        presetNormal.setOnAction(e -> cpuSlider.setValue(1.2));
        presetSlow.setOnAction(e -> cpuSlider.setValue(3.0));
        presetThink.setOnAction(e -> cpuSlider.setValue(10.0));
        HBox presetsRow = new HBox(calcSize(6), presetFast, presetNormal, presetSlow, presetThink);
        presetsRow.setAlignment(Pos.CENTER_LEFT);

        // ── DISPLAY section ───────────────────────────────────────────────────────
        HBox rowShowDeck    = mkToggle.apply("Show deck area (card back stack)", Main.settingShowDeck);
        HBox rowShowMisc    = mkToggle.apply("Show misc indicator circles", Main.settingShowMisc);
        HBox rowShowCurr    = mkToggle.apply("Show current / next player panel", Main.settingShowCurrentPlayer);

        // ── Save button ───────────────────────────────────────────────────────────
        Button saveBtn = new Button("✔  Save & Close");
        styleWalnutButton(saveBtn, "#3A5C2A", "#243815", "#7CB87A");
        saveBtn.setPrefWidth(calcSize(200)); saveBtn.setPrefHeight(calcSize(42));
        saveBtn.setOnAction(ev -> {
            Main.settingAutoSelectMarble  = readToggle(rowAutoSelect);
            Main.settingMarblePath        = readToggle(rowMarblePath);
            Main.settingEnableTrapCells   = readToggle(rowTrapCellsEnabled);
            Main.settingShowTrapCells     = readToggle(rowTrapCells);
            Main.settingShowDeck          = readToggle(rowShowDeck);
            Main.settingShowMisc          = readToggle(rowShowMisc);
            Main.settingShowCurrentPlayer = readToggle(rowShowCurr);
            Main.settingCpuSpeedSeconds   = cpuSlider.getValue();
            Main.applySettingsToUI(this);
            dlg.close();
        });

        HBox saveBtnRow = new HBox(saveBtn);
        saveBtnRow.setAlignment(Pos.CENTER);

        // ── Layout ────────────────────────────────────────────────────────────────
        VBox v = new VBox(calcSize(6),
            titleLbl, topDiv,
            mkHdr.apply("GAMEPLAY"),
            rowAutoSelect, rowMarblePath, rowTrapCellsEnabled, rowTrapCells,
            mkDiv.get(),
            mkHdr.apply("CPU TURN SPEED"),
            cpuSpeedRow, cpuSliderRow, presetsRow,
            mkDiv.get(),
            mkHdr.apply("DISPLAY — Hide / Show UI Panels"),
            rowShowDeck, rowShowMisc, rowShowCurr,
            new javafx.scene.layout.Region(),
            saveBtnRow
        );
        ((javafx.scene.layout.Region) v.getChildren().get(v.getChildren().size() - 2)).setMinHeight(calcSize(10));
        VBox.setMargin(saveBtnRow, new Insets(calcSize(8), 0, 0, 0));
        v.setAlignment(Pos.TOP_LEFT);
        v.setPadding(new Insets(calcSize(20), calcSize(24), calcSize(22), calcSize(24)));
        v.setBackground(new Background(new BackgroundFill(
                Color.web("#1C0F05"), new CornerRadii(calcSize(12)), Insets.EMPTY)));
        v.setBorder(new Border(new BorderStroke(
                Color.web("#C4A97A"), BorderStrokeStyle.SOLID,
                new CornerRadii(calcSize(12)), new BorderWidths(calcSize(1.5)))));
        javafx.scene.effect.DropShadow ds2 = new javafx.scene.effect.DropShadow();
        ds2.setColor(Color.web("#000000", 0.80)); ds2.setRadius(calcSize(22));
        v.setEffect(ds2);

        javafx.scene.control.ScrollPane scroll = new javafx.scene.control.ScrollPane(v);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #1C0F05; -fx-background-color: #1C0F05; -fx-border-color: transparent;");
        scroll.setVbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);

        Scene s = new Scene(scroll);
        s.setFill(Color.TRANSPARENT);
        dlg.setScene(s);
        dlg.setWidth(calcSize(480));
        dlg.setHeight(calcSize(620));
        dlg.showAndWait();
    }

    /** Reads the boolean state of a themed toggle row (first child = StackPane with userData Boolean). */
    private boolean readToggle(HBox row) {
        if (row.getChildren().isEmpty()) return false;
        javafx.scene.Node first = row.getChildren().get(0);
        if (first instanceof StackPane) {
            Object ud = ((StackPane) first).getUserData();
            if (ud instanceof Boolean) return (Boolean) ud;
        }
        return false;
    }

    /** Builds a small preset button for the CPU speed row. */
    private Button makePresetBtn(String label) {
        Button btn = new Button(label);
        String base =
            "-fx-background-color: linear-gradient(to bottom, #3A2510, #221408);" +
            "-fx-text-fill: #C4A97A; -fx-font-family: 'Georgia';" +
            "-fx-font-size: " + (int)calcSize(10) + "px;" +
            "-fx-background-radius: 14; -fx-border-color: #C4A97A80; -fx-border-radius: 14; -fx-border-width: 1;" +
            "-fx-padding: 3 8;";
        String hover =
            "-fx-background-color: linear-gradient(to bottom, #5C3A1A, #3A2210);" +
            "-fx-text-fill: #E8C97A; -fx-font-family: 'Georgia';" +
            "-fx-font-size: " + (int)calcSize(10) + "px;" +
            "-fx-background-radius: 14; -fx-border-color: #E8C97A; -fx-border-radius: 14; -fx-border-width: 1.5;" +
            "-fx-padding: 3 8;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
        return btn;
    }


    /**
     * Builds a face-down card panel for CPU players.
     * Shows the back of the card — the count/number is hidden.
     */
    public StackPane buildCpuFaceDownCard(int count) {
        double cardW = calcSize(50);
        double cardH = calcSize(70);

        Rectangle bg = new Rectangle(cardW, cardH);
        bg.setArcWidth(calcSize(8));
        bg.setArcHeight(calcSize(8));
        // Marble brown card back — matches the walnut board aesthetic
        javafx.scene.paint.LinearGradient backGrad = new javafx.scene.paint.LinearGradient(
                0, 0, 1, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new javafx.scene.paint.Stop(0.00, Color.web("#5C3A1A")),
                new javafx.scene.paint.Stop(0.20, Color.web("#372412")),
                new javafx.scene.paint.Stop(0.40, Color.web("#4A2C0F")),
                new javafx.scene.paint.Stop(0.60, Color.web("#382411")),
                new javafx.scene.paint.Stop(0.80, Color.web("#372412")),
                new javafx.scene.paint.Stop(1.00, Color.web("#3D2208")));
        bg.setFill(backGrad);
        bg.setStroke(Color.web("#C4A97A"));
        bg.setStrokeWidth(calcSize(1.5));

        // Inner border for card-back decoration
        Rectangle inner = new Rectangle(cardW - calcSize(8), cardH - calcSize(8));
        inner.setArcWidth(calcSize(5));
        inner.setArcHeight(calcSize(5));
        inner.setFill(Color.TRANSPARENT);
        inner.setStroke(Color.web("#E8C97A", 0.55));
        inner.setStrokeWidth(calcSize(1));

        // Diamond pattern label
        Label patternLbl = new Label("✦");
        patternLbl.setFont(Font.font("Arial", FontWeight.BOLD, calcSize(20)));
        patternLbl.setTextFill(Color.web("#E8C97A", 0.75));

        StackPane panel = new StackPane(bg, inner, patternLbl);
        panel.setPrefSize(cardW, cardH);
        panel.setMaxSize(cardW, cardH);
        return panel;
    }
    public Button createPlayButton() {
        Button button = new Button("▶ PLAY");
        button.setLayoutX(calcX(1500.0));
        button.setLayoutY(calcY(430.0));
        button.setMnemonicParsing(false);
        button.setPrefHeight(calcSize(52.0));
        button.setPrefWidth(calcSize(148.0));
        button.setFont(calcFont("Georgia", FontWeight.BOLD, 16.5));
        String base =
                "-fx-background-color: linear-gradient(to bottom, #4A2F1A, #2C1810);" +
                "-fx-text-fill: #E8D5B5; -fx-font-weight: bold; -fx-font-family: 'Georgia';" +
                "-fx-font-size: " + (int)calcSize(16.5) + "px; -fx-background-radius: 26;" +
                "-fx-border-color: #f1dbb3; -fx-border-radius: 26; -fx-border-width: 2;" +
                "-fx-effect: dropshadow(gaussian, rgba(212, 163, 115, 0.3), 10, 0, 0, 2);";
        String hover =
                "-fx-background-color: linear-gradient(to bottom, #6B4226, #3E2720);" +
                "-fx-text-fill: #FFF2DF; -fx-font-weight: bold; -fx-font-family: 'Georgia';" +
                "-fx-font-size: " + (int)calcSize(16.5) + "px; -fx-background-radius: 26;" +
                "-fx-border-color: #f1dbb3; -fx-border-radius: 26; -fx-border-width: 2.5;" +
                "-fx-effect: dropshadow(gaussian, rgba(240, 198, 138, 0.85), 25, 0, 0, 8);";
        button.setStyle(base);
        button.setOnMouseEntered(e -> button.setStyle(hover));
        button.setOnMouseExited(e -> button.setStyle(base));
        return button;
    }
    private Label createCurrentPlayerLabel() {
        Label label = new Label("Current: —");
        label.setLayoutX(calcX(1500.0));
        label.setLayoutY(calcY(150.0));
        label.setPrefHeight(calcSize(38.0));
        label.setPrefWidth(calcSize(310.0));
        label.setFont(calcFont("Georgia", FontWeight.BOLD, 16));
        label.setAlignment(Pos.CENTER_LEFT);
        label.setPadding(new Insets(calcSize(6), calcSize(12), calcSize(6), calcSize(12)));
        label.setBackground(new Background(new BackgroundFill(
                Color.web("#1A0F05", 0.80), new CornerRadii(calcSize(8)), Insets.EMPTY)));
        label.setBorder(new Border(new BorderStroke(
                Color.web("#C4A97A", 0.6), BorderStrokeStyle.SOLID,
                new CornerRadii(calcSize(8)), new BorderWidths(calcSize(1.2)))));
        label.setTextFill(Color.web("#E8D5B5"));
        return label;
    }

    private Label createNextPlayerLabel() {
        Label label = new Label("Next: —");
        label.setLayoutX(calcX(1500.0));
        label.setLayoutY(calcY(196.0));
        label.setPrefHeight(calcSize(38.0));
        label.setPrefWidth(calcSize(310.0));
        label.setFont(calcFont("Georgia", FontWeight.BOLD, 16));
        label.setAlignment(Pos.CENTER_LEFT);
        label.setPadding(new Insets(calcSize(6), calcSize(12), calcSize(6), calcSize(12)));
        label.setBackground(new Background(new BackgroundFill(
                Color.web("#1A0F05", 0.80), new CornerRadii(calcSize(8)), Insets.EMPTY)));
        label.setBorder(new Border(new BorderStroke(
                Color.web("#C4A97A", 0.4), BorderStrokeStyle.SOLID,
                new CornerRadii(calcSize(8)), new BorderWidths(calcSize(1.2)))));
        label.setTextFill(Color.web("#C4A97A"));
        return label;
    }

    private Rectangle createDeckShadow(double offX, double offY) {
        Rectangle rect = new Rectangle(
                calcX(1500.0) + offX, calcY(196.0) + offY,
                calcSize(310.0), calcSize(200.0));
        rect.setArcHeight(calcSize(14.0));
        rect.setArcWidth(calcSize(14.0));
        rect.setFill(Color.web("#5A3015"));
        rect.setStroke(Color.web("#C4A97A", 0.3));
        rect.setStrokeType(javafx.scene.shape.StrokeType.INSIDE);
        rect.setStrokeWidth(calcSize(2.0));
        rect.setOpacity(0.55);
        return rect;
    }

    private Rectangle createDeckRect() {
        Rectangle rect = new Rectangle(calcX(1500.0), calcY(200.0), calcSize(310.0), calcSize(200.0));
        rect.setArcHeight(calcSize(14.0));
        rect.setArcWidth(calcSize(14.0));
        LinearGradient deckGrad = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
        	       new Stop(0.00, Color.web("#20160d")),
        	        new Stop(0.20, Color.web("#372412")),
        	        new Stop(0.40, Color.web("#3d2b19")),
        	        new Stop(0.60, Color.web("#382411")),
        	        new Stop(0.80, Color.web("#372412")),
        	        new Stop(1.00, Color.web("#20160d")));
        rect.setFill(deckGrad);
        rect.setStroke(Color.web("#2c211b"));
        rect.setStrokeType(javafx.scene.shape.StrokeType.INSIDE);
        rect.setStrokeWidth(calcSize(4.0));
        
       
        
        return rect;
    }

    private Label createDeckLabel() {
        Label label = new Label("♠ ♥\n Deck \n♣ ♦");
        label.setLayoutX(calcX(1500.0));
        label.setLayoutY(calcY(200.0));
        label.setPrefWidth(calcSize(310.0));
        label.setPrefHeight(calcSize(130.0));
        label.setAlignment(Pos.CENTER);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setTextFill(Color.web("#E8D5B5"));
        label.setFont(calcFont("Georgia", FontWeight.BOLD, 18.0));
        label.setWrapText(true);
        
        // Subtle glow
        javafx.scene.effect.DropShadow subtleGlow = new javafx.scene.effect.DropShadow();
        subtleGlow.setColor(Color.web("#E8BA86", 0.5));
        subtleGlow.setRadius(calcSize(15));
        subtleGlow.setSpread(0.2);
        subtleGlow.setOffsetX(0);
        subtleGlow.setOffsetY(0);
        label.setEffect(subtleGlow);
        
        return label;
    }

    private Label createDeckRemainingLabel() {
        Label label = new Label("Rem: x");
        label.setLayoutX(calcX(1500.0));
        label.setLayoutY(calcY(300.0));
        label.setPrefWidth(calcSize(310.0));
        label.setPrefHeight(calcSize(60.0));
        label.setAlignment(Pos.CENTER);
        label.setMinWidth(calcSize(145.0));
        label.setTextFill(Color.web("#E8D5B5"));
        label.setFont(calcFont("Georgia", FontWeight.BOLD, 18.0));
        
        // Subtle glow
        javafx.scene.effect.DropShadow subtleGlow = new javafx.scene.effect.DropShadow();
        subtleGlow.setColor(Color.web("#E8BA86", 0.45));
        subtleGlow.setRadius(calcSize(12));
        subtleGlow.setSpread(0.15);
        subtleGlow.setOffsetX(0);
        subtleGlow.setOffsetY(0);
        label.setEffect(subtleGlow);
        
        return label;
    }
    private Label createMiscLabel() {
        Label label = new Label("⚡ Welcome! Select a card and marble.");
        label.setLayoutX(calcX(1500.0));
        label.setLayoutY(calcY(92.0));
        label.setFont(calcFont("Georgia", FontWeight.BOLD, 12.0));
        label.setAlignment(Pos.CENTER_LEFT);
        label.setWrapText(true);
        label.setPrefWidth(calcSize(310));
        label.setPrefHeight(calcSize(50));
        label.setPadding(new Insets(calcSize(8), calcSize(12), calcSize(8), calcSize(12)));
        label.setBackground(new Background(new BackgroundFill(
                Color.web("#1A0F05", 0.85), new CornerRadii(calcSize(10)), Insets.EMPTY)));
        label.setBorder(new Border(new BorderStroke(
                Color.web("#C4A97A", 0.6), BorderStrokeStyle.SOLID,
                new CornerRadii(calcSize(10)), new BorderWidths(calcSize(1.8)))));
        label.setTextFill(Color.web("#E8D5B5"));
        
        javafx.scene.effect.DropShadow deckStyleGlow = new javafx.scene.effect.DropShadow();
        deckStyleGlow.setColor(Color.web("#E8BA86", 0.5));
        deckStyleGlow.setRadius(calcSize(14));
        deckStyleGlow.setSpread(0.2);
        deckStyleGlow.setOffsetX(0);
        deckStyleGlow.setOffsetY(0);
        label.setEffect(deckStyleGlow);
        
        return label;
    }

    private Circle createMiscCircle(double x, double y) {
        Circle circle = new Circle(x, y, calcSize(16.0), Color.web("#fad180"));
        circle.setStroke(Color.BLACK);
        circle.setStrokeType(javafx.scene.shape.StrokeType.INSIDE);
        return circle;
    }

    // Alert helpers (kept exactly as before)
    public void displayAlert1(String title, String message) throws FileNotFoundException {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setResizable(false);
        VBox layout = new VBox(18);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(28, 34, 24, 34));
        layout.setBackground(new Background(new BackgroundFill(Color.web("#1C0F05"), new CornerRadii(14), Insets.EMPTY)));
        layout.setBorder(new Border(new BorderStroke(Color.web("#C4A97A"), BorderStrokeStyle.SOLID, new CornerRadii(14), new BorderWidths(2))));
        javafx.scene.effect.DropShadow ds = new javafx.scene.effect.DropShadow();
        ds.setColor(Color.web("#000000", 0.7)); ds.setRadius(18); ds.setOffsetX(3); ds.setOffsetY(3);
        layout.setEffect(ds);
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web("#E8C97A"));
        Label messageLabel = new Label(message);
        messageLabel.setFont(Font.font("Georgia", 14));
        messageLabel.setTextFill(Color.web("#F5E6C8"));
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(380);
        messageLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        Button okButton = new Button("OK");
        okButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #4A2F1A, #2C1810);" +
            "-fx-text-fill: #E8D5B5; -fx-font-weight: bold; -fx-font-family: 'Georgia'; -fx-font-size: 13px;" +
            "-fx-background-radius: 20; -fx-border-color: #f1dbb3; -fx-border-radius: 20; -fx-border-width: 1.5;" +
            "-fx-padding: 6 28; -fx-effect: dropshadow(gaussian, rgba(212,163,115,0.3), 8, 0, 0, 2);");
        okButton.setOnMouseEntered(e -> okButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #6B4226, #3E2720);" +
            "-fx-text-fill: #FFF2DF; -fx-font-weight: bold; -fx-font-family: 'Georgia'; -fx-font-size: 13px;" +
            "-fx-background-radius: 20; -fx-border-color: #f1dbb3; -fx-border-radius: 20; -fx-border-width: 2;" +
            "-fx-padding: 6 28; -fx-effect: dropshadow(gaussian, rgba(240,198,138,0.8), 16, 0, 0, 5);"));
        okButton.setOnMouseExited(e -> okButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #4A2F1A, #2C1810);" +
            "-fx-text-fill: #E8D5B5; -fx-font-weight: bold; -fx-font-family: 'Georgia'; -fx-font-size: 13px;" +
            "-fx-background-radius: 20; -fx-border-color: #f1dbb3; -fx-border-radius: 20; -fx-border-width: 1.5;" +
            "-fx-padding: 6 28; -fx-effect: dropshadow(gaussian, rgba(212,163,115,0.3), 8, 0, 0, 2);"));
        okButton.setOnAction(e -> window.close());
        layout.getChildren().addAll(titleLabel, messageLabel, okButton);
        Scene scene = new Scene(layout);
        scene.setFill(Color.TRANSPARENT);
        window.setScene(scene);
        window.setWidth(460);
        window.setHeight(220);
        window.showAndWait();
    }

    public void displayAlert2(String title, String message) {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setResizable(false);
        VBox layout = new VBox(18);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(28, 34, 24, 34));
        layout.setBackground(new Background(new BackgroundFill(Color.web("#1C0F05"), new CornerRadii(14), Insets.EMPTY)));
        layout.setBorder(new Border(new BorderStroke(Color.web("#C4A97A"), BorderStrokeStyle.SOLID, new CornerRadii(14), new BorderWidths(2))));
        javafx.scene.effect.DropShadow ds = new javafx.scene.effect.DropShadow();
        ds.setColor(Color.web("#000000", 0.7)); ds.setRadius(18); ds.setOffsetX(3); ds.setOffsetY(3);
        layout.setEffect(ds);
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web("#E8C97A"));
        Label messageLabel = new Label(message);
        messageLabel.setFont(Font.font("Georgia", 14));
        messageLabel.setTextFill(Color.web("#F5E6C8"));
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(380);
        messageLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        Button okButton = new Button("OK");
        okButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #4A2F1A, #2C1810);" +
            "-fx-text-fill: #E8D5B5; -fx-font-weight: bold; -fx-font-family: 'Georgia'; -fx-font-size: 13px;" +
            "-fx-background-radius: 20; -fx-border-color: #f1dbb3; -fx-border-radius: 20; -fx-border-width: 1.5;" +
            "-fx-padding: 6 28; -fx-effect: dropshadow(gaussian, rgba(212,163,115,0.3), 8, 0, 0, 2);");
        okButton.setOnMouseEntered(e -> okButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #6B4226, #3E2720);" +
            "-fx-text-fill: #FFF2DF; -fx-font-weight: bold; -fx-font-family: 'Georgia'; -fx-font-size: 13px;" +
            "-fx-background-radius: 20; -fx-border-color: #f1dbb3; -fx-border-radius: 20; -fx-border-width: 2;" +
            "-fx-padding: 6 28; -fx-effect: dropshadow(gaussian, rgba(240,198,138,0.8), 16, 0, 0, 5);"));
        okButton.setOnMouseExited(e -> okButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #4A2F1A, #2C1810);" +
            "-fx-text-fill: #E8D5B5; -fx-font-weight: bold; -fx-font-family: 'Georgia'; -fx-font-size: 13px;" +
            "-fx-background-radius: 20; -fx-border-color: #f1dbb3; -fx-border-radius: 20; -fx-border-width: 1.5;" +
            "-fx-padding: 6 28; -fx-effect: dropshadow(gaussian, rgba(212,163,115,0.3), 8, 0, 0, 2);"));
        okButton.setOnAction(e -> window.close());
        layout.getChildren().addAll(titleLabel, messageLabel, okButton);
        Scene scene = new Scene(layout);
        scene.setFill(Color.TRANSPARENT);
        window.setScene(scene);
        window.setWidth(460);
        window.setHeight(220);
        window.showAndWait();
    }
    


    public int selectedCount;
}