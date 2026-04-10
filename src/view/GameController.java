package view;

import engine.Game;
import exception.GameException;
import model.Colour;
import model.card.Card;
import model.card.standard.*;
import model.card.wild.*;
import model.player.Marble;
import model.player.Player;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * MVC Controller: connects UI events to game engine calls.
 * Holds selection state and drives CPU automation.
 */
public class GameController {

    private final Game game;
    private final GameApp app;
    private final javafx.stage.Stage stage;
    private final GameView view;

    // Selection state
    private Card selectedCard = null;
    private final List<Marble> selectedMarbles = new ArrayList<>();
    private boolean cpuRunning = false;

    public GameController(Game game, GameApp app, javafx.stage.Stage stage) {
        this.game  = game;
        this.app   = app;
        this.stage = stage;
        this.view  = new GameView(game, this);
    }

    public GameView getView() { return view; }

    // ─── Selection helpers ────────────────────────────────────────────────

    public Card getSelectedCard()       { return selectedCard; }
    public List<Marble> getSelectedMarbles() { return selectedMarbles; }

    public boolean isHumanTurn() {
        return game.getActivePlayerColour() == game.getPlayers().get(0).getColour();
    }

    // ─── Card clicked ─────────────────────────────────────────────────────

    public void onCardClicked(Card card) {
        if (!isHumanTurn() || cpuRunning) return;
        try {
            // Deselect previous
            game.deselectAll();
            selectedMarbles.clear();
            selectedCard = card;
            game.selectCard(card);
            view.setStatus("Card selected: " + card.getName() + "  — click marble(s), then PLAY");
        } catch (Exception ex) {
            showError("Card Selection", ex.getMessage());
        }
        refreshAll();
    }

    // ─── Marble clicked (track or safe-zone) ─────────────────────────────

    public void onMarbleClicked(Marble marble) {
        if (!isHumanTurn() || cpuRunning) return;
        if (selectedCard == null) {
            view.setStatus("⚠ Select a card first!");
            return;
        }
        try {
            game.selectMarble(marble);
            selectedMarbles.add(marble);
            view.setStatus("Marble selected (" + selectedMarbles.size() + "). Press PLAY or select another.");
        } catch (Exception ex) {
            showError("Marble Selection", ex.getMessage());
        }
        refreshAll();
    }

    // ─── Play button ──────────────────────────────────────────────────────

    public void onPlayClicked() {
        if (!isHumanTurn() || cpuRunning) {
            showError("Not Your Turn", "Please wait for your turn.");
            return;
        }
        if (selectedCard == null) {
            showError("No Card Selected", "Please select a card first.");
            return;
        }
        try {
            game.playPlayerTurn();
            finishHumanTurn();
        } catch (GameException ex) {
            showError("Invalid Action", ex.getMessage());
            // Let player retry
        }
    }

    // ─── Deselect all ────────────────────────────────────────────────────

    public void onDeselectAll() {
        selectedCard = null;
        selectedMarbles.clear();
        try { game.deselectAll(); } catch (Exception ignored) {}
        view.setStatus("Selection cleared.");
        refreshAll();
    }

    // ─── Split distance changed ───────────────────────────────────────────

    public void onSplitDistanceChanged(int val) {
        try { game.editSplitDistance(val); }
        catch (Exception ex) { showError("Split Error", ex.getMessage()); }
    }

    // ─── Keyboard shortcut: F = field a marble ───────────────────────────

    public void fieldMarbleShortcut() {
        if (!isHumanTurn() || cpuRunning) return;
        Player human = game.getPlayers().get(0);
        for (Card c : human.getHand()) {
            if (c instanceof Ace || c instanceof King) {
                try {
                    game.deselectAll();
                    selectedMarbles.clear();
                    selectedCard = c;
                    game.selectCard(c);
                    // For Ace/King with no marbles selected → fields from home zone
                    game.playPlayerTurn();
                    finishHumanTurn();
                    return;
                } catch (GameException ex) {
                    showError("Field Error", ex.getMessage());
                    try { game.deselectAll(); } catch (Exception ignored) {}
                    selectedCard = null;
                    selectedMarbles.clear();
                    refreshAll();
                    return;
                }
            }
        }
        view.setStatus("No Ace or King card available to field a marble!");
    }

    // ─── Internal ─────────────────────────────────────────────────────────

    private void finishHumanTurn() {
        game.endPlayerTurn();
        selectedCard = null;
        selectedMarbles.clear();
        refreshAll();

        Colour winner = game.checkWin();
        if (winner != null) { showWin(winner); return; }

        startCpuTurns();
    }

    private void startCpuTurns() {
        cpuRunning = true;
        runNextCpu();
    }

    private void runNextCpu() {
        if (isHumanTurn()) {
            cpuRunning = false;
            refreshAll();
            view.setStatus("🎴 Your turn! Select a card, click marble(s), then PLAY.");
            return;
        }

        Player cpu = findPlayer(game.getActivePlayerColour());
        view.setStatus("⏳ " + (cpu != null ? cpu.getName() : "CPU") + " is thinking...");

        PauseTransition pause = new PauseTransition(Duration.millis(900));
        pause.setOnFinished(e -> {
            try { game.playPlayerTurn(); } catch (GameException ignored) { /* CPU may discard */ }
            try { game.endPlayerTurn(); } catch (Exception ignored) {}

            refreshAll();

            Colour winner = game.checkWin();
            if (winner != null) { cpuRunning = false; showWin(winner); return; }

            runNextCpu();
        });
        pause.play();
    }

    private Player findPlayer(Colour col) {
        for (Player p : game.getPlayers())
            if (p.getColour() == col) return p;
        return null;
    }

    // ─── Dialogs ─────────────────────────────────────────────────────────

    private void showError(String title, String msg) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(msg != null ? msg : "An unexpected error occurred.");
        styleDialog(alert);
        alert.showAndWait();
        // Game continues — do NOT call anything that stops the game
    }

    private void showWin(Colour winner) {
        Player winPl = findPlayer(winner);
        String name  = winPl != null ? winPl.getName() : winner.name();

        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over!");
        alert.setHeaderText("🏆  " + winner.name() + " WINS!");
        alert.setContentText(name + " moved all marbles into the Safe Zone!\n\nClick OK to return to the start screen.");
        styleDialog(alert);
        alert.showAndWait();
        app.showStartScreen();
    }

    private void styleDialog(javafx.scene.control.Alert alert) {
        javafx.scene.control.DialogPane dp = alert.getDialogPane();
        dp.setStyle("-fx-background-color: #2a1200; -fx-border-color: #8B6914; -fx-border-width: 2;");
        javafx.scene.Node header = dp.lookup(".header-panel");
        if (header != null) header.setStyle("-fx-background-color: #3a1800;");
        javafx.scene.Node headerLbl = dp.lookup(".header-panel .label");
        if (headerLbl != null) headerLbl.setStyle("-fx-text-fill: #FFD700; -fx-font-family: Georgia; -fx-font-weight: bold;");
        javafx.scene.Node content = dp.lookup(".content.label");
        if (content != null) content.setStyle("-fx-text-fill: #E0C080; -fx-font-family: Georgia;");
    }

    // ─── Refresh ──────────────────────────────────────────────────────────

    public void refreshAll() {
        view.refreshAll();
    }
}