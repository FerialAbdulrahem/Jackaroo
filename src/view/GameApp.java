package view;

import engine.Game;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

public class GameApp extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("Jackaroo");
        stage.setResizable(false);
        showStartScreen();
        stage.show();
    }

    public void showStartScreen() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1a0800, #3b1800);");

        VBox box = new VBox(18);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(50));
        box.setMaxWidth(400);

        // Title
        Label title = new Label("JACKAROO");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 60));
        title.setTextFill(Color.web("#FFD700"));
        title.setStyle("-fx-effect: dropshadow(gaussian, #ff8800, 18, 0.5, 0, 0);");

        Label sub = new Label("A New Game Spin");
        sub.setFont(Font.font("Georgia", 20));
        sub.setTextFill(Color.web("#C8A45A"));

        Separator sep = new Separator();
        sep.setMaxWidth(280);

        Label enterName = new Label("Enter Your Name:");
        enterName.setFont(Font.font("Georgia", 16));
        enterName.setTextFill(Color.web("#E0C080"));

        TextField nameField = new TextField();
        nameField.setMaxWidth(260);
        nameField.setFont(Font.font("Georgia", 15));
        nameField.setPromptText("Your name...");
        nameField.setStyle(
                "-fx-background-color: #2a1000;" +
                "-fx-text-fill: #FFD700;" +
                "-fx-prompt-text-fill: #60401a;" +
                "-fx-border-color: #8B6914;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 5;" +
                "-fx-background-radius: 5;");

        Button startBtn = new Button("START GAME");
        startBtn.setFont(Font.font("Georgia", FontWeight.BOLD, 16));
        startBtn.setPrefWidth(200);
        startBtn.setPrefHeight(45);
        startBtn.setStyle(goldBtnStyle(false));
        startBtn.setOnMouseEntered(e -> startBtn.setStyle(goldBtnStyle(true)));
        startBtn.setOnMouseExited(e  -> startBtn.setStyle(goldBtnStyle(false)));

        Label errLabel = new Label();
        errLabel.setFont(Font.font("Georgia", 13));
        errLabel.setTextFill(Color.web("#FF5555"));

        Label shortcutHint = new Label("Tip: Press F during your turn to field a marble (Ace/King shortcut)");
        shortcutHint.setFont(Font.font("Georgia", 11));
        shortcutHint.setTextFill(Color.web("#807050"));
        shortcutHint.setWrapText(true);
        shortcutHint.setTextAlignment(TextAlignment.CENTER);

        startBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) { errLabel.setText("Please enter your name!"); return; }
            try {
                Game game = new Game(name);
                showGameScreen(game, name);
            } catch (Exception ex) {
                errLabel.setText("Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        nameField.setOnAction(e -> startBtn.fire());

        box.getChildren().addAll(title, sub, sep, enterName, nameField, startBtn, errLabel, shortcutHint);
        root.getChildren().add(box);

        Scene scene = new Scene(root, 520, 500);
        primaryStage.setScene(scene);
    }

    private void showGameScreen(Game game, String playerName) {
        GameController controller = new GameController(game, this, primaryStage);
        Scene scene = new Scene(controller.getView(), 1260, 820);

        // Keyboard shortcut: F = field marble
        scene.setOnKeyPressed(ke -> {
            if (ke.getCode() == javafx.scene.input.KeyCode.F) {
                controller.fieldMarbleShortcut();
            }
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("Jackaroo – " + playerName);
        controller.refreshAll();
    }

    private String goldBtnStyle(boolean hover) {
        String bg = hover ? "#a07820" : "#8B6914";
        return "-fx-background-color: " + bg + ";" +
               "-fx-text-fill: #FFD700;" +
               "-fx-border-color: #FFD700;" +
               "-fx-border-width: 2;" +
               "-fx-border-radius: 5;" +
               "-fx-background-radius: 5;" +
               "-fx-cursor: hand;";
    }

    public static void main(String[] args) {
        launch(args);
    }
}