package views;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import controllers.*;
import models.map.*;
import models.player.*;
import models.order.*;
import models.station.*;
import models.item.*;
import models.core.*;
import models.recipe.*;
import models.order.Order;
import models.enums.*;
import models.station.IngredientStorage;

import java.util.*;

public class GameView {
    private static final int TILE_SIZE = 50;
    private static final int MAP_WIDTH = GameMap.WIDTH * TILE_SIZE;
    private static final int MAP_HEIGHT = GameMap.HEIGHT * TILE_SIZE;

    // For testing (solid colours)
    private static final Color COLOR_WALL = Color.rgb(80, 80, 80);
    private static final Color COLOR_PLAYER_ACTIVE = Color.BLACK;
    private static final Color COLOR_PLAYER_INACTIVE = Color.rgb(60, 60, 60);
    private static final Color COLOR_FLOOR = Color.rgb(200, 180, 150);
    private static final Color COLOR_CUTTING = Color.RED;
    private static final Color COLOR_COOKING = Color.BLUE;
    private static final Color COLOR_ASSEMBLY = Color.GREEN;
    private static final Color COLOR_SERVING = Color.YELLOW;
    private static final Color COLOR_WASHING = Color.MAGENTA;
    private static final Color COLOR_INGREDIENT = Color.CYAN;
    private static final Color COLOR_PLATE = Color.PURPLE;
    private static final Color COLOR_TRASH = Color.ORANGE;

    private GameController gameController;
    private Canvas canvas;
    private GraphicsContext gc;
    private controllers.Stage gameStage;

    // HUD Components
    private VBox orderPanel;
    private Label scoreValueLabel;
    private Label timeValueLabel;
    private Label chefLabel;
    private Label dashCooldownLabel;


    private AnimationTimer gameLoop;
    private long lastUpdate = 0;

    private Map<String, Image> imageCache = new HashMap<>();
    private boolean useImages = false; // Set to TRUE when image resource is available

    public GameView(GameController controller) {
        this.gameController = controller;
        this.gameStage = controller.getStage();
        loadImages();
    }

    private void loadImages() {
//      try {
//            loadImage("pizza_margherita", "/images/pizzas/pizza_margherita.png");
//            loadImage("pizza_sosis", "/images/pizzas/pizza_sosis.png");
//            loadImage("pizza_ayam", "/images/pizzas/pizza_ayam.png");
//            loadImage("dough", "/images/ingredients/dough.png");
//            loadImage("tomato", "/images/ingredients/tomato. png");
//            loadImage("cheese", "/images/ingredients/cheese.png");
//            loadImage("sausage", "/images/ingredients/sausage.png");
//            loadImage("chicken", "/images/ingredients/chicken.png");
//            loadImage("cutting", "/images/stations/cutting.png");
//            loadImage("cooking", "/images/stations/cooking.png");
//            loadImage("assembly", "/images/stations/assembly. png");
//            loadImage("serving", "/images/stations/serving.png");
//            loadImage("washing", "/images/stations/washing.png");
//            loadImage("ingredient_storage", "/images/stations/ingredient_storage.png");
//            loadImage("plate_storage", "/images/stations/plate_storage.png");
//            loadImage("trash", "/images/stations/trash.png");
//            loadImage("wall", "/images/stations/wall.png");
//            loadImage("floor", "/images/stations/floor.png");
//            loadImage("chef1", "/images/players/chef1. png");
//            loadImage("chef2", "/images/players/chef2.png");
//            loadImage("score_icon", "/images/ui/score_icon.png");
//            loadImage("timer_icon", "/images/ui/timer_icon.png");
//        } catch (Exception e) {
//            useImages = false;
//        }
        useImages = false;
        System.out.println("[GameView] Using solid colors for testing");
    }

//    private void loadImage(String key, String path) {
//        try {
//            var resource = getClass().getResourceAsStream(path);
//            if (resource != null) {
//                Image img = new Image(resource, TILE_SIZE, TILE_SIZE, true, true);
//                imageCache.put(key, img);
//            } else {
//                System.out.println("Image not found: " + path);
//            }
//        } catch (Exception e) {
//            System.out. println("Failed to load image: " + path);
//        }
//    }

    private Image getImage(String key) {
        return imageCache.get(key);
    }

    private boolean hasImage(String key) {
        return imageCache.containsKey(key) && imageCache.get(key) != null;
    }

    public void show(Stage primaryStage) {
        VBox mainContainer = new VBox(0);
        mainContainer.setStyle("-fx-background-color: #1A1A1A;");

        HBox topSection = createTopSection();
        StackPane mapSection = createMapSection();
        HBox bottomSection = createBottomSection();

        mainContainer.getChildren().addAll(topSection, mapSection, bottomSection);

        Scene scene = new Scene(mainContainer, MAP_WIDTH, 700);

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                showPauseMenu(primaryStage);
            } else if (e.isShiftDown()) {
                gameController.handleDashInput(e.getCode(), true);
            } else {
                gameController.handleInput(e.getCode());
            }
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("NIMONSCOOKED - Playing");
        primaryStage.setResizable(false);

        startGameLoop(primaryStage);
        render();
    }

    private HBox createTopSection() {
        HBox topSection = new HBox(10);
        topSection.setPadding(new Insets(10));
        topSection.setStyle("-fx-background-color: #2A2A2A;");
        topSection.setPrefHeight(150);

        orderPanel = createOrderPanel();
        orderPanel.setPrefWidth((double) MAP_WIDTH / 2 - 10);
        orderPanel.setMaxWidth((double) MAP_WIDTH / 2 - 10);

        HBox rightSection = createScoreTimerSection();
        rightSection.setPrefWidth((double) MAP_WIDTH / 2 - 10);

        topSection.getChildren().addAll(orderPanel, rightSection);
        return topSection;
    }

    private VBox createOrderPanel() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #3A3A3A;" + "-fx-background-radius: 10;");

        Label title = new Label("ORDERS");
        title.setFont(Font.font("Inter" +
                "", FontWeight.BOLD, 14));
        title.setTextFill(Color.WHITE);
        panel.getChildren().add(title);

        return panel;
    }

    private HBox createScoreTimerSection() {
        HBox section = new HBox(10);
        section.setAlignment(Pos.CENTER);

        VBox scorePanel = createScorePanel();
        scorePanel.setPrefWidth((double) (MAP_WIDTH / 2 - 20) / 2);

        VBox timerPanel = createTimerPanel();
        timerPanel.setPrefWidth((double) (MAP_WIDTH / 2 - 20) / 2);

        section.getChildren().addAll(scorePanel, timerPanel);
        return section;
    }

    private VBox createScorePanel() {
        VBox panel = new VBox(5);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #3A3A3A;" + "-fx-background-radius: 10;");

//        HBox titleBox = new HBox(5);
//        titleBox.setAlignment(Pos.CENTER);
//
//        if (hasImage("score_icon")) {
//            ImageView icon = new ImageView(getImage("score_icon"));
//            icon.setFitWidth(24);
//            icon. setFitHeight(24);
//            titleBox.getChildren(). add(icon);
//        }

        Label titleLabel = new Label("SCORE");
        titleLabel.setFont(Font.font("Inter" +
                "", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.LIGHTGRAY);

        scoreValueLabel = new Label("$0");
        scoreValueLabel.setFont(Font.font("Inter" +
                "", FontWeight.BOLD, 32));
        scoreValueLabel.setTextFill(Color.GOLD);

        panel.getChildren().addAll(titleLabel, scoreValueLabel);
        return panel;
    }


    private VBox createTimerPanel() {
        VBox panel = new VBox(5);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #3A3A3A;" + "-fx-background-radius: 10;");

//        HBox titleBox = new HBox(5);
//        titleBox.setAlignment(Pos.CENTER);
//
//        if (hasImage("timer_icon")) {
//            ImageView icon = new ImageView(getImage("timer_icon"));
//            icon.setFitWidth(24);
//            icon.setFitHeight(24);
//            titleBox.getChildren().add(icon);
//        }

        Label titleLabel = new Label("TIME");
        titleLabel.setFont(Font.font("Inter" +
                "", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.LIGHTGRAY);
//        titleBox.getChildren(). add(titleLabel);

        timeValueLabel = new Label("3:00");
        timeValueLabel.setFont(Font.font("Inter" +
                "", FontWeight.BOLD, 32));
        timeValueLabel.setTextFill(Color.WHITE);

        panel.getChildren().addAll(titleLabel, timeValueLabel);
        return panel;
    }

    private StackPane createMapSection() {
        canvas = new Canvas(MAP_WIDTH, MAP_HEIGHT);
        gc = canvas.getGraphicsContext2D();

        StackPane mapContainer = new StackPane(canvas);
        mapContainer.setStyle("-fx-background-color: #000000;");
        mapContainer.setPadding(new Insets(5));

        return mapContainer;
    }

    private HBox createBottomSection() {
        HBox bottom = new HBox(20);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(10));
        bottom.setStyle("-fx-background-color: #2A2A2A;");

        Label controlsLabel = new Label("W/A/S/D: Move | Shift+WASD: Dash | SPACE: Throw | C/V: Interact | B: Switch Chef | ESC: Pause");
        controlsLabel.setFont(Font.font("Inter" +
                "", 12));
        controlsLabel.setTextFill(Color.LIGHTGRAY);

        chefLabel = new Label("Active: Chef 1");
        chefLabel.setFont(Font.font("Inter" +
                "", FontWeight.BOLD, 14));
        chefLabel.setTextFill(Color.LIGHTGREEN);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        dashCooldownLabel = new Label("Dash: Ready");
        dashCooldownLabel.setFont(Font.font("Inter" +
                "", FontWeight.BOLD, 12));
        dashCooldownLabel.setTextFill(Color.LIGHTGREEN);
        bottom.getChildren().add(dashCooldownLabel);
        bottom.getChildren().addAll(controlsLabel, spacer, chefLabel);
        return bottom;
    }

    private void startGameLoop(Stage primaryStage) {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 1_000_000_000) {
                    lastUpdate = now;

                    if (!gameController.isPaused() && gameStage.isGameRunning()) {
                        gameStage.update();
                        updateHUD();

                        if (!gameStage.isGameRunning()) {
                            this.stop();
                            showResultScreen(primaryStage);
                        }
                    }
                }
                render();
            }
        };
        gameLoop.start();
    }

    private void updateHUD() {
        scoreValueLabel.setText("$" + gameStage.getScore());

        int time = gameStage.getTimeRemaining();
        int mins = time / 60;
        int secs = time % 60;
        timeValueLabel.setText(String.format("%d:%02d", mins, secs));

        if (time <= 30) {
            timeValueLabel.setTextFill(Color.RED);
        } else if (time <= 60) {
            timeValueLabel.setTextFill(Color.YELLOW);
        } else {
            timeValueLabel.setTextFill(Color.WHITE);
        }

        ChefPlayer activeChef = gameStage.getActiveChef();
        if (activeChef != null) {
            long cooldown = activeChef.getDashCooldownRemaining();
            if (cooldown > 0) {
                dashCooldownLabel.setText("Dash: " + (cooldown / 1000.0) + "s");
                dashCooldownLabel.setTextFill(Color.RED);
            } else {
                dashCooldownLabel.setText("Dash: Ready [Shift+WASD]");
                dashCooldownLabel.setTextFill(Color.LIGHTGREEN);
            }
        }

        updateOrderPanel();
    }

    private void updateOrderPanel() {
        if (orderPanel.getChildren().size() > 1) {
            orderPanel.getChildren().remove(1, orderPanel.getChildren().size());
        }

        List<Order> orders = gameStage.getAllOrders();

        HBox ordersContainer = new HBox(8);
        ordersContainer.setAlignment(Pos.CENTER_LEFT);

        for (Order order : orders) {
            VBox orderBox = createOrderBox(order);
            ordersContainer.getChildren().add(orderBox);
        }

        orderPanel.getChildren().add(ordersContainer);
    }

    private VBox createOrderBox(Order order) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(8));
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(100);
        box.setStyle("-fx-background-color: #4A4A4A;" + "-fx-background-radius: 8;");
//        String pizzaKey = getPizzaImageKey(order. getRecipe().getName());
//        if (hasImage(pizzaKey)) {
//            ImageView pizzaImg = new ImageView(getImage(pizzaKey));
//            pizzaImg.setFitWidth(40);
//            pizzaImg.setFitHeight(40);
//            box.getChildren(). add(pizzaImg);
//        } else {
        Label pizzaIcon = new Label("[PIZZA]");
        pizzaIcon.setFont(Font.font("Inter" +
                "", FontWeight.BOLD, 10));
        pizzaIcon.setTextFill(Color.ORANGE);
//        box.getChildren().add(pizzaIcon);
//        }
        Label nameLabel = new Label(getShortName(order.getRecipe().getName()));
        nameLabel.setFont(Font.font("Inter" +
                "", FontWeight.BOLD, 10));
        nameLabel.setTextFill(Color.WHITE);

        int timeLeft = gameStage.getOrderTimeRemaining(order);
        double progress = gameStage.getOrderTimeProgress(order);

        Label timeLabel = new Label("Time: " + timeLeft + "s");
        timeLabel.setFont(Font.font("Inter" +
                "", FontWeight.BOLD, 11));
        timeLabel.setTextFill(getProgressColor(progress));

        HBox ingredientsBox = createIngredientsIcons(order.getRecipe());
        StackPane progressBar = createProgressBar(progress, 80);
        box.getChildren().addAll(nameLabel, timeLabel, ingredientsBox, progressBar);

        return box;
    }

//    private String getPizzaImageKey(String recipeName) {
//        return switch (recipeName. toLowerCase()) {
//            case "pizza margherita" -> "pizza_margherita";
//            case "pizza sosis" -> "pizza_sosis";
//            case "pizza ayam" -> "pizza_ayam";
//            default -> "pizza_margherita";
//        };
//    }

    private String getShortName(String fullName) {
        return fullName.replace("Pizza ", "P.");
    }

    private HBox createIngredientsIcons(Recipe recipe) {
        HBox icons = new HBox(2);
        icons.setAlignment(Pos.CENTER);

        List<RecipeIngredientRequirement> requirements = recipe.getRequiredComponents();

        for (RecipeIngredientRequirement req : requirements) {
            String ingredientName = req.getIngredientType().getSimpleName();
            String shortName = ingredientName.substring(0, Math.min(2, ingredientName.length())).toUpperCase();

            Label iconLabel = new Label("[" + shortName + "]");
            iconLabel.setFont(Font.font("Inter" +
                    "", 8));
            iconLabel.setTextFill(Color.LIGHTGRAY);
            icons.getChildren().add(iconLabel);
        }

        return icons;
//        for (RecipeIngredientRequirement req : requirements) {
//            String ingredientName = req.getIngredientType(). getSimpleName(). toLowerCase();
//            if (hasImage(ingredientName)) {
//                ImageView icon = new ImageView(getImage(ingredientName));
//                icon.setFitWidth(16);
//                icon. setFitHeight(16);
//                icons.getChildren().add(icon);
//            } else {
//                String emoji = getIngredientEmoji(ingredientName);
//                Label iconLabel = new Label(emoji);
//                iconLabel.setFont(Font.font("Inter
//                ", 12));
//                icons.getChildren().add(iconLabel);
//            }
//        }
    }

    private StackPane createProgressBar(double progress, double width) {
        StackPane bar = new StackPane();
        bar.setAlignment(Pos.CENTER_LEFT);

        Rectangle bg = new Rectangle(width, 6);
        bg.setFill(Color.rgb(80, 80, 80));
        bg.setArcWidth(4);
        bg.setArcHeight(4);

        Rectangle fill = new Rectangle(width * progress, 6);
        fill.setFill(getProgressColorFX(progress));
        fill.setArcWidth(4);
        fill.setArcHeight(4);

        bar.getChildren().addAll(bg, fill);
        StackPane.setAlignment(fill, Pos.CENTER_LEFT);

        return bar;
    }

    private Color getProgressColor(double progress) {
        if (progress > 0.5) return Color.LIGHTGREEN;
        else if (progress > 0.25) return Color.YELLOW;
        else return Color.RED;
    }

    private Color getProgressColorFX(double progress) {
        if (progress > 0.5) return Color.rgb(46, 204, 113);
        else if (progress > 0.25) return Color.rgb(241, 196, 15);
        else return Color.rgb(231, 76, 60);
    }

    private void render() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        GameMap map = gameStage.getGameMap();

        drawMap(map);
        drawPlayers();
    }

    private void drawMap(GameMap map) {
        char[][] grid = map.getGrid();
        Map<Position, Station> stations = map.getAllStations();

        for (int y = 0; y < GameMap.HEIGHT; y++) {
            for (int x = 0; x < GameMap.WIDTH; x++) {
                int drawX = x * TILE_SIZE;
                int drawY = y * TILE_SIZE;
                char tile = grid[y][x];
                Position pos = new Position(x, y);
                Station station = stations.get(pos);

                Color tileColor;
                if (tile == 'X') {
                    tileColor = COLOR_WALL;
                } else if (station != null) {
                    tileColor = getStationColor(station);
                    System.out.println("[RENDER] Station at (" + x + ", " + y + "): " +
                            station.getType() + " -> Color: " + colorToString(tileColor));
                } else {
                    tileColor = COLOR_FLOOR;
                }

                gc.setFill(tileColor);
                gc.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);

                gc.setStroke(Color.rgb(100, 100, 100));
                gc.setLineWidth(1);
                gc.strokeRect(drawX, drawY, TILE_SIZE, TILE_SIZE);

                if (station != null) {
                    drawStationLabel(drawX, drawY, station);
                }
            }
        }
    }

    private String colorToString(Color color) {
        return "RGB(" +
                (int) (color.getRed() * 255) + ", " +
                (int) (color.getGreen() * 255) + ", " +
                (int) (color.getBlue() * 255) + ")";
    }

    private void drawStationLabel(int x, int y, Station station) {
        String label;
        Color labelColor = Color.WHITE;

        switch (station.getType()) {
            case CUTTING -> {
                label = "CUTTING";
                labelColor = Color.WHITE;
            }
            case COOKING -> {
                label = "OVEN";
                labelColor = Color.ORANGE;
            }
            case ASSEMBLY -> {
                label = "ASSEMBLY";
                labelColor = Color.LIGHTGREEN;
            }
            case SERVING_COUNTER -> {
                label = "SERVE";
                labelColor = Color.GOLD;
            }
            case WASHING -> {
                label = "WASH";
                labelColor = Color.LIGHTBLUE;
            }
            case INGREDIENT_STORAGE -> {
                if (station instanceof IngredientStorage storage) {
                    IngredientType type = storage.getIngredientType();
                    label = switch (type) {
                        case DOUGH -> "DOUGH";
                        case TOMATO -> "TOMATO";
                        case CHEESE -> "CHEESE";
                        case SAUSAGE -> "SAUSAGE";
                        case CHICKEN -> "CHICKEN";
                    };
                    labelColor = Color.ORANGE;
                } else {
                    label = "INGRED";
                }
            }
            case PLATE_STORAGE -> {
                label = "PLATES";
                labelColor = Color.LIGHTGRAY;
            }
            case TRASH -> {
                label = "TRASH";
                labelColor = Color.RED;
            }
            default -> label = "?";
        }

        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(x + 2, y + TILE_SIZE - 18, TILE_SIZE - 4, 16);

        // Draw label text
        gc.setFill(labelColor);
        gc.setFont(Font.font("Inter" +
                "", FontWeight.BOLD, 9));
        gc.fillText(label, x + 4, y + TILE_SIZE - 6);
    }

    private void drawChefInventory(ChefPlayer chef, int x, int y) {
        if (chef.hasItem()) {
            Item item = chef.getInventory();
            String itemName = item.getName();

            gc.setFill(Color.rgb(0, 0, 0, 0.8));
            gc.fillRect(x - 5, y + TILE_SIZE + 2, TILE_SIZE + 10, 18);

            gc.setFill(Color.YELLOW);
            gc.setFont(Font.font("Inter" +
                    "", FontWeight.BOLD, 10));
            gc.fillText(itemName.substring(0, Math.min(8, itemName.length())), x, y + TILE_SIZE + 14);
        }
    }

//    private void drawTileWithFallback(int x, int y, String imageKey, Color fallbackColor) {
//        if (useImages && hasImage(imageKey)) {
//            gc.drawImage(getImage(imageKey), x, y, TILE_SIZE, TILE_SIZE);
//        } else {
//            gc.setFill(fallbackColor);
//            gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
//        }
//    }

//    private String getStationImageKey(Station station) {
//        return switch (station.getType()) {
//            case CUTTING -> "cutting";
//            case COOKING -> "cooking";
//            case ASSEMBLY -> "assembly";
//            case SERVING_COUNTER -> "serving";
//            case WASHING -> "washing";
//            case INGREDIENT_STORAGE -> "ingredient_storage";
//            case PLATE_STORAGE -> "plate_storage";
//            case TRASH -> "trash";
//            default -> "floor";
//        };
//    }

    private Color getStationColor(Station station) {
        return switch (station.getType()) {
            case CUTTING -> COLOR_CUTTING;
            case COOKING -> COLOR_COOKING;
            case ASSEMBLY -> COLOR_ASSEMBLY;
            case SERVING_COUNTER -> COLOR_SERVING;
            case WASHING -> COLOR_WASHING;
            case INGREDIENT_STORAGE -> COLOR_INGREDIENT;
            case PLATE_STORAGE -> COLOR_PLATE;
            case TRASH -> COLOR_TRASH;
//            default -> COLOR_FLOOR;
        };
    }
//
//    private void drawStationLabel(int x, int y, Station station) {
//
//        String label = switch (station.getType()) {
//            case CUTTING -> "C";
//            case COOKING -> "R";
//            case ASSEMBLY -> "A";
//            case SERVING_COUNTER -> "S";
//            case WASHING -> "W";
//            case INGREDIENT_STORAGE -> "I";
//            case PLATE_STORAGE -> "P";
//            case TRASH -> "T";
//            default -> "? ";
//        };
//
//        gc.setFill(Color.WHITE);
//        gc. setFont(Font. font("Inter
//        ", FontWeight.BOLD, 14));
//        gc. fillText(label, x + (double) TILE_SIZE / 2 - 5, y + (double) TILE_SIZE / 2 + 5);
//    }

    private void drawPlayers() {
        List<ChefPlayer> chefs = gameStage.getChefs();
        ChefPlayer activeChef = gameStage.getActiveChef();

        for (ChefPlayer chef : chefs) {
            int x = chef.getPosition().getX() * TILE_SIZE;
            int y = chef.getPosition().getY() * TILE_SIZE;
            boolean isActive = chef == activeChef;

            gc.setFill(isActive ? COLOR_PLAYER_ACTIVE : COLOR_PLAYER_INACTIVE);
            int padding = 8;
            gc.fillOval(x + padding, y + padding, TILE_SIZE - 2 * padding, TILE_SIZE - 2 * padding);

            if (isActive) {
                gc.setStroke(Color.YELLOW);
                gc.setLineWidth(3);
                gc.strokeOval(x + padding - 2, y + padding - 2,
                        TILE_SIZE - 2 * padding + 4, TILE_SIZE - 2 * padding + 4);
            }
            gc.setFill(Color.WHITE);
            drawDirectionIndicator(chef, x, y);
            gc.setFont(Font.font("Inter" +
                    "", FontWeight.BOLD, 10));
            String label = isActive ? "★ " + chef.getName() : chef.getName();
            gc.fillText(label, x + 5, y - 5);
            drawChefInventory(chef, x, y);
        }
    }

    private void drawDirectionIndicator(ChefPlayer chef, int x, int y) {
        int centerX = x + TILE_SIZE / 2;
        int centerY = y + TILE_SIZE / 2;
        int size = 6;

        double[] xPoints, yPoints;

        switch (chef.getDirection()) {
            case UP -> {
                xPoints = new double[]{centerX, centerX - size, centerX + size};
                yPoints = new double[]{centerY - size - 2, centerY + 2, centerY + 2};
            }
            case DOWN -> {
                xPoints = new double[]{centerX, centerX - size, centerX + size};
                yPoints = new double[]{centerY + size + 2, centerY - 2, centerY - 2};
            }
            case LEFT -> {
                xPoints = new double[]{centerX - size - 2, centerX + 2, centerX + 2};
                yPoints = new double[]{centerY, centerY - size, centerY + size};
            }
            case RIGHT -> {
                xPoints = new double[]{centerX + size + 2, centerX - 2, centerX - 2};
                yPoints = new double[]{centerY, centerY - size, centerY + size};
            }
            default -> {
                return;
            }
        }

        gc.fillPolygon(xPoints, yPoints, 3);
    }

    private void showPauseMenu(Stage primaryStage) {
        gameController.togglePause();

        if (!gameController.isPaused()) return;

        Stage pauseStage = new Stage();

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #2D2D2D;");

        Label title = new Label("PAUSED");
        title.setFont(Font.font("Inter" +
                "", FontWeight.BOLD, 32));
        title.setTextFill(Color.WHITE);

        Button resumeBtn = createPauseButton("Resume");
        Button restartBtn = createPauseButton("Restart");
        Button quitBtn = createPauseButton("Quit to Menu");

        resumeBtn.setOnAction(e -> {
            gameController.togglePause();
            pauseStage.close();
        });

        restartBtn.setOnAction(e -> {
            pauseStage.close();
            gameLoop.stop();
            gameController.startLevel(gameController.getLevelManager().getCurrentLevel());
            gameStage = gameController.getStage();
            startGameLoop(primaryStage);
        });

        quitBtn.setOnAction(e -> {
            pauseStage.close();
            gameLoop.stop();
            MainMenuView mainMenu = new MainMenuView();
            mainMenu.start(primaryStage);
        });

        root.getChildren().addAll(title, resumeBtn, restartBtn, quitBtn);

        Scene scene = new Scene(root, 300, 280);
        pauseStage.setScene(scene);
        pauseStage.setTitle("Paused");
        pauseStage.show();

        pauseStage.setOnCloseRequest(e -> gameController.togglePause());
    }

    private Button createPauseButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(200);
        btn.setFont(Font.font("Inter" +
                "", FontWeight.BOLD, 14));
        btn.setStyle(
                "-fx-background-color: #4682B4;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 5;"
        );
        return btn;
    }

    private void showResultScreen(Stage primaryStage) {
        gameLoop.stop();
        ResultView resultView = new ResultView(gameController);
        resultView.show(primaryStage);
    }
}
