package views;

import controllers.GameController;
import models.level.Level;
import javafx.geometry.Insets;
import javafx. geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control. Button;
import javafx.scene.control. Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene. text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class ResultView {

    private GameController gameController;
    private controllers.Stage gameStage;
    private Level level;

    public ResultView(GameController controller) {
        this. gameController = controller;
        this.gameStage = controller.getStage();
        this.level = controller. getLevelManager().getCurrentLevel();
    }

    public void show(Stage primaryStage) {
        boolean passed = gameStage.getScore() >= level.getTargetScore();

        VBox root = new VBox(20);
        root. setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.setStyle("-fx-background-color: " + (passed ? "#228B22" : "#8B2222") + ";");

        Label resultLabel = new Label(passed ? "STAGE CLEARED!" : "STAGE FAILED");
        resultLabel. setFont(Font. font("Arial", FontWeight.BOLD, 36));
        resultLabel.setTextFill(Color.WHITE);

        Label scoreLabel = new Label("Final Score: " + gameStage.getScore());
        scoreLabel. setFont(Font. font("Arial", FontWeight.BOLD, 28));
        scoreLabel.setTextFill(Color.WHITE);

        Label targetLabel = new Label("Target: " + level.getTargetScore());
        targetLabel.setFont(Font.font("Arial", 18));
        targetLabel.setTextFill(Color.WHITE);

        Label successLabel = new Label("Orders Completed: " + gameStage.getSuccessfulOrders());
        successLabel. setFont(Font. font("Arial", 16));
        successLabel.setTextFill(Color. LIGHTGREEN);

        Label expiredLabel = new Label("Orders Expired: " + gameStage. getExpiredOrders());
        expiredLabel.setFont(Font.font("Arial", 16));
        expiredLabel. setTextFill(Color.YELLOW);

        Label failedLabel = new Label("Total Failed: " + gameStage.getFailedOrdersCount() +
                " / " + gameStage.getMaxFailedOrders());
        failedLabel. setFont(Font. font("Arial", 16));
        failedLabel.setTextFill(Color. LIGHTCORAL);

        Button retryBtn = new Button("Retry Level");
        Button menuBtn = new Button("Back to Menu");

        retryBtn.setPrefWidth(200);
        menuBtn.setPrefWidth(200);
        retryBtn.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        menuBtn.setFont(Font.font("Arial", FontWeight. BOLD, 16));

        retryBtn.setOnAction(e -> retryLevel(primaryStage));
        menuBtn.setOnAction(e -> backToMenu(primaryStage));

        if (passed) {
            level.setCompleted(true);
        }

        VBox spacer1 = new VBox();
        spacer1.setMinHeight(10);
        VBox spacer2 = new VBox();
        spacer2.setMinHeight(20);

        root. getChildren().addAll(
                resultLabel,
                scoreLabel,
                targetLabel,
                spacer1,
                successLabel,
                expiredLabel,
                failedLabel,
                spacer2,
                retryBtn,
                menuBtn
        );

        Scene scene = new Scene(root, 600, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Game Over");
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