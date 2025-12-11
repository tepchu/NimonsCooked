package views;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Cursor;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class MainMenuView extends Application {

    private Image backgroundImage;
    private Map<String, Image> buttonImages;
    private boolean useImages = true;

    public MainMenuView() {
        buttonImages = new HashMap<>();
    }


    @Override
    public void start(Stage primaryStage) {
        loadImages();

        StackPane root = new StackPane();
        root.setPrefSize(800, 600);

        // Background
        Canvas bgCanvas = new Canvas(800, 600);
        drawBackground(bgCanvas.getGraphicsContext2D());

        VBox mainContent = new VBox(5);
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setPadding(new Insets(20));

        // Title with glow effect
        Label titleLabel = new Label("WEARECOOKED");
        titleLabel.setFont(Font.font("Impact", FontWeight.BOLD, 72));
        titleLabel.setTextFill(Color.web("#FF6B35"));

        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#FF6B35", 0.8));
        glow.setRadius(20);
        glow.setSpread(0.6);
        titleLabel.setEffect(glow);

        Label subtitleLabel = new Label("Pizza Making Adventure");
        subtitleLabel.setFont(Font.font("Impact", FontWeight.BOLD, 22));
        subtitleLabel.setTextFill(Color.WHITE);

        DropShadow subtitleShadow = new DropShadow();
        subtitleShadow.setColor(Color.BLACK);
        subtitleShadow.setRadius(5);
        subtitleLabel.setEffect(subtitleShadow);

        VBox spacer = new VBox();
        spacer.setMinHeight(15);

        // Create stylish buttons
        StackPane startBtn = createImageButton("start", "START GAME", "#4CAF50");
        StackPane howToPlayBtn = createImageButton("howtoplay", "HOW TO PLAY", "#2196F3");
        StackPane exitBtn = createImageButton("exit", "EXIT", "#F44336");

        // Button actions
        startBtn.setOnMouseClicked(e -> openLevelSelect(primaryStage));
        howToPlayBtn.setOnMouseClicked(e -> showHowToPlay());
        exitBtn.setOnMouseClicked(e -> Platform.exit());

        mainContent.getChildren().addAll(
                startBtn,
                howToPlayBtn,
                exitBtn
        );

        root.getChildren().addAll(bgCanvas, mainContent);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("NIMONSCOOKED");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void loadImages() {
        try {
            // Try to load custom background
            var bgResource = getClass().getResourceAsStream("/images/menu/background.png");
            if (bgResource != null) {
                backgroundImage = new Image(bgResource);
                System.out.println("✓ Loaded background image");
            }
            loadButtonImage("start", "/images/menu/button_start.png");
            loadButtonImage("start_hover", "/images/menu/button_start_hover.png");
            loadButtonImage("howtoplay", "/images/menu/button_howtoplay.png");
            loadButtonImage("howtoplay_hover", "/images/menu/button_howtoplay_hover.png");
            loadButtonImage("exit", "/images/menu/button_exit.png");
            loadButtonImage("exit_hover", "/images/menu/button_exit_hover.png");

            System.out.println("✓ Loaded " + buttonImages.size() + " button images");
        } catch (Exception e) {
            System.out.println("Using gradient background");
            useImages = false;
        }
    }

    private void loadButtonImage(String key, String path) {
        try {
            var resource = getClass().getResourceAsStream(path);
            if (resource != null) {
                Image img = new Image(resource);
                if (!img.isError()) {
                    buttonImages.put(key, img);
                    System.out.println("✓ Loaded button: " + key);
                }
            }
        } catch (Exception e) {
            System.out.println("✗ Failed to load: " + path);
        }
    }

    private boolean hasButtonImage(String key) {
        return buttonImages.containsKey(key) && buttonImages.get(key) != null;
    }

    private StackPane createImageButton(String imageKey, String text, String fallbackColor) {
        StackPane button = new StackPane();
        button.setPrefSize(320, 80);
        button.setCursor(Cursor.HAND);

        // Check if we have custom button images
        if (useImages && hasButtonImage(imageKey)) {
            // Use image-based button
            ImageView normalView = new ImageView(buttonImages.get(imageKey));
            normalView.setFitWidth(320);
            normalView.setFitHeight(150);
            normalView.setPreserveRatio(false);
            normalView.setSmooth(true);

            ImageView hoverView = null;
            if (hasButtonImage(imageKey + "_hover")) {
                hoverView = new ImageView(buttonImages.get(imageKey + "_hover"));
                hoverView.setFitWidth(320);
                hoverView.setFitHeight(150);
                hoverView.setPreserveRatio(false);
                hoverView.setSmooth(true);
                hoverView.setVisible(false);
            }

            button.getChildren().add(normalView);
            if (hoverView != null) {
                button.getChildren().add(hoverView);
                ImageView finalHoverView = hoverView;

                // Hover effects
                button.setOnMouseEntered(e -> {
                    normalView.setVisible(false);
                    finalHoverView.setVisible(true);
                    button.setScaleX(1.05);
                    button.setScaleY(1.05);
                });

                button.setOnMouseExited(e -> {
                    normalView.setVisible(true);
                    finalHoverView.setVisible(false);
                    button.setScaleX(1.0);
                    button.setScaleY(1.0);
                });
            } else {
                // Just scale effect if no hover image
                button.setOnMouseEntered(e -> {
                    button.setScaleX(1.05);
                    button.setScaleY(1.05);
                });

                button.setOnMouseExited(e -> {
                    button.setScaleX(1.0);
                    button.setScaleY(1.0);
                });
            }

            // Add shadow
            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.rgb(0, 0, 0, 0.5));
            shadow.setRadius(10);
            shadow.setOffsetY(5);
            button.setEffect(shadow);

        } else {
            // Fall back to styled button
            Pane btnBackground = new Pane();
            btnBackground.setPrefSize(320, 100);
            btnBackground.setStyle(String.format(
                    "-fx-background-color: %s; " +
                            "-fx-background-radius: 40; " +
                            "-fx-border-color: white; " +
                            "-fx-border-width: 3; " +
                            "-fx-border-radius: 40;",
                    fallbackColor
            ));

            Label btnLabel = new Label(text);
            btnLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
            btnLabel.setTextFill(Color.WHITE);

            button.getChildren().addAll(btnBackground, btnLabel);

            // Hover effects for styled button
            button.setOnMouseEntered(e -> {
                button.setScaleX(1.05);
                button.setScaleY(1.05);
                btnBackground.setStyle(String.format(
                        "-fx-background-color: derive(%s, 20%%); " +
                                "-fx-background-radius: 40; " +
                                "-fx-border-color: white; " +
                                "-fx-border-width: 3; " +
                                "-fx-border-radius: 40;",
                        fallbackColor
                ));
            });

            button.setOnMouseExited(e -> {
                button.setScaleX(1.0);
                button.setScaleY(1.0);
                btnBackground.setStyle(String.format(
                        "-fx-background-color: %s; " +
                                "-fx-background-radius: 15; " +
                                "-fx-border-color: white; " +
                                "-fx-border-width: 3; " +
                                "-fx-border-radius: 15;",
                        fallbackColor
                ));
            });

            // Shadow
            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.rgb(0, 0, 0, 0.5));
            shadow.setRadius(10);
            shadow.setOffsetY(5);
            button.setEffect(shadow);
        }

        return button;
    }

    private void drawBackground(GraphicsContext gc) {
        if (useImages && backgroundImage != null) {
            gc.drawImage(backgroundImage, 0, 0, 800, 600);

            // Add dark overlay for readability
            gc.setFill(Color.rgb(0, 0, 0, 0.4));
            gc.fillRect(0, 0, 800, 600);
        } else {
            // Fallback: Beautiful gradient background
            LinearGradient gradient = new LinearGradient(
                    0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#1a1a2e")),
                    new Stop(0.5, Color.web("#16213e")),
                    new Stop(1, Color.web("#0f3460"))
            );

            gc.setFill(gradient);
            gc.fillRect(0, 0, 800, 600);

            // Draw decorative circles
            drawDecorativeElements(gc);
        }
    }

    private void drawDecorativeElements(GraphicsContext gc) {
        // Large semi-transparent circles for decoration
        gc.setFill(Color.rgb(255, 107, 53, 0.1));
        gc.fillOval(-50, -50, 200, 200);
        gc.fillOval(650, 450, 200, 200);

        gc.setFill(Color.rgb(33, 150, 243, 0.1));
        gc.fillOval(600, -80, 250, 250);
        gc.fillOval(-80, 400, 250, 250);

        // Pizza slice decorations
        drawPizzaSlice(gc, 100, 500);
        drawPizzaSlice(gc, 700, 100);
    }

    private void drawPizzaSlice(GraphicsContext gc, double x, double y) {
        gc.save();
        gc.setFill(Color.rgb(255, 193, 7, 0.3));

        // Simple triangle for pizza slice
        double[] xPoints = {x, x + 40, x + 20};
        double[] yPoints = {y, y, y + 60};
        gc.fillPolygon(xPoints, yPoints, 3);

        // Pepperoni
        gc.setFill(Color.rgb(255, 87, 51, 0.5));
        gc.fillOval(x + 15, y + 20, 8, 8);
        gc.fillOval(x + 10, y + 35, 8, 8);

        gc.restore();
    }

    private Button createStyledButton(String text, String colorHex) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        btn.setPrefSize(320, 60);

        String baseStyle = String.format(
                "-fx-background-color: %s; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 15; " +
                        "-fx-border-radius: 15; " +
                        "-fx-border-color: white; " +
                        "-fx-border-width: 2; " +
                        "-fx-cursor: hand; " +
                        "-fx-font-size: 18px; " +
                        "-fx-font-weight: bold;",
                colorHex
        );

        btn.setStyle(baseStyle);

        // Hover effect
        btn.setOnMouseEntered(e -> {
            btn.setStyle(baseStyle + "-fx-scale-x: 1.05; -fx-scale-y: 1.05;");
            btn.setScaleX(1.05);
            btn.setScaleY(1.05);
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(baseStyle);
            btn.setScaleX(1.0);
            btn.setScaleY(1.0);
        });

        // Shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        shadow.setRadius(10);
        shadow.setOffsetY(5);
        btn.setEffect(shadow);

        return btn;
    }

    private void openLevelSelect(Stage primaryStage) {
        LevelSelectView levelSelect = new LevelSelectView();
        levelSelect.show(primaryStage);
    }

    private void showHowToPlay() {
        Stage helpStage = new Stage();

        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_LEFT);
        root.setPadding(new Insets(25));

        // Gradient background
        LinearGradient gradient = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#2C3E50")),
                new Stop(1, Color.web("#34495E"))
        );
        BackgroundFill bgFill = new BackgroundFill(gradient, null, null);
        root.setBackground(new Background(bgFill));

        Label title = new Label("🎮 HOW TO PLAY");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#FFD700"));

        DropShadow titleShadow = new DropShadow();
        titleShadow.setColor(Color.BLACK);
        titleShadow.setRadius(5);
        title.setEffect(titleShadow);

        VBox controlsBox = createSection("CONTROLS",
                "W/A/S/D - Move chef\n" +
                        "Shift + W/A/S/D - Dash (3 tiles)\n" +
                        "C or V - Interact with station\n" +
                        "SPACE - Throw ingredient\n" +
                        "B - Switch between chefs\n" +
                        "ESC - Pause game\n" +
                        "Z - Undo last action\n" +
                        "Y - Redo action");

        VBox objectiveBox = createSection("OBJECTIVE",
                "Complete pizza orders before time runs out!\n" +
                        "Get 3 stars by reaching the target score\n" +
                        "Don't let orders expire or fail too many times!");

        VBox stationsBox = createSection("STATIONS",
                "C - Cutting Station (chop ingredients)\n" +
                        "R - Cooking Station (bake pizzas in oven)\n" +
                        "A - Assembly Station (combine ingredients)\n" +
                        "S - Serving Counter (deliver orders)\n" +
                        "W - Washing Station (clean dirty plates)\n" +
                        "I - Ingredient Storage (get ingredients)\n" +
                        "P - Plate Storage (get clean plates)\n" +
                        "T - Trash Station (discard mistakes)");

        VBox tipsBox = createSection("TIPS",
                "Use both chefs efficiently (press B to switch)\n" +
                        "Dash to move quickly between stations\n" +
                        "Throw ingredients to your partner chef\n" +
                        "Watch the order timers - don't let them expire!\n" +
                        "Pizzas must be BAKED before serving\n" +
                        "Don't leave pizzas in the oven too long or they'll burn!");

        Button closeBtn = createStyledButton("Close", "#E74C3C");
        closeBtn.setPrefWidth(200);
        closeBtn.setOnAction(e -> helpStage.close());

        VBox.setMargin(closeBtn, new Insets(10, 0, 0, 0));

        root.getChildren().addAll(title, controlsBox, objectiveBox, stationsBox, tipsBox, closeBtn);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #2C3E50; -fx-background-color: #2C3E50;");

        Scene scene = new Scene(scrollPane, 550, 650);
        helpStage.setTitle("How To Play");
        helpStage.setScene(scene);
        helpStage.show();
    }

    private VBox createSection(String title, String content) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.1); " +
                        "-fx-background-radius: 10; " +
                        "-fx-border-color: rgba(255, 255, 255, 0.2); " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 10;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web("#FFD700"));

        Label contentLabel = new Label(content);
        contentLabel.setFont(Font.font("Consolas", 13));
        contentLabel.setTextFill(Color.WHITE);
        contentLabel.setWrapText(true);

        box.getChildren().addAll(titleLabel, contentLabel);
        return box;
    }
}