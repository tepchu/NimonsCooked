package views;

import controllers.GameController;
import models.level.Level;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class ResultView {

    private GameController gameController;
    private controllers.Stage gameStage;
    private Level level;

    public ResultView(GameController controller) {
        this.gameController = controller;
        this.gameStage = controller.getStage();
        this.level = controller.getLevelManager().getCurrentLevel();
    }

    public void show(Stage primaryStage) {
        int finalScore = gameStage.getScore();
        int starsEarned = level.calculateStars(finalScore);
        boolean passed = starsEarned >= 3;

        // Update level progress
        level.updateStarsIfBetter(starsEarned);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));

        // Background color based on performance
        String bgColor = passed ? "#228B22" : (starsEarned >= 1 ? "#B8860B" : "#8B2222");
        root.setStyle("-fx-background-color: " + bgColor + ";");

        Label resultLabel = new Label(passed ? "STAGE CLEARED!" : (starsEarned >= 1 ? "STAGE PASSED" : "STAGE FAILED"));
        resultLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        resultLabel.setTextFill(Color.WHITE);

        // Star display
        HBox starsBox = createStarsDisplay(starsEarned);

        Label scoreLabel = new Label("Final Score: " + finalScore);
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        scoreLabel.setTextFill(Color.WHITE);

        // Score thresholds
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

        Button retryBtn = new Button("Retry Level");
        Button menuBtn = new Button("Back to Menu");

        retryBtn.setPrefWidth(200);
        menuBtn.setPrefWidth(200);
        retryBtn.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        menuBtn.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        retryBtn.setOnAction(e -> retryLevel(primaryStage));
        menuBtn.setOnAction(e -> backToMenu(primaryStage));

        VBox spacer1 = new VBox();
        spacer1.setMinHeight(10);
        VBox spacer2 = new VBox();
        spacer2.setMinHeight(20);

        root.getChildren().addAll(
                resultLabel,
                starsBox,
                scoreLabel,
                thresholdsBox,
                spacer1,
                successLabel,
                expiredLabel,
                failedLabel,
                spacer2,
                retryBtn,
                menuBtn
        );

        Scene scene = new Scene(root, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Game Over");
    }

    private HBox createStarsDisplay(int stars) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);

        for (int i = 1; i <= 3; i++) {
            Label star = new Label("★");
            star.setFont(Font.font("Arial", FontWeight.BOLD, 48));
            star.setTextFill(i <= stars ? Color.GOLD : Color.rgb(100, 100, 100));
            box.getChildren().add(star);
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