package views;

import controllers.GameController;
import models.level.Level;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class ResultView {

    private GameController gameController;
    private controllers.Stage gameStage;
    private Level level;

    private Map<String, Image> imageCache = new HashMap<>();
    private boolean useImages = true;

    public ResultView(GameController controller) {
        this.gameController = controller;
        this.gameStage = controller.getStage();
        this.level = controller.getLevelManager().getCurrentLevel();
        loadImages();
    }

    private void loadImages() {
        try {
            loadImage("level_succeed_bg", "/images/results/level_succeed_bg.png");
            loadImage("level_fail_bg", "/images/results/level_fail_bg.png");
            loadImage("star_empty", "/images/results/star_empty.png");
            loadImage("star_filled", "/images/results/star_filled.png");
            loadImage("congrats_text", "/images/results/congrats_text.png");
            loadImage("failed_text", "/images/results/failed_text.png");

            System.out.println("[ResultView] Loaded " + imageCache.size() + " images");
        } catch (Exception e) {
            System.out.println("[ResultView] Failed to load images, using fallback");
            useImages = false;
        }
    }

    private void loadImage(String key, String path) {
        try {
            var resource = getClass().getResourceAsStream(path);
            if (resource != null) {
                Image img = new Image(resource);
                if (!img.isError()) {
                    imageCache.put(key, img);
                }
            }
        } catch (Exception e) {
            System.out.println("[ResultView] Failed: " + path);
        }
    }

    private Image getImage(String key) {
        return imageCache.get(key);
    }

    private boolean hasImage(String key) {
        return imageCache.containsKey(key) && imageCache.get(key) != null;
    }

    public void show(Stage primaryStage) {
        int finalScore = gameStage.getScore();
        int starsEarned = level.calculateStars(finalScore);
        boolean passed = starsEarned >= 3;

        level.updateStarsIfBetter(starsEarned);

        StackPane root = new StackPane();
        root.setPrefSize(800, 600);

        // Background
        if (useImages) {
            String bgKey = passed ? "level_succeed_bg" : "level_fail_bg";
            if (hasImage(bgKey)) {
                ImageView bgView = new ImageView(getImage(bgKey));
                bgView.setFitWidth(800);
                bgView.setFitHeight(600);
                bgView.setPreserveRatio(false);
                root.getChildren().add(bgView);
            } else {
                useFallbackBackground(root, passed);
            }
        } else {
            useFallbackBackground(root, passed);
        }

        // Content
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(50));

        // Title Image (320x120 at top, centered)
        if (useImages) {
            String titleKey = passed ? "congrats_text" : "failed_text";
            if (hasImage(titleKey)) {
                ImageView titleView = new ImageView(getImage(titleKey));
                titleView.setFitWidth(320);
                titleView.setFitHeight(120);
                titleView.setPreserveRatio(true);
                content.getChildren().add(titleView);
                StackPane.setAlignment(titleView, Pos.TOP_CENTER);
                StackPane.setMargin(titleView, new Insets(80, 0, 0, 0));
            }
        } else {
            Label titleLabel = new Label(passed ? "STAGE CLEARED!" : "STAGE FAILED");
            titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 48));
            titleLabel.setTextFill(Color.WHITE);
            content.getChildren().add(titleLabel);
        }

        // Stars (80x80 each, 20px apart, centered vertically)
        HBox starsBox = createStarsDisplay(starsEarned);
        content.getChildren().add(starsBox);
        StackPane.setAlignment(starsBox, Pos.CENTER);

        // Score info
        VBox scoreBox = new VBox(10);
        scoreBox.setAlignment(Pos.CENTER);

        Label scoreLabel = new Label("Final Score: " + finalScore);
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        scoreLabel.setTextFill(Color.WHITE);

        VBox thresholdsBox = new VBox(5);
        thresholdsBox.setAlignment(Pos.CENTER);

        Label threshold1 = new Label("★ 1 Star: " + level.getOneStarThreshold());
        threshold1.setFont(Font.font("Arial", 14));
        threshold1.setTextFill(starsEarned >= 1 ? Color.GOLD : Color.LIGHTGRAY);

        Label threshold2 = new Label("★★ 2 Stars: " + level.getTwoStarThreshold());
        threshold2.setFont(Font.font("Arial", 14));
        threshold2.setTextFill(starsEarned >= 2 ? Color.GOLD : Color.LIGHTGRAY);

        Label threshold3 = new Label("★★★ 3 Stars: " + level.getThreeStarThreshold());
        threshold3.setFont(Font.font("Arial", 14));
        threshold3.setTextFill(starsEarned >= 3 ? Color.GOLD : Color.LIGHTGRAY);

        thresholdsBox.getChildren().addAll(threshold1, threshold2, threshold3);

        Label successLabel = new Label("Orders Completed: " + gameStage.getSuccessfulOrders());
        successLabel.setFont(Font.font("Arial", 16));
        successLabel.setTextFill(Color.LIGHTGREEN);

        Label expiredLabel = new Label("Orders Expired: " + gameStage.getExpiredOrders());
        expiredLabel.setFont(Font.font("Arial", 16));
        expiredLabel.setTextFill(Color.YELLOW);

        Label failedLabel = new Label("Total Failed: " + gameStage.getFailedOrdersCount() +
                " / " + gameStage.getMaxFailedOrders());
        failedLabel.setFont(Font.font("Arial", 16));
        failedLabel.setTextFill(Color.LIGHTCORAL);

        scoreBox.getChildren().addAll(scoreLabel, thresholdsBox, successLabel, expiredLabel, failedLabel);
        content.getChildren().add(scoreBox);

        // Buttons
        HBox buttonsBox = new HBox(20);
        buttonsBox.setAlignment(Pos.CENTER);

        Button retryBtn = new Button("Retry Level");
        Button menuBtn = new Button("Back to Menu");

        retryBtn.setPrefWidth(200);
        menuBtn.setPrefWidth(200);
        retryBtn.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        menuBtn.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        retryBtn.setOnAction(e -> retryLevel(primaryStage));
        menuBtn.setOnAction(e -> backToMenu(primaryStage));

        buttonsBox.getChildren().addAll(retryBtn, menuBtn);
        content.getChildren().add(buttonsBox);

        root.getChildren().add(content);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Game Over");
    }

    private void useFallbackBackground(StackPane root, boolean passed) {
        String bgColor = passed ? "#228B22" : "#8B2222";
        root.setStyle("-fx-background-color: " + bgColor + ";");
    }

    private HBox createStarsDisplay(int starsEarned) {
        HBox box = new HBox(20); // 20px spacing between stars
        box.setAlignment(Pos.CENTER);

        for (int i = 1; i <= 3; i++) {
            if (useImages && hasImage("star_empty") && hasImage("star_filled")) {
                String starKey = i <= starsEarned ? "star_filled" : "star_empty";
                ImageView starView = new ImageView(getImage(starKey));
                starView.setFitWidth(80);
                starView.setFitHeight(80);
                starView.setPreserveRatio(true);
                box.getChildren().add(starView);
            } else {
                // Fallback
                Label star = new Label("★");
                star.setFont(Font.font("Arial", FontWeight.BOLD, 60));
                star.setTextFill(i <= starsEarned ? Color.GOLD : Color.rgb(100, 100, 100));
                box.getChildren().add(star);
            }
        }

        return box;
    }

    private void retryLevel(Stage primaryStage) {
        gameController.startLevel(level);
        GameView gameView = new GameView(gameController);
        gameView.show(primaryStage);
    }

    private void backToMenu(Stage primaryStage) {
        MainMenuView mainMenu = new MainMenuView();
        mainMenu.start(primaryStage);
    }
}