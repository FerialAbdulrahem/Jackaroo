package view;

import engine.Game;
import engine.board.Cell;
import engine.board.CellType;
import engine.board.SafeZone;
import model.Colour;
import model.card.Card;
import model.card.standard.Standard;
import model.card.standard.Suit;
import model.card.standard.Seven;
import model.card.wild.Wild;
import model.player.Marble;
import model.player.Player;

import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;

import java.util.*;

/**
 * MVC View: renders the full Jackaroo board, player panels, cards, and status.
 *
 * Board layout (100 cells, clockwise square):
 *   Bottom side : cells  0-24  (left → right)
 *   Right  side : cells 25-49  (bottom → top)
 *   Top    side : cells 50-74  (right → left)
 *   Left   side : cells 75-99  (top → bottom)
 *
 * Base cells at 0, 25, 50, 75.  Entry cells at 23, 48, 73, 98.
 * Safe zones branch inward from each entry cell.
 */
public class GameView extends BorderPane {

    // ── sizes ────────────────────────────────────────────────────────────────
    private static final int    CELLS      = 100;
    private static final double CELL_R     = 12.0;   // circle radius for track cells
    private static final double MARBLE_R   = 10.0;
    private static final double SZ_CELL_R  = 11.0;   // safe-zone cell radius
    private static final double BOARD_W    = 660;
    private static final double BOARD_H    = 660;
    private static final double MARGIN     = 28;

    // ── colour palette ────────────────────────────────────────────────────────
    private static final Map<Colour, Color> COL_BRIGHT;
    private static final Map<Colour, Color> COL_DARK;
    static {
        COL_BRIGHT = new EnumMap<>(Colour.class);
        COL_BRIGHT.put(Colour.RED,    Color.web("#EF5350"));
        COL_BRIGHT.put(Colour.GREEN,  Color.web("#66BB6A"));
        COL_BRIGHT.put(Colour.BLUE,   Color.web("#42A5F5"));
        COL_BRIGHT.put(Colour.YELLOW, Color.web("#FFEE58"));

        COL_DARK = new EnumMap<>(Colour.class);
        COL_DARK.put(Colour.RED,    Color.web("#7B1010"));
        COL_DARK.put(Colour.GREEN,  Color.web("#1B5E20"));
        COL_DARK.put(Colour.BLUE,   Color.web("#0D47A1"));
        COL_DARK.put(Colour.YELLOW, Color.web("#786000"));
    }

    // ── game / controller refs ────────────────────────────────────────────────
    private final Game           game;
    private final GameController ctrl;

    // ── board drawing nodes ───────────────────────────────────────────────────
    private final Pane    boardPane      = new Pane();
    // track cell circles  [0..99]
    private final Circle[] trackCells   = new Circle[CELLS];
    // marble overlays on track (drawn on top of cell circles)
    private final Circle[] trackMarbles = new Circle[CELLS];
    // computed centres for track cells
    private final double[] cx           = new double[CELLS];
    private final double[] cy           = new double[CELLS];

    // safe-zone circles: colour → 4 circles (index 0 = closest to track)
    private final Map<Colour, Circle[]>  szCells   = new EnumMap<>(Colour.class);
    private final Map<Colour, double[]>  szCx      = new EnumMap<>(Colour.class);
    private final Map<Colour, double[]>  szCy      = new EnumMap<>(Colour.class);
    private final Map<Colour, Circle[]>  szMarbles = new EnumMap<>(Colour.class);

    // ── right panel (players) ─────────────────────────────────────────────────
    private final VBox rightPanel = new VBox(8);

    // ── left panel (info) ──────────────────────────────────────────────────────
    private final VBox  leftPanel    = new VBox(10);
    private final Label currentLbl   = new Label();
    private final Label nextLbl      = new Label();
    private final Label firePitLbl   = new Label("Empty");
    private final Spinner<Integer> splitSpinner = new Spinner<>(1, 6, 3);

    // ── bottom bar (hand cards + buttons) ────────────────────────────────────
    private final HBox   cardBox   = new HBox(6);
    private final Button playBtn   = new Button("▶  PLAY");
    private final Button deselBtn  = new Button("✕  DESELECT");
    private final Label  statusLbl = new Label("Welcome to Jackaroo!");

    // ── home zone marble indicators (colour → 4 small circles) ───────────────
    private final Map<Colour, Circle[]> homeCircles = new EnumMap<>(Colour.class);

    // ────────────────────────────────────────────────────────────────────────────
    public GameView(Game game, GameController ctrl) {
        this.game = game;
        this.ctrl = ctrl;
        buildLayout();
    }

    // ═══════════════════════════ LAYOUT BUILD ═══════════════════════════════

    private void buildLayout() {
        setStyle("-fx-background-color: #180900;");

        boardPane.setPrefSize(BOARD_W + MARGIN * 2, BOARD_H + MARGIN * 2);
        boardPane.setStyle("-fx-background-color: #2C1800; -fx-border-color: #7a5a10; -fx-border-width: 3;");

        buildTrackCells();
        buildTrackMarbles();
        buildSafeZones();
        buildFirePitLabel();

        // Status bar at top of center
        statusLbl.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        statusLbl.setTextFill(Color.web("#FFD700"));
        statusLbl.setMaxWidth(Double.MAX_VALUE);
        statusLbl.setAlignment(Pos.CENTER);
        statusLbl.setPadding(new Insets(6));
        statusLbl.setStyle("-fx-background-color: #200e00; -fx-border-color: #5a3a00; -fx-border-width: 0 0 2 0;");

        VBox centerCol = new VBox(0, statusLbl, boardPane);
        centerCol.setAlignment(Pos.TOP_CENTER);

        setCenter(centerCol);
        setRight(buildRightPanel());
        setLeft(buildLeftPanel());
        setBottom(buildBottomBar());

        BorderPane.setMargin(centerCol, new Insets(0, 4, 0, 4));
    }

    // ─── Track cell circles ───────────────────────────────────────────────────
    private void buildTrackCells() {
        computeTrackCentres();
        for (int i = 0; i < CELLS; i++) {
            Circle c = new Circle(cx[i], cy[i], CELL_R);
            styleTrackCell(c, game.getBoard().getTrack().get(i));
            boardPane.getChildren().add(c);
            trackCells[i] = c;

            // Index label every 5 cells
            if (i % 5 == 0) {
                Text t = new Text(String.valueOf(i));
                t.setX(cx[i] - 7); t.setY(cy[i] + 3);
                t.setFont(Font.font("Arial", 7));
                t.setFill(Color.web("#604020"));
                t.setMouseTransparent(true);
                boardPane.getChildren().add(t);
            }
        }
    }

    // ─── Marble overlay circles on track ─────────────────────────────────────
    private void buildTrackMarbles() {
        for (int i = 0; i < CELLS; i++) {
            Circle m = new Circle(cx[i], cy[i], MARBLE_R);
            m.setFill(Color.TRANSPARENT);
            m.setStroke(Color.TRANSPARENT);
            m.setStrokeWidth(0);
            final int idx = i;
            m.setOnMouseClicked(e -> {
                Marble marble = game.getBoard().getTrack().get(idx).getMarble();
                if (marble != null) ctrl.onMarbleClicked(marble);
            });
            boardPane.getChildren().add(m);
            trackMarbles[i] = m;
        }
    }

    // ─── Safe zone ───────────────────────────────────────────────────────────
    private void buildSafeZones() {
        List<SafeZone> szList = game.getBoard().getSafeZones();
        for (SafeZone sz : szList) {
            Colour col = sz.getColour();
            int baseIdx   = baseIndexFor(col, szList);
            int entryIdx  = (baseIdx - 2 + CELLS) % CELLS;

            // direction pointing inward
            double[] dir = inwardDirection(entryIdx);
            double stepX = dir[0] * (SZ_CELL_R * 2 + 3);
            double stepY = dir[1] * (SZ_CELL_R * 2 + 3);

            double[] cellX = new double[4];
            double[] cellY = new double[4];
            Circle[] cells   = new Circle[4];
            Circle[] marbles = new Circle[4];

            for (int ci = 0; ci < 4; ci++) {
                cellX[ci] = cx[entryIdx] + stepX * (ci + 1);
                cellY[ci] = cy[entryIdx] + stepY * (ci + 1);

                Circle cell = new Circle(cellX[ci], cellY[ci], SZ_CELL_R);
                cell.setFill(COL_DARK.get(col));
                cell.setStroke(COL_BRIGHT.get(col));
                cell.setStrokeWidth(1.5);
                boardPane.getChildren().add(cell);
                cells[ci] = cell;

                Circle marble = new Circle(cellX[ci], cellY[ci], SZ_CELL_R - 2);
                marble.setFill(Color.TRANSPARENT);
                marble.setStroke(Color.TRANSPARENT);
                boardPane.getChildren().add(marble);
                marbles[ci] = marble;
            }

            szCells.put(col, cells);
            szCx.put(col, cellX);
            szCy.put(col, cellY);
            szMarbles.put(col, marbles);
        }
    }

    // ─── FirePit decoration in board centre ──────────────────────────────────
    private void buildFirePitLabel() {
        double cx2 = MARGIN + BOARD_W / 2;
        double cy2 = MARGIN + BOARD_H / 2;

        Rectangle r = new Rectangle(cx2 - 70, cy2 - 35, 140, 70);
        r.setArcWidth(12); r.setArcHeight(12);
        r.setFill(Color.web("#3a1800"));
        r.setStroke(Color.web("#8B4513")); r.setStrokeWidth(2.5);

        Text fp = new Text("🔥 FirePit");
        fp.setX(cx2 - 38); fp.setY(cy2 - 6);
        fp.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        fp.setFill(Color.web("#FF6600"));

        boardPane.getChildren().addAll(r, fp);
    }

    // ─── Right panel: players ─────────────────────────────────────────────────
    private VBox buildRightPanel() {
        rightPanel.setPadding(new Insets(10));
        rightPanel.setPrefWidth(220);
        rightPanel.setStyle("-fx-background-color: #1a0900; -fx-border-color: #5a3a00; -fx-border-width: 0 0 0 2;");
        rightPanel.getChildren().add(styledLabel("PLAYERS", 15, "#FFD700", FontWeight.BOLD));
        return rightPanel;
    }

    // ─── Left panel: turn info, split, firepit ────────────────────────────────
    private VBox buildLeftPanel() {
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(165);
        leftPanel.setStyle("-fx-background-color: #1a0900; -fx-border-color: #5a3a00; -fx-border-width: 0 2 0 0;");

        currentLbl.setFont(Font.font("Georgia", 13)); currentLbl.setWrapText(true);
        nextLbl.setFont(Font.font("Georgia", 12)); nextLbl.setWrapText(true);
        nextLbl.setTextFill(Color.web("#A09060"));

        Label splitLbl2 = styledLabel("Seven split (1-6):", 11, "#C8A45A", FontWeight.NORMAL);
        splitSpinner.setStyle("-fx-background-color: #2a1000;");
        splitSpinner.setPrefWidth(80);
        splitSpinner.valueProperty().addListener((obs, o, n) -> ctrl.onSplitDistanceChanged(n));

        Label fireLbl = styledLabel("🔥 Fire Pit:", 13, "#FF7700", FontWeight.BOLD);
        firePitLbl.setFont(Font.font("Georgia", 11));
        firePitLbl.setTextFill(Color.web("#C0A060"));
        firePitLbl.setWrapText(true);

        leftPanel.getChildren().addAll(
            styledLabel("GAME INFO", 14, "#FFD700", FontWeight.BOLD),
            currentLbl, nextLbl,
            new Separator(), splitLbl2, splitSpinner,
            new Separator(), fireLbl, firePitLbl
        );
        return leftPanel;
    }

    // ─── Bottom bar: cards + buttons ─────────────────────────────────────────
    private HBox buildBottomBar() {
        HBox bar = new HBox(8);
        bar.setPadding(new Insets(8));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #130700; -fx-border-color: #5a3a00; -fx-border-width: 2 0 0 0;");

        Label handLbl = styledLabel("YOUR HAND:", 12, "#C8A45A", FontWeight.BOLD);
        cardBox.setAlignment(Pos.CENTER_LEFT);

        playBtn.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        playBtn.setPrefWidth(100); playBtn.setPrefHeight(44);
        playBtn.setStyle(actionBtnStyle("#2a6a1a", "#4CAF50", false));
        playBtn.setOnMouseEntered(e -> playBtn.setStyle(actionBtnStyle("#3a8a28", "#66BB6A", false)));
        playBtn.setOnMouseExited(e  -> playBtn.setStyle(actionBtnStyle("#2a6a1a", "#4CAF50", false)));
        playBtn.setOnAction(e -> ctrl.onPlayClicked());

        deselBtn.setFont(Font.font("Georgia", 12));
        deselBtn.setPrefHeight(44);
        deselBtn.setStyle(actionBtnStyle("#6a1a1a", "#B03030", false));
        deselBtn.setOnMouseEntered(e -> deselBtn.setStyle(actionBtnStyle("#8a2828", "#CC4444", false)));
        deselBtn.setOnMouseExited(e  -> deselBtn.setStyle(actionBtnStyle("#6a1a1a", "#B03030", false)));
        deselBtn.setOnAction(e -> ctrl.onDeselectAll());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        bar.getChildren().addAll(handLbl, cardBox, spacer, deselBtn, playBtn);
        return bar;
    }

    // ═══════════════════════════ REFRESH ════════════════════════════════════

    public void refreshAll() {
        refreshTrack();
        refreshSafeZones();
        refreshPlayerPanel();
        refreshCards();
        refreshInfoPanel();
        refreshFirePit();
    }

    // ─── Track ───────────────────────────────────────────────────────────────
    private void refreshTrack() {
        List<Cell> track = game.getBoard().getTrack();
        List<Marble> sel = ctrl.getSelectedMarbles();

        for (int i = 0; i < CELLS; i++) {
            Cell cell = track.get(i);
            styleTrackCell(trackCells[i], cell);

            Marble m = cell.getMarble();
            if (m != null) {
                boolean isSelected = sel.contains(m);
                trackMarbles[i].setFill(COL_BRIGHT.get(m.getColour()));
                trackMarbles[i].setStroke(isSelected ? Color.web("#FFD700") : Color.WHITE);
                trackMarbles[i].setStrokeWidth(isSelected ? 3 : 1.5);
                trackMarbles[i].setRadius(isSelected ? MARBLE_R + 1.5 : MARBLE_R);
            } else {
                trackMarbles[i].setFill(Color.TRANSPARENT);
                trackMarbles[i].setStroke(Color.TRANSPARENT);
                trackMarbles[i].setRadius(MARBLE_R);
            }
        }
    }

    private void styleTrackCell(Circle c, Cell cell) {
        Color fill;
        Color stroke;
        double strokeW = 1;

        if (cell.getCellType() == CellType.BASE) {
            fill = Color.web("#6a4e10"); stroke = Color.web("#FFD700"); strokeW = 2;
        } else if (cell.getCellType() == CellType.ENTRY) {
            fill = Color.web("#2a5a30"); stroke = Color.web("#90EE90"); strokeW = 2;
        } else if (cell.isTrap()) {
            fill = Color.web("#7a1010"); stroke = Color.web("#FF4444"); strokeW = 2;
        } else {
            fill = Color.web("#3a2600"); stroke = Color.web("#5a4020");
        }
        c.setFill(fill);
        c.setStroke(stroke);
        c.setStrokeWidth(strokeW);
        c.setRadius(CELL_R);
    }

    // ─── Safe zones ──────────────────────────────────────────────────────────
    private void refreshSafeZones() {
        for (SafeZone sz : game.getBoard().getSafeZones()) {
            Colour col      = sz.getColour();
            Circle[] cells  = szCells.get(col);
            Circle[] marbs  = szMarbles.get(col);
            if (cells == null) continue;

            List<Cell> szCellList = sz.getCells();
            for (int ci = 0; ci < 4; ci++) {
                Marble m = szCellList.get(ci).getMarble();
                if (m != null) {
                    marbs[ci].setFill(COL_BRIGHT.get(col));
                    marbs[ci].setStroke(Color.WHITE);
                    marbs[ci].setStrokeWidth(1.5);
                } else {
                    marbs[ci].setFill(Color.TRANSPARENT);
                    marbs[ci].setStroke(Color.TRANSPARENT);
                }
            }
        }
    }

    // ─── Player panel ─────────────────────────────────────────────────────────
    private void refreshPlayerPanel() {
        rightPanel.getChildren().clear();
        rightPanel.getChildren().add(styledLabel("PLAYERS", 15, "#FFD700", FontWeight.BOLD));

        Colour active = game.getActivePlayerColour();
        Colour next   = game.getNextPlayerColour();

        for (Player p : game.getPlayers()) {
            rightPanel.getChildren().add(buildPlayerCard(p, active, next));
        }
    }

    private VBox buildPlayerCard(Player p, Colour active, Colour next) {
        boolean isActive = p.getColour() == active;
        boolean isNext   = p.getColour() == next;
        Color   bright   = COL_BRIGHT.get(p.getColour());

        VBox box = new VBox(4);
        box.setPadding(new Insets(7));
        box.setStyle(
            "-fx-background-color: " + (isActive ? "#2e1800" : "#1a0a00") + ";" +
            "-fx-border-color: " + webHex(bright) + ";" +
            "-fx-border-width: " + (isActive ? 3 : 1) + ";" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;"
        );

        // Name row
        HBox nameRow = new HBox(6);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(7, bright);
        dot.setStroke(Color.WHITE); dot.setStrokeWidth(1);
        Label nameLbl = new Label(p.getName());
        nameLbl.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        nameLbl.setTextFill(bright);
        nameRow.getChildren().addAll(dot, nameLbl);

        if (isActive) {
            Label tag = new Label("▶ NOW");
            tag.setFont(Font.font("Arial", 9)); tag.setTextFill(Color.web("#FFD700"));
            nameRow.getChildren().add(tag);
        } else if (isNext) {
            Label tag = new Label("NEXT");
            tag.setFont(Font.font("Arial", 9)); tag.setTextFill(Color.web("#A0C0FF"));
            nameRow.getChildren().add(tag);
        }

        // Cards count
        Label cardsLbl = new Label("Cards: " + p.getHand().size());
        cardsLbl.setFont(Font.font("Georgia", 11));
        cardsLbl.setTextFill(Color.web("#C0A060"));

        // Home zone marbles
        HBox homeRow = new HBox(4);
        homeRow.setAlignment(Pos.CENTER_LEFT);
        Label homeLbl = new Label("Home: ");
        homeLbl.setFont(Font.font("Georgia", 10));
        homeLbl.setTextFill(Color.web("#807060"));
        homeRow.getChildren().add(homeLbl);
        for (int mi = 0; mi < 4; mi++) {
            Circle hm = new Circle(6);
            hm.setFill(mi < p.getMarbles().size() ? bright : Color.web("#2a2a2a"));
            hm.setStroke(Color.web("#555")); hm.setStrokeWidth(1);
            homeRow.getChildren().add(hm);
        }

        box.getChildren().addAll(nameRow, cardsLbl, homeRow);
        return box;
    }

    // ─── Human player's hand ─────────────────────────────────────────────────
    private void refreshCards() {
        cardBox.getChildren().clear();
        Player human     = game.getPlayers().get(0);
        boolean myTurn   = ctrl.isHumanTurn();
        Card    selCard  = ctrl.getSelectedCard();

        for (Card card : human.getHand()) {
            VBox cv = buildCardNode(card, myTurn, card == selCard);
            cardBox.getChildren().add(cv);
        }
    }

    private VBox buildCardNode(Card card, boolean clickable, boolean selected) {
        VBox box = new VBox(3);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(5, 7, 5, 7));
        box.setPrefWidth(72); box.setPrefHeight(96);
        box.setStyle(cardStyle(selected, clickable));

        // Suit symbol and colour
        String suitSym = "";
        Color  suitCol = Color.web("#1a0a00");
        if (card instanceof Standard) {
            Standard std = (Standard) card;
            Suit s = std.getSuit();
            if      (s == Suit.HEART)   { suitSym = "♥"; suitCol = Color.web("#CC0000"); }
            else if (s == Suit.DIAMOND) { suitSym = "♦"; suitCol = Color.web("#CC0000"); }
            else if (s == Suit.SPADE)   { suitSym = "♠"; }
            else if (s == Suit.CLUB)    { suitSym = "♣"; }
        } else if (card instanceof Wild) {
            suitSym = "★"; suitCol = Color.web("#7700CC");
        }

        Label nameLbl = new Label(card.getName());
        nameLbl.setFont(Font.font("Georgia", FontWeight.BOLD, 9));
        nameLbl.setWrapText(true);
        nameLbl.setTextAlignment(TextAlignment.CENTER);
        nameLbl.setTextFill(selected ? Color.web("#1a0800") : Color.web("#2a1800"));

        Label suitLbl = new Label(suitSym);
        suitLbl.setFont(Font.font("Arial", 20));
        suitLbl.setTextFill(selected ? suitCol.darker() : suitCol);

        Label descLbl = new Label(shortDesc(card));
        descLbl.setFont(Font.font("Arial", 7));
        descLbl.setTextFill(Color.web("#5a4020"));
        descLbl.setWrapText(true);
        descLbl.setTextAlignment(TextAlignment.CENTER);

        box.getChildren().addAll(nameLbl, suitLbl, descLbl);

        if (clickable) {
            box.setOnMouseClicked(e -> ctrl.onCardClicked(card));
            box.setOnMouseEntered(e -> { if (!selected) box.setStyle(cardStyle(false, true) + "-fx-background-color: #FFF5DC;"); });
            box.setOnMouseExited(e  -> { if (!selected) box.setStyle(cardStyle(false, true)); });
        }
        return box;
    }

    private String shortDesc(Card card) {
        String d = card.getDescription();
        if (d == null) return "";
        return d.length() > 38 ? d.substring(0, 36) + "…" : d;
    }

    private String cardStyle(boolean selected, boolean clickable) {
        String bg     = selected  ? "#FFD700" : "#F5E6C8";
        String border = selected  ? "#FF8C00" : "#8B6914";
        String bw     = selected  ? "3"       : "1";
        String cursor = clickable ? "-fx-cursor: hand;" : "-fx-opacity: 0.55;";
        return "-fx-background-color: " + bg + ";" +
               "-fx-border-color: " + border + ";" +
               "-fx-border-width: " + bw + ";" +
               "-fx-border-radius: 8;" +
               "-fx-background-radius: 8;" +
               cursor;
    }

    // ─── Info panel (left) ────────────────────────────────────────────────────
    private void refreshInfoPanel() {
        Colour active = game.getActivePlayerColour();
        Colour next   = game.getNextPlayerColour();
        Player activePl = playerFor(active);
        Player nextPl   = playerFor(next);

        currentLbl.setText("Current:\n" + (activePl != null ? activePl.getName() : "?") + " (" + active + ")");
        currentLbl.setTextFill(COL_BRIGHT.get(active));

        nextLbl.setText("Next:\n" + (nextPl != null ? nextPl.getName() : "?") + " (" + next + ")");
        nextLbl.setTextFill(COL_BRIGHT.getOrDefault(next, Color.GRAY));
    }

    private void refreshFirePit() {
        List<Card> fp = game.getFirePit();
        if (fp.isEmpty()) {
            firePitLbl.setText("Empty");
        } else {
            Card top = fp.get(fp.size() - 1);
            firePitLbl.setText(top.getName() + "\n" + shortDesc(top));
        }
    }

    // ─── Public status setter (called by controller) ──────────────────────────
    public void setStatus(String msg) {
        statusLbl.setText(msg);
    }

    // ═══════════════════════════ GEOMETRY HELPERS ════════════════════════════

    /**
     * Compute (cx[i], cy[i]) for all 100 track cells.
     * 25 cells per side, clockwise: bottom, right, top, left.
     */
    private void computeTrackCentres() {
        double step = (BOARD_W - MARGIN * 2) / 24.0;   // 24 gaps on each side
        double x0 = MARGIN, y0 = MARGIN;
        double x1 = x0 + BOARD_W - MARGIN * 2;         // = MARGIN + side length
        double y1 = y0 + BOARD_H - MARGIN * 2;

        // Bottom side: cells 0-24, left to right, y = y1
        for (int i = 0; i < 25; i++) {
            cx[i] = x0 + i * step;
            cy[i] = y1;
        }
        // Right side: cells 25-49, bottom to top, x = x1
        for (int i = 0; i < 25; i++) {
            cx[25 + i] = x1;
            cy[25 + i] = y1 - i * step;
        }
        // Top side: cells 50-74, right to left, y = y0
        for (int i = 0; i < 25; i++) {
            cx[50 + i] = x1 - i * step;
            cy[50 + i] = y0;
        }
        // Left side: cells 75-99, top to bottom, x = x0
        for (int i = 0; i < 25; i++) {
            cx[75 + i] = x0;
            cy[75 + i] = y0 + i * step;
        }

        // Shift all by MARGIN so they sit within boardPane
        for (int i = 0; i < CELLS; i++) {
            cx[i] += MARGIN;
            cy[i] += MARGIN;
        }
    }

    /**
     * Returns inward unit-direction (dx, dy) for a safe zone starting at entryIdx.
     */
    private double[] inwardDirection(int entryIdx) {
        if      (entryIdx < 25) return new double[]{ 0, -1 }; // bottom → up
        else if (entryIdx < 50) return new double[]{ -1, 0 }; // right → left
        else if (entryIdx < 75) return new double[]{ 0,  1 }; // top → down
        else                    return new double[]{ 1,  0 }; // left → right
    }

    /**
     * Returns the track index of the BASE cell for the given colour,
     * based on the safe-zone list order (base at i*25).
     */
    private int baseIndexFor(Colour col, List<SafeZone> szList) {
        for (int i = 0; i < szList.size(); i++)
            if (szList.get(i).getColour() == col) return i * 25;
        return 0;
    }

    // ─── Misc helpers ─────────────────────────────────────────────────────────

    private Player playerFor(Colour col) {
        for (Player p : game.getPlayers())
            if (p.getColour() == col) return p;
        return null;
    }

    private Label styledLabel(String text, int size, String hex, FontWeight fw) {
        Label l = new Label(text);
        l.setFont(Font.font("Georgia", fw, size));
        l.setTextFill(Color.web(hex));
        return l;
    }

    private String actionBtnStyle(String bg, String border, boolean hover) {
        return "-fx-background-color: " + bg + ";" +
               "-fx-text-fill: white;" +
               "-fx-border-color: " + border + ";" +
               "-fx-border-width: 2;" +
               "-fx-border-radius: 5;" +
               "-fx-background-radius: 5;" +
               "-fx-cursor: hand;";
    }

    private String webHex(Color c) {
        return String.format("#%02X%02X%02X",
            (int)(c.getRed() * 255),
            (int)(c.getGreen() * 255),
            (int)(c.getBlue() * 255));
    }
}