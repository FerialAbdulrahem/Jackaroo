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
            double cardX = calcX(1100.0 + i * 115.0); // space them 115 apart horizontally
            double cardY = calcY(750.0);

            cardPanels[i].setLayoutX(cardX);
            cardPanels[i].setLayoutY(cardY);

            gameBoard.getChildren().add(cardPanels[i]);
        }
    }
    /**
     * Builds a single visual card StackPane for slot index.
     */
    public StackPane buildCardPanel(int index) {
        double cardW = calcSize(102);
        double cardH = calcSize(148);

        Rectangle bg = new Rectangle(cardW, cardH);
        bg.setArcWidth(calcSize(12));
        bg.setArcHeight(calcSize(12));
        javafx.scene.paint.LinearGradient cardGrad = new javafx.scene.paint.LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0.00, Color.web("#F7EFE2")),
            new Stop(0.35, Color.web("#E9D9C8")),
            new Stop(0.70, Color.web("#CDB89A")),
            new Stop(1.00, Color.web("#B89B77")));
        bg.setFill(cardGrad);
        bg.setStroke(Color.web("#C4A97A"));
        bg.setStrokeWidth(calcSize(1.5));

        // Subtle inner border for depth
        Rectangle inner = new Rectangle(cardW - calcSize(10), cardH - calcSize(12));
        inner.setArcWidth(calcSize(10)); inner.setArcHeight(calcSize(10));
        inner.setFill(Color.TRANSPARENT);
        inner.setStroke(Color.web("#F7EFE2", 0.18));
        inner.setStrokeWidth(calcSize(1));
        StackPane.setAlignment(inner, Pos.CENTER);

        // Top-left rank label
        Label rankLabel = new Label("?");
        rankLabel.setFont(calcFont("Georgia", FontWeight.BOLD, 18));
        rankLabel.setTextFill(Color.web("#5B3A23"));
        StackPane.setAlignment(rankLabel, Pos.TOP_LEFT);
        rankLabel.setTranslateX(calcSize(8));
        rankLabel.setTranslateY(calcSize(6));

        // Centre suit symbol (large)
        Label suitLabel = new Label("♠");
        suitLabel.setFont(Font.font("Georgia", FontWeight.BOLD, calcSize(36)));
        suitLabel.setTextFill(Color.web("#5B3A23"));
        StackPane.setAlignment(suitLabel, Pos.CENTER);
        suitLabel.setTranslateY(calcSize(-10));

        // Description at bottom
        Label descLabel = new Label("");
        descLabel.setFont(calcFont("Georgia", FontWeight.NORMAL, 10));
        descLabel.setTextFill(Color.web("#3E2B23"));
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(cardW - calcSize(10));
        descLabel.setTextAlignment(TextAlignment.CENTER);
        StackPane.setAlignment(descLabel, Pos.BOTTOM_CENTER);
        descLabel.setTranslateY(calcSize(-6));

        // "Card N" placeholder at centre-top
        Label cardNumLabel = new Label("Card " + (index + 1));
        cardNumLabel.setFont(calcFont("Georgia", FontWeight.BOLD, 11));
        cardNumLabel.setTextFill(Color.web("#7A5A43"));
        StackPane.setAlignment(cardNumLabel, Pos.TOP_CENTER);
        cardNumLabel.setTranslateY(calcSize(6));

        // Compose panel with inner border on top
        StackPane panel = new StackPane(bg, inner, rankLabel, suitLabel, descLabel, cardNumLabel);
        panel.setPrefSize(cardW, cardH);
        panel.setMaxSize(cardW, cardH);

        // Store sub-labels as userData list so Main can update them
        panel.setUserData(new Label[]{rankLabel, suitLabel, descLabel, cardNumLabel});

        // Visual selection effect: clicking the panel selects the card
        final int idx = index;
        panel.setOnMouseClicked(e -> {
            if (!panel.isVisible()) return;
            boolean wasSelected = checkBoxes[idx].isSelected();
            // Deselect all others
            for (int k = 0; k < 4; k++) {
                checkBoxes[k].setSelected(false);
                highlightCard(k, false);
            }
            // Toggle this one
            if (!wasSelected) {
                checkBoxes[idx].setSelected(true);
                highlightCard(idx, true);
            }
        });

        // Hover effect
        panel.setOnMouseEntered(e -> {
            if (panel.isVisible() && !checkBoxes[idx].isSelected()) {
                bg.setStroke(Color.web("#E8C97A"));
                bg.setStrokeWidth(calcSize(2.5));
                bg.setEffect(new javafx.scene.effect.DropShadow(calcSize(8), Color.web("#000000", 0.45)));
            }
        });
        panel.setOnMouseExited(e -> {
            if (!checkBoxes[idx].isSelected()) {
                bg.setStroke(Color.web("#C4A97A"));
                bg.setStrokeWidth(calcSize(1.5));
                bg.setEffect(null);
            }
        });

        return panel;
    }

    /** Highlights or un-highlights the card panel at the given index. */
    public void highlightCard(int index, boolean selected) {
        if (cardPanels[index] == null) return;
        Rectangle bg = (Rectangle) cardPanels[index].getChildren().get(0);
        if (selected) {
            bg.setStroke(Color.GOLD);
            bg.setStrokeWidth(calcSize(4));
            bg.setFill(Color.web("#fffde7"));
        } else {
            bg.setStroke(Color.web("#B8A080"));
            bg.setStrokeWidth(calcSize(1.5));
            bg.setFill(Color.web("#FEFDF8"));
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
		labels[3].setTextFill(suitColor.equals(Color.RED) ? Color.CRIMSON : Color.DARKSLATEBLUE);
		
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
        actionPanel.setLayoutY(calcY(420.0));
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
        discardButton = new Button("\uD83D\uDDD1  Discard Card");
        styleWalnutButton(discardButton, "#7B1F1F", "#4A0F0F", "#C8625A");
        discardButton.setPrefHeight(calcSize(44));
        discardButton.setPrefWidth(calcSize(270));
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

        // ✂ Apply Split button
    splitButton = new Button("✂  Apply Split");
    String splitBase =
        "-fx-background-color: linear-gradient(to bottom, #1A4A2A, #0A2010);" +
        "-fx-text-fill: #C8F0D0;" +
        "-fx-font-weight: bold;" +
        "-fx-font-family: 'Georgia';" +
        "-fx-font-size: " + (int) calcSize(14) + "px;" +
        "-fx-background-radius: 14;" +
        "-fx-border-color: #4CAF50;" +
        "-fx-border-radius: 14;" +
        "-fx-border-width: 2;" +
        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.65), 12, 0, 2, 5);";
    String splitHover =
        "-fx-background-color: linear-gradient(to bottom, #4CAF50, #2E7D32);" +
        "-fx-text-fill: #FFFDE7;" +
        "-fx-font-weight: bold;" +
        "-fx-font-family: 'Georgia';" +
        "-fx-font-size: " + (int) calcSize(14) + "px;" +
        "-fx-background-radius: 14;" +
        "-fx-border-color: #A5D6A7;" +
        "-fx-border-radius: 14;" +
        "-fx-border-width: 2;" +
        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.75), 16, 0, 2, 6);";
    splitButton.setStyle(splitBase);
    splitButton.setOnMouseEntered(e -> splitButton.setStyle(splitHover));
    splitButton.setOnMouseExited(e -> splitButton.setStyle(splitBase));
    splitButton.setPrefHeight(calcSize(44));
    splitButton.setPrefWidth(calcSize(160));

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
                Color.web("#1A3A5C"), new CornerRadii(cSize / 2), Insets.EMPTY)));
        circle.setBorder(new Border(new BorderStroke(
                Color.web("#5A9BD4"), BorderStrokeStyle.SOLID,
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
                discardButton.setText("\uD83D\uDDD1  Discard Opponent\u2019s Card (Ten)");
                discardButton.setVisible(true);
                discardButton.setManaged(true);
                actionPanel.setVisible(true);
                break;
            case "QUEEN":
                discardButton.setText("\uD83D\uDDD1  Discard Opponent\u2019s Card (Queen)");
                discardButton.setVisible(true);
                discardButton.setManaged(true);
                actionPanel.setVisible(true);
                break;
            case "DISCARD_ANY":
                discardButton.setText("\uD83D\uDDD1  Discard This Card");
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
        actionPanel.setLayoutY(calcY(420.0));

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
                cardPanels[i].setLayoutX(calcX(1100.0 + i * 115.0));
                cardPanels[i].setLayoutY(calcY(750.0));
            }
        }

        playButton.setLayoutX(calcX(1500.0));
        playButton.setLayoutY(calcY(350.0));
        playButton.setPrefHeight(calcSize(50.0));
        playButton.setPrefWidth(calcSize(145.0));
        playButton.setFont(calcFont("Georgia", FontWeight.BOLD, 20.0));

        if (skipButton != null) {
            skipButton.setLayoutX(calcX(1667.0));
            skipButton.setLayoutY(calcY(350.0));
            skipButton.setPrefHeight(calcSize(50.0));
            skipButton.setPrefWidth(calcSize(145.0));
            skipButton.setFont(calcFont("Georgia", FontWeight.BOLD, 18.0));
        }

        if (cpu1CardRow != null) { cpu1CardRow.setLayoutX(calcX(55.0));   cpu1CardRow.setLayoutY(calcY(450.0)); }
        if (cpu2CardRow != null) { cpu2CardRow.setLayoutX(calcX(595.0));  cpu2CardRow.setLayoutY(calcY(-50.0)); }
        if (cpu3CardRow != null) { cpu3CardRow.setLayoutX(calcX(1130.0)); cpu3CardRow.setLayoutY(calcY(450.0)); }

        currentPlayerLabel.setLayoutX(calcX(1250.0));
        currentPlayerLabel.setLayoutY(calcY(150.0));
        currentPlayerLabel.setFont(calcFont("Georgia", FontWeight.BOLD, 22));

        nextPlayerLabel.setLayoutX(calcX(1250.0));
        nextPlayerLabel.setLayoutY(calcY(200.0));
        nextPlayerLabel.setFont(calcFont("Georgia", FontWeight.BOLD, 22));

        if (deckShadow1 != null) {
            deckShadow1.setX(calcX(1500.0) + calcSize(6));
            deckShadow1.setY(calcY(76.0) + calcSize(6));
            deckShadow1.setWidth(calcSize(310.0));
            deckShadow1.setHeight(calcSize(200.0));
        }
        if (deckShadow2 != null) {
            deckShadow2.setX(calcX(1500.0) + calcSize(3));
            deckShadow2.setY(calcY(130.0) + calcSize(3));
            deckShadow2.setWidth(calcSize(310.0));
            deckShadow2.setHeight(calcSize(200.0));
        }

        deckRect.setX(calcX(1500.0));
        deckRect.setY(calcY(130.0));
        deckRect.setWidth(calcSize(310.0));
        deckRect.setHeight(calcSize(200.0));

        deckLabel.setLayoutX(calcX(1500.0));
        deckLabel.setLayoutY(calcY(130.0));
        deckLabel.setPrefWidth(calcSize(310.0));
        deckLabel.setPrefHeight(calcSize(130.0));
        deckLabel.setFont(calcFont("Georgia", FontWeight.BOLD, 18.0));

        deckRemainingLabel.setLayoutX(calcX(1500.0));
        deckRemainingLabel.setLayoutY(calcY(230.0));
        deckRemainingLabel.setPrefWidth(calcSize(310.0));
        deckRemainingLabel.setFont(calcFont("Georgia", FontWeight.BOLD, 18.0));

        miscLabel.setLayoutX(calcX(1500.0));
        miscLabel.setLayoutY(calcY(30.0));
        miscLabel.setFont(calcFont("Georgia", FontWeight.BOLD, 14.0));  // ← updated
        miscLabel.setPrefWidth(calcSize(310));
        miscLabel.setPrefHeight(calcSize(90));
        miscLabel.setPadding(new Insets(calcSize(12), calcSize(14), calcSize(12), calcSize(14)));

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
        Button button = new Button(" ⏭ Skip Turn");
        button.setLayoutX(calcX(1667.0));
        button.setLayoutY(calcY(350.0));
        button.setMnemonicParsing(false);
        button.setPrefHeight(calcSize(50.0));
        button.setPrefWidth(calcSize(145.0));
        button.setFont(calcFont("Georgia", FontWeight.BOLD, 18));

        String baseStyle =
        	    "-fx-background-color: linear-gradient(to bottom, #38200a, #3E2723);" +
        	    "-fx-text-fill: #FAF3E0;" +
        	    "-fx-font-weight: bold;" +
        	    "-fx-font-family: 'Georgia';" +
        	    "-fx-font-size: " + (int) calcSize(18) + "px;" +
        	    "-fx-background-radius: 14;" +
        	    "-fx-border-color: #372412;" +
        	    "-fx-border-radius: 14;" +
        	    "-fx-border-width: 2;" +
        	    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.65), 12, 0, 2, 5);";

        String hoverStyle =
        	    "-fx-background-color: linear-gradient(to bottom, #8D6E63, #4E342E);" +
        	    "-fx-text-fill: #FFFDE7;" +
        	    "-fx-font-weight: bold;" +
        	    "-fx-font-family: 'Georgia';" +
        	    "-fx-font-size: " + (int) calcSize(18) + "px;" +
        	    "-fx-background-radius: 14;" +
        	    "-fx-border-color: #D7CCC8;" +
        	    "-fx-border-radius: 14;" +
        	    "-fx-border-width: 2;" +
        	    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.75), 14, 0, 2, 6);";
        	
        button.setStyle(baseStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));

        return button;
    }

    public Button createHelpButton() {
        Button button = new Button("❓ Help");
        button.setLayoutX(calcX(1500.0));
        button.setLayoutY(calcY(280.0));
        button.setMnemonicParsing(false);
        button.setPrefHeight(calcSize(40.0));
        button.setPrefWidth(calcSize(120.0));
        button.setFont(calcFont("Georgia", FontWeight.BOLD, 14));

        String baseStyle =
                "-fx-background-color: linear-gradient(to bottom, #2E7D32, #1B5E20);" +
                "-fx-text-fill: #FAF3E0;" +
                "-fx-font-weight: bold;" +
                "-fx-font-family: 'Georgia';" +
                "-fx-font-size: " + (int) calcSize(14) + "px;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #2E7D32;" +
                "-fx-border-radius: 10;" +
                "-fx-border-width: 1.5;";

        String hoverStyle =
                "-fx-background-color: linear-gradient(to bottom, #66BB6A, #2E7D32);" +
                "-fx-text-fill: #FFFDE7;" +
                "-fx-font-weight: bold;" +
                "-fx-font-family: 'Georgia';" +
                "-fx-font-size: " + (int) calcSize(14) + "px;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #D7CCC8;" +
                "-fx-border-radius: 10;" +
                "-fx-border-width: 1.5;";

        button.setStyle(baseStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));

        button.setOnAction(e -> showHelpDialog());
        return button;
    }

    private void showHelpDialog() {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("About Jackaroo — Overview & Help");

        String desc = "Jackaroo — Quick Game Overview:\n\n" +
                "Players take turns playing cards to move marbles around the board. " +
                "Special cards have effects: Ten/Queen may discard a card from another player and skip their turn; " +
                "Seven can split 7 steps between two marbles; Ace/King can field marbles from Home.\n\n" +
                "Use the Play and Skip buttons to play or discard. The board shows your marbles (bottom-left) and CPUs around the board.\n\n" +
                "Click 'Rules' below for full rules and card reference.";

        Label descLbl = new Label(desc);
        descLbl.setWrapText(true);
        descLbl.setFont(calcFont(14));
        descLbl.setMaxWidth(calcSize(420));

        Button rulesBtn = new Button("📜 Rules");
        Button settingsBtn = new Button("⚙ Settings");
        styleWalnutButton(rulesBtn, "#2B3A67", "#14213D", "#5C6BC0");
        styleWalnutButton(settingsBtn, "#5D4037", "#3E2723", "#372412");
        rulesBtn.setOnAction(ev -> showRulesDialog());
        settingsBtn.setOnAction(ev -> showSettingsDialog());

        HBox btnRow = new HBox(calcSize(12), settingsBtn, rulesBtn);
        btnRow.setAlignment(Pos.CENTER);

        VBox v = new VBox(calcSize(12), descLbl, btnRow);
        v.setPadding(new Insets(calcSize(12)));
        v.setAlignment(Pos.CENTER_LEFT);

        Scene s = new Scene(v);
        dlg.setScene(s);
        dlg.setWidth(calcSize(480));
        dlg.setHeight(calcSize(320));
        dlg.showAndWait();
    }

    private void showRulesDialog() {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Full Rules — Jackaroo");

        String rules =
            "Basic Rules:\n" +
            "- Each player has 4 marbles in Home. Goal: move all marbles around the track into your Safe Zone.\n" +
            "- Play cards to move your marbles or use special actions (see card reference).\n\n" +
            "Card highlights:\n" +
            "- Ace: Field a marble from Home or move 1 step.\n" +
            "- Seven: Split 7 steps between two marbles (select 2 marbles on track).\n" +
            "- Ten: Discard an opponent's card and skip their next turn, or move 10 steps.\n" +
            "- Queen: Discard a random opponent card and skip their next turn, or move 12 steps.\n" +
            "- Jack: Swap or move 11 steps.\n" +
            "- Burners/Savers: Special wild cards (see in-game card descriptions).\n\n" +
            "Gameplay:\n" +
            "- Select a card from your hand, select marbles (one or two for Seven), then press Play.\n" +
            "- If you cannot play, use Skip to discard a card and end your turn.\n" +
            "- The game automatically advances CPU turns after you play.\n\n" +
            "This in-game Rules panel gives a quick reference. For the full card CSV use the project files.";

        Label rulesLbl = new Label(rules);
        rulesLbl.setWrapText(true);
        rulesLbl.setFont(calcFont(13));
        rulesLbl.setMaxWidth(calcSize(560));

        VBox v = new VBox(calcSize(10), rulesLbl);
        v.setPadding(new Insets(calcSize(12)));

        Scene s = new Scene(v);
        dlg.setScene(s);
        dlg.setWidth(calcSize(640));
        dlg.setHeight(calcSize(480));
        dlg.showAndWait();
    }

    private void showSettingsDialog() {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Settings");

        CheckBox hints = new CheckBox("Enable move hints (visual)");
        CheckBox fastCPU = new CheckBox("Faster CPU turns");
        hints.setSelected(true);
        fastCPU.setSelected(false);

        VBox v = new VBox(calcSize(12), hints, fastCPU);
        v.setPadding(new Insets(calcSize(12)));

        Scene s = new Scene(v);
        dlg.setScene(s);
        dlg.setWidth(calcSize(360));
        dlg.setHeight(calcSize(220));
        dlg.showAndWait();
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
        Button button = new Button("▶  Play");
        button.setLayoutX(calcX(1500.0));
        button.setLayoutY(calcY(350.0));
        button.setMnemonicParsing(false);
        button.setPrefHeight(calcSize(50.0));
        button.setPrefWidth(calcSize(145.0));
        button.setFont(calcFont("Georgia", FontWeight.BOLD, 20));

        String baseStyle =
        	    "-fx-background-color: linear-gradient(to bottom, #5D4037, #3E2723);" +
        	    	    "-fx-text-fill: #FAF3E0;" +
        	    	    "-fx-font-weight: bold;" +
        	    	    "-fx-font-family: 'Georgia';" +
        	    	    "-fx-font-size: " + (int) calcSize(20) + "px;" +
        	    	    "-fx-background-radius: 14;" +
        	    	    "-fx-border-color: #372412;" +
        	    	    "-fx-border-radius: 14;" +
        	    	    "-fx-border-width: 2;" +
        	    	    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.65), 12, 0, 2, 5);";

        String hoverStyle =
        	    "-fx-background-color: linear-gradient(to bottom, #8D6E63, #4E342E);" +
        	    "-fx-text-fill: #FFFDE7;" +
        	    "-fx-font-weight: bold;" +
        	    "-fx-font-family: 'Georgia';" +
        	    "-fx-font-size: " + (int) calcSize(20) + "px;" +
        	    "-fx-background-radius: 14;" +
        	    "-fx-border-color: #D7CCC8;" +
        	    "-fx-border-radius: 14;" +
        	    "-fx-border-width: 2;" +
        	    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.75), 14, 0, 2, 6);";

        button.setStyle(baseStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));

        return button;
    }
    private Label createCurrentPlayerLabel() {
        Label label = new Label("Current player is:= x");
        label.setLayoutX(calcX(1210.0));
        label.setLayoutY(calcY(30.0));
        label.setPrefHeight(calcSize(46.0));
        label.setPrefWidth(calcSize(420.0));
        label.setFont(calcFont("Georgia", FontWeight.BOLD, 22));
        label.setAlignment(Pos.CENTER_LEFT);
        return label;
    }

    private Label createNextPlayerLabel() {
        Label label = new Label("Next player is:= x");
        label.setLayoutX(calcX(1210.0));
        label.setLayoutY(calcY(76.0));
        label.setPrefHeight(calcSize(46.0));
        label.setPrefWidth(calcSize(420.0));
        label.setFont(calcFont("Georgia", FontWeight.BOLD, 22));
        label.setAlignment(Pos.CENTER_LEFT);
        return label;
    }

    private Rectangle createDeckShadow(double offX, double offY) {
        Rectangle rect = new Rectangle(
                calcX(1500.0) + offX, calcY(130.0) + offY,
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
    	Rectangle rect = new Rectangle(calcX(1500.0), calcY(130.0), calcSize(310.0), calcSize(200.0));
        rect.setArcHeight(calcSize(14.0));
        rect.setArcWidth(calcSize(14.0));
        LinearGradient deckGrad = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0.00, Color.web("#5C3A1A")),
            new Stop(0.20, Color.web("#372412")),
            new Stop(0.40, Color.web("#4A2C0F")),
            new Stop(0.60, Color.web("#382411")),
            new Stop(0.80, Color.web("#372412")),
            new Stop(1.00, Color.web("#3D2208")));
        rect.setFill(deckGrad);
        rect.setStroke(Color.web("#816140"));
        rect.setStrokeType(javafx.scene.shape.StrokeType.INSIDE);
        rect.setStrokeWidth(calcSize(4.0));
        javafx.scene.effect.DropShadow deckShadow = new javafx.scene.effect.DropShadow();
        deckShadow.setColor(Color.web("#1A0A00", 0.7));
        deckShadow.setRadius(calcSize(14));
        deckShadow.setOffsetX(calcSize(3));
        deckShadow.setOffsetY(calcSize(3));
        rect.setEffect(deckShadow);
        return rect;
    }

    private Label createDeckLabel() {
        Label label = new Label("♠ ♥\n Deck \n♣ ♦");
        label.setLayoutX(calcX(1500.0));
        label.setLayoutY(calcY(130.0));
        label.setPrefWidth(calcSize(310.0));
        label.setPrefHeight(calcSize(130.0));
        label.setAlignment(Pos.CENTER);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setTextFill(Color.web("#E8C97A"));
        label.setFont(calcFont("Georgia", FontWeight.BOLD, 18.0));
        label.setWrapText(true);
        return label;
    }

    private Label createDeckRemainingLabel() {
        Label label = new Label("Rem: x");
        label.setLayoutX(calcX(1500.0));
        label.setLayoutY(calcY(230.0));
        label.setPrefWidth(calcSize(310.0));
        label.setPrefHeight(calcSize(60.0));
        label.setAlignment(Pos.CENTER);
        label.setMinWidth(calcSize(145.0));
        label.setTextFill(Color.web("#C4A97A"));
        label.setFont(calcFont("Georgia", FontWeight.BOLD, 18.0));
        return label;
    }

    private Label createMiscLabel() {
        Label label = new Label(" Welcome! Select a card and marble to play.");
        label.setLayoutX(calcX(1500.0));
        label.setLayoutY(calcY(30.0));
        label.setFont(calcFont("Georgia", FontWeight.BOLD, 14.0));
        label.setAlignment(Pos.CENTER_LEFT);
        label.setWrapText(true);
        label.setPrefWidth(calcSize(310));
        label.setPrefHeight(calcSize(90));
        label.setPadding(new Insets(calcSize(12), calcSize(14), calcSize(12), calcSize(14)));

        // Layered background — dark inner with subtle warm tint
        label.setBackground(new Background(new BackgroundFill(
                Color.web("#372412", 0.96), new CornerRadii(calcSize(12)), Insets.EMPTY)));

        // Double border effect — golden outer
        label.setBorder(new Border(
            new BorderStroke(
                Color.web("#5C3A1A"),
                BorderStrokeStyle.SOLID,
                new CornerRadii(calcSize(12)),
                new BorderWidths(calcSize(2.5))
            )
        ));

        label.setTextFill(Color.web("#F5E6C8"));

        // Stronger shadow for depth
        javafx.scene.effect.DropShadow notifShadow = new javafx.scene.effect.DropShadow();
        notifShadow.setColor(Color.web("#000000", 0.75));
        notifShadow.setRadius(calcSize(18));
        notifShadow.setOffsetX(calcSize(3));
        notifShadow.setOffsetY(calcSize(4));
        notifShadow.setSpread(0.1);

        // Inner glow effect on top of shadow
        javafx.scene.effect.InnerShadow innerGlow = new javafx.scene.effect.InnerShadow();
        innerGlow.setColor(Color.web("#C4A97A", 0.15));
        innerGlow.setRadius(calcSize(10));
        innerGlow.setInput(notifShadow);

        label.setEffect(innerGlow);

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
        VBox layout = new VBox(calcSize(18));
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(calcSize(30), calcSize(36), calcSize(28), calcSize(36)));
        layout.setBackground(new Background(new BackgroundFill(Color.web("#1C0F05"), new CornerRadii(calcSize(14)), Insets.EMPTY)));
        layout.setBorder(new Border(new BorderStroke(Color.web("#C4A97A"), BorderStrokeStyle.SOLID, new CornerRadii(calcSize(14)), new BorderWidths(2))));
        javafx.scene.effect.DropShadow ds = new javafx.scene.effect.DropShadow();
        ds.setColor(Color.web("#000000", 0.7)); ds.setRadius(calcSize(20)); ds.setOffsetX(calcSize(4)); ds.setOffsetY(calcSize(4));
        layout.setEffect(ds);
        Label titleLabel = new Label(title);
        titleLabel.setFont(calcFont("Georgia", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.web("#E8C97A"));
        Label messageLabel = new Label(message);
        messageLabel.setFont(calcFont("Georgia", null, 15));
        messageLabel.setTextFill(Color.web("#F5E6C8"));
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(calcSize(420));
        messageLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        Button okButton = new Button("OK");
        styleThemedButton(okButton);
        okButton.setOnAction(e -> window.close());
        layout.getChildren().addAll(titleLabel, messageLabel, okButton);
        Scene scene = new Scene(layout);
        scene.setFill(Color.TRANSPARENT);
        window.setScene(scene);
        window.showAndWait();
    }

    public void displayAlert2(String title, String message) {
        Stage window = new Stage();
        VBox layout = new VBox(calcSize(18));
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(calcSize(30), calcSize(36), calcSize(28), calcSize(36)));
        layout.setBackground(new Background(new BackgroundFill(Color.web("#1C0F05"), new CornerRadii(calcSize(14)), Insets.EMPTY)));
        layout.setBorder(new Border(new BorderStroke(Color.web("#C4A97A"), BorderStrokeStyle.SOLID, new CornerRadii(calcSize(14)), new BorderWidths(2))));
        javafx.scene.effect.DropShadow ds = new javafx.scene.effect.DropShadow();
        ds.setColor(Color.web("#000000", 0.7)); ds.setRadius(calcSize(20)); ds.setOffsetX(calcSize(4)); ds.setOffsetY(calcSize(4));
        layout.setEffect(ds);
        Label titleLabel = new Label(title);
        titleLabel.setFont(calcFont("Georgia", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.web("#E8C97A"));
        Label messageLabel = new Label(message);
        messageLabel.setFont(calcFont("Georgia", null, 15));
        messageLabel.setTextFill(Color.web("#F5E6C8"));
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(calcSize(420));
        messageLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        Button okButton = new Button("OK");
        styleThemedButton(okButton);
        okButton.setOnAction(e -> window.close());
        layout.getChildren().addAll(titleLabel, messageLabel, okButton);
        Scene scene = new Scene(layout);
        scene.setFill(Color.TRANSPARENT);
        window.setScene(scene);
        window.showAndWait();
    }

    private void styleThemedButton(Button btn) {
        String base = "-fx-background-color: linear-gradient(to bottom, #4CAF50, #2E7D32);" +
            "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-family: 'Georgia'; -fx-font-size: " + (int) calcSize(14) + "px;" +
            "-fx-background-radius: " + (int) calcSize(8) + "; -fx-border-color: #A5D6A7; -fx-border-radius: " + (int) calcSize(8) + "; -fx-border-width: 1.5;" +
            "-fx-padding: " + (int) calcSize(7) + " " + (int) calcSize(28) + "; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 6, 0, 2, 2);";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #66BB6A, #388E3C);" +
            "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-family: 'Georgia'; -fx-font-size: " + (int) calcSize(14) + "px;" +
            "-fx-background-radius: " + (int) calcSize(8) + "; -fx-border-color: #C8E6C9; -fx-border-radius: " + (int) calcSize(8) + "; -fx-border-width: 1.5;" +
            "-fx-padding: " + (int) calcSize(7) + " " + (int) calcSize(28) + "; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 8, 0, 2, 3);"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(base));
    }


    public int selectedCount;
}