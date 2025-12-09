package views;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx. geometry.Pos;
import javafx.scene.Scene;
import javafx.scene. control.Button;
import javafx.scene.control.Label;
import javafx. scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx. scene.text.FontWeight;
import javafx.stage.Stage;

public class MainMenuView extends Application {

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(20);
        root. setAlignment(Pos. CENTER);
        root.setPadding(new Insets(50));
        root.setStyle("-fx-background-color: #2D2D2D;");

        Label titleLabel = new Label("NIMONSCOOKED");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        titleLabel.setTextFill(Color. ORANGE);

        Label subtitleLabel = new Label("Pizza Kitchen Adventure");
        subtitleLabel.setFont(Font.font("Arial", FontWeight. NORMAL, 18));
        subtitleLabel.setTextFill(Color.WHITE);

        Button startBtn = createMenuButton("START GAME");
        Button howToPlayBtn = createMenuButton("HOW TO PLAY");
        Button exitBtn = createMenuButton("EXIT");

        startBtn.setOnAction(e -> openLevelSelect(primaryStage));
        howToPlayBtn.setOnAction(e -> showHowToPlay());
        exitBtn. setOnAction(e -> Platform.exit());

        VBox spacer = new VBox();
        spacer. setMinHeight(40);

        root.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                spacer,
                startBtn,
                howToPlayBtn,
                exitBtn
        );

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("NIMONSCOOKED");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn. setFont(Font.font("Arial", FontWeight. BOLD, 18));
        btn. setPrefSize(300, 50);
        btn.setStyle(
                "-fx-background-color: #4682B4;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #5A9BD4;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: #4682B4;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;"
        ));

        return btn;
    }

    private void openLevelSelect(Stage primaryStage) {
        LevelSelectView levelSelect = new LevelSelectView();
        levelSelect.show(primaryStage);
    }

    private void showHowToPlay() {
        Stage helpStage = new Stage();

        VBox root = new VBox(15);
        root. setAlignment(Pos.TOP_LEFT);
        root. setPadding(new Insets(20));
        root. setStyle("-fx-background-color: #3D3D3D;");

        Label title = new Label("HOW TO PLAY");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.ORANGE);

        String instructions =
                "CONTROLS:\n" +
                        "  W/A/S/D - Move chef\n" +
                        "  C or V - Interact with station\n" +
                        "  B - Switch between chefs\n" +
                        "  ESC - Pause game\n\n" +
                        "OBJECTIVE:\n" +
                        "  Complete pizza orders before time runs out!\n\n" +
                        "STATIONS:\n" +
                        "  C (Brown) - Cutting Station\n" +
                        "  R (Red) - Cooking Station\n" +
                        "  A (Green) - Assembly Station\n" +
                        "  S (Gold) - Serving Counter\n" +
                        "  W (Blue) - Washing Station\n" +
                        "  I (Orange) - Ingredient Storage\n" +
                        "  P (White) - Plate Storage\n" +
                        "  T (Gray) - Trash Station";

        Label instructionsLabel = new Label(instructions);
        instructionsLabel.setFont(Font.font("Consolas", 14));
        instructionsLabel.setTextFill(Color. WHITE);

        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> helpStage.close());

        root.getChildren(). addAll(title, instructionsLabel, closeBtn);

        Scene scene = new Scene(root, 450, 500);
        helpStage.setTitle("How To Play");
        helpStage.setScene(scene);
        helpStage.show();
    }
}