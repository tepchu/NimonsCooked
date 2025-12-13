package views;

import controllers.GameController;
import models.level.Level;
import models.level.LevelManager;
import javafx.geometry.Insets;
import javafx. geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control. Button;
import javafx.scene.control.Label;
import javafx.scene. control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene. text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;

public class LevelSelectView {

    private LevelManager levelManager;
    private GameController gameController;

    public LevelSelectView() {
        this.levelManager = LevelManager.getInstance();
        this.gameController = new GameController();
    }

    public void show(Stage primaryStage) {
        VBox root = new VBox(15);
        root. setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(30));
        root. setStyle("-fx-background-color: #2D2D2D;");

        Label titleLabel = new Label("SELECT LEVEL");
        titleLabel.setFont(Font.font("Arial", FontWeight. BOLD, 36));
        titleLabel.setTextFill(Color. ORANGE);

        VBox levelsBox = new VBox(10);
        levelsBox.setAlignment(Pos.CENTER);

        Label predefinedLabel = new Label("CHOOSE LEVEL:");
        predefinedLabel.setFont(Font.font("Arial", FontWeight. BOLD, 18));
        predefinedLabel.setTextFill(Color. WHITE);
        levelsBox.getChildren().add(predefinedLabel);

        List<Level> levels = levelManager.getPredefinedLevels();
        for (Level level : levels) {
            Button levelBtn = createLevelButton(level, primaryStage);
            levelsBox. getChildren().add(levelBtn);
        }

        VBox spacer = new VBox();
        spacer.setMinHeight(20);
        levelsBox.getChildren(). add(spacer);

        Label randomLabel = new Label("RANDOM LEVEL:");
        randomLabel. setFont(Font. font("Arial", FontWeight.BOLD, 18));
        randomLabel.setTextFill(Color. WHITE);
        levelsBox.getChildren().add(randomLabel);

        Button easyRandomBtn = createRandomButton("EASY - Random", Level. Difficulty.EASY, primaryStage);
        Button hardRandomBtn = createRandomButton("HARD - Random", Level. Difficulty.HARD, primaryStage);
        levelsBox.getChildren().addAll(easyRandomBtn, hardRandomBtn);

        Button backBtn = new Button("<- Back to Menu");
        backBtn.setFont(Font.font("Arial", 14));
        backBtn.setOnAction(e -> {
            MainMenuView mainMenu = new MainMenuView();
            mainMenu. start(primaryStage);
        });

        ScrollPane scrollPane = new ScrollPane(levelsBox);
        scrollPane.setFitToWidth(true);
        scrollPane. setStyle("-fx-background: #2D2D2D; -fx-background-color: #2D2D2D;");

        root.getChildren().addAll(titleLabel, scrollPane, backBtn);

        Scene scene = new Scene(root, 800, 600);
        primaryStage. setScene(scene);
    }

    private Button createLevelButton(Level level, Stage primaryStage) {
        String difficultyText = switch (level.getDifficulty()) {
            case EASY -> "[EASY]";
            case MEDIUM -> "[MEDIUM]";
            case HARD -> "[HARD]";
        };

        String status = level.isCompleted() ? " [DONE]" : "";
        String text = String.format("%s %s | Target: %d pts | Time: %ds%s",
                difficultyText, level.getName(), level.getTargetScore(),
                level.getTimeLimit(), status);

        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", 14));
        btn.setPrefWidth(500);
        btn. setPrefHeight(40);
        btn. setStyle(
                "-fx-background-color: #4682B4;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 5;"
        );

        btn.setOnAction(e -> startGame(level, primaryStage));

        return btn;
    }

    private Button createRandomButton(String text, Level. Difficulty difficulty, Stage primaryStage) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        btn. setPrefWidth(500);
        btn. setPrefHeight(45);

        String bgColor = difficulty == Level.Difficulty.EASY ? "#2E8B57" : "#B22222";
        btn.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 5;"
        );

        btn.setOnAction(e -> {
            Level randomLevel = difficulty == Level.Difficulty.EASY ?
                    levelManager.generateRandomEasy() : levelManager. generateRandomHard();
            startGame(randomLevel, primaryStage);
        });

        return btn;
    }

    private void startGame(Level level, Stage primaryStage) {
        gameController.startLevel(level);
        GameView gameView = new GameView(gameController);
        gameView. show(primaryStage);
    }
}