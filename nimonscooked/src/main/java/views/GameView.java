package views;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
import models.item.kitchenutensils.Plate;
import utils.ImageManager;

import java.util.*;

public class GameView {
    private static final int TILE_SIZE = 50;
    private static final int MAP_WIDTH = GameMap.WIDTH * TILE_SIZE;
    private static final int MAP_HEIGHT = GameMap.HEIGHT * TILE_SIZE;

    // Fallback colors
    private static final Color COLOR_WALL = Color.rgb(80, 80, 80);
    private static final Color COLOR_FLOOR = Color.rgb(200, 180, 150);
    private static final Color COLOR_CUTTING = Color.RED;
    private static final Color COLOR_COOKING = Color.BLUE;
    private static final Color COLOR_ASSEMBLY = Color.GREEN;
    private static final Color COLOR_SERVING = Color.YELLOW;
    private static final Color COLOR_WASHING = Color.MAGENTA;
    private static final Color COLOR_INGREDIENT = Color.CYAN;
    private static final Color COLOR_PLATE = Color.PURPLE;
    private static final Color COLOR_TRASH = Color.ORANGE;

    private final GameController gameController;
    private Canvas canvas;
    private GraphicsContext gc;
    private controllers.Stage gameStage;
    private ImageManager imageManager;

    // HUD Components
    private VBox orderPanel;
    private Label scoreValueLabel;
    private Label timeValueLabel;
    private Label chefLabel;
    private Label dashCooldownLabel;

    private AnimationTimer gameLoop;
    private long lastUpdate = 0;

    public GameView(GameController controller) {
        this.gameController = controller;
        this.gameStage = controller.getStage();
        this.imageManager = ImageManager.getInstance();
    }

    // ==================== SHOW ====================

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
                return;
            }

            // Check if Shift is held down for dash
            if (e.isShiftDown()) {
                // This is a dash input - handle WASD with shift
                switch (e.getCode()) {
                    case W, A, S, D -> {
                        gameController.handleDashInput(e.getCode(), true);
                        return; // Don't process as normal movement
                    }
                }
            }

            // Normal input (no shift)
            gameController.handleInput(e.getCode());
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("WEARECOOKED - Playing");
        primaryStage.setResizable(false);

        startGameLoop(primaryStage);
        render();
    }

    // ==================== HUD CREATION ====================

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
        panel.setStyle("-fx-background-color: #3A3A3A; -fx-background-radius: 10;");

        Label title = new Label("ORDERS");
        title.setFont(Font.font("Inter", FontWeight.BOLD, 14));
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
        panel.setStyle("-fx-background-color: #3A3A3A; -fx-background-radius: 10;");

        Label titleLabel = new Label("SCORE");
        titleLabel.setFont(Font.font("Inter", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.LIGHTGRAY);

        scoreValueLabel = new Label("$0");
        scoreValueLabel.setFont(Font.font("Inter", FontWeight.BOLD, 32));
        scoreValueLabel.setTextFill(Color.GOLD);

        panel.getChildren().addAll(titleLabel, scoreValueLabel);
        return panel;
    }

    private VBox createTimerPanel() {
        VBox panel = new VBox(5);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #3A3A3A; -fx-background-radius: 10;");

        Label titleLabel = new Label("TIME");
        titleLabel.setFont(Font.font("Inter", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.LIGHTGRAY);

        timeValueLabel = new Label("3:00");
        timeValueLabel.setFont(Font.font("Inter", FontWeight.BOLD, 32));
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

        Label controlsLabel = new Label("W/A/S/D: Move | Shift+WASD: Dash | SPACE: Throw | C: Pickup/Drop | X: Interact | B: Switch Chef | ESC: Pause");
        controlsLabel.setFont(Font.font("Inter", 12));
        controlsLabel.setTextFill(Color.LIGHTGRAY);

        chefLabel = new Label("Active: Chef 1");
        chefLabel.setFont(Font.font("Inter", FontWeight.BOLD, 14));
        chefLabel.setTextFill(Color.LIGHTGREEN);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        dashCooldownLabel = new Label("Dash: Ready");
        dashCooldownLabel.setFont(Font.font("Inter", FontWeight.BOLD, 12));
        dashCooldownLabel.setTextFill(Color.LIGHTGREEN);

        bottom.getChildren().addAll(controlsLabel, spacer, dashCooldownLabel, chefLabel);
        return bottom;
    }

    // ==================== GAME LOOP ====================

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

    // ==================== HUD UPDATE ====================

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
            String chefStatus = "Active: " + activeChef.getName();
            if (activeChef.isBusy() && !activeChef.isMoving()) {
                CurrentAction action = activeChef.getCurrentAction();
                int remaining = activeChef.getBusyTimeRemaining();
                String actionName = switch (action) {
                    case CUTTING -> "Cutting";
                    case COOKING -> "Cooking";
                    case WASHING -> "Washing";
                    default -> "Busy";
                };
                chefStatus += " (" + actionName + " " + remaining + "s)";
                chefLabel.setTextFill(Color.ORANGE);
            } else {
                chefLabel.setTextFill(Color.LIGHTGREEN);
            }
            chefLabel.setText(chefStatus);

            long cooldown = activeChef.getDashCooldownRemaining();
            if (cooldown > 0) {
                dashCooldownLabel.setText("Dash: " + String.format("%.1f", cooldown / 1000.0) + "s");
                dashCooldownLabel.setTextFill(Color.RED);
            } else {
                dashCooldownLabel.setText("Dash: Ready");
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
        box.setStyle("-fx-background-color: #4A4A4A; -fx-background-radius: 8;");

        String pizzaImageKey = getPizzaImageKeyFromRecipe(order.getRecipe());
        if (imageManager.isUsingImages() && pizzaImageKey != null && imageManager.hasImage(pizzaImageKey)) {
            ImageView pizzaView = new ImageView(imageManager.getImage(pizzaImageKey));
            pizzaView.setFitWidth(50);
            pizzaView.setFitHeight(50);
            pizzaView.setPreserveRatio(true);
            box.getChildren().add(pizzaView);
        }


        Label nameLabel = new Label(getShortName(order.getRecipe().getName()));
        nameLabel.setFont(Font.font("Inter", FontWeight.BOLD, 10));
        nameLabel.setTextFill(Color.WHITE);

        int timeLeft = gameStage.getOrderTimeRemaining(order);
        double progress = gameStage.getOrderTimeProgress(order);

        Label timeLabel = new Label("Time: " + timeLeft + "s");
        timeLabel.setFont(Font.font("Inter", FontWeight.BOLD, 11));
        timeLabel.setTextFill(getProgressColor(progress));

        HBox ingredientsBox = createIngredientsIcons(order.getRecipe());
        StackPane progressBar = createProgressBar(progress, 80);

        box.getChildren().addAll(nameLabel, timeLabel, ingredientsBox, progressBar);

        return box;
    }

    private String getPizzaImageKeyFromRecipe(Recipe recipe) {
        String recipeName = recipe.getName().toLowerCase();

        if (recipeName.contains("margherita")) {
            return "pizza_margherita";
        } else if (recipeName.contains("sosis") || recipeName.contains("sausage")) {
            return "pizza_sosis";
        } else if (recipeName.contains("ayam") || recipeName.contains("chicken")) {
            return "pizza_ayam";
        }

        return null; // Unknown pizza type
    }

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
            iconLabel.setFont(Font.font("Inter", 8));
            iconLabel.setTextFill(Color.LIGHTGRAY);
            icons.getChildren().add(iconLabel);
        }

        return icons;
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

    // ==================== RENDER ====================

    private void render() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        GameMap map = gameStage.getGameMap();

        // Update all chef movements for smooth animation
        for (ChefPlayer chef : gameStage.getChefs()) {
            chef.updateMovement();
        }

        drawMap(map);
        drawFloorItems();
        drawPlayers();
    }

    // ==================== DRAW MAP ====================

    private void drawMap(GameMap map) {
        char[][] grid = map.getGrid();
        Map<Position, Station> stations = map.getAllStations();

        Set<WashingStation> drawnWashingStations = new HashSet<>();

        for (int y = 0; y < GameMap.HEIGHT; y++) {
            for (int x = 0; x < GameMap.WIDTH; x++) {
                int drawX = x * TILE_SIZE;
                int drawY = y * TILE_SIZE;
                char tile = grid[y][x];
                Position pos = new Position(x, y);
                Station station = stations.get(pos);

                // Draw tile
                if (tile == 'X') {
                    drawTileWithFallback(drawX, drawY, "wall", COLOR_WALL);
                } else if (station != null) {
                    if (station instanceof WashingStation washStation) {
                        if (!drawnWashingStations.contains(washStation)) {
                            // Draw both blocks of the washing station
                            Position washPos = washStation.getWashPosition();
                            Position cleanPos = washStation.getCleanPosition();

                            // Draw wash side
                            drawTileWithFallback(washPos.getX() * TILE_SIZE, washPos.getY() * TILE_SIZE,
                                    "station_washing", COLOR_WASHING);
                            drawWashingStationVisual(washPos.getX() * TILE_SIZE, washPos.getY() * TILE_SIZE,
                                    washStation);

                            // Draw clean side
                            drawTileWithFallback(cleanPos.getX() * TILE_SIZE, cleanPos.getY() * TILE_SIZE,
                                    "station_washing", COLOR_WASHING);
                            drawWashingStationVisual(cleanPos.getX() * TILE_SIZE, cleanPos.getY() * TILE_SIZE,
                                    washStation);

                            drawnWashingStations.add(washStation);
                        }
                    } else {
                        // Regular station rendering
                        String stationKey = getStationImageKey(station);
                        Color fallbackColor = getStationColor(station);
                        drawTileWithFallback(drawX, drawY, stationKey, fallbackColor);
                        drawStationLabel(drawX, drawY, station);
                    }
                } else if (tile == '.' || tile == 'V') {
                    drawTileWithFallback(drawX, drawY, "floor", COLOR_FLOOR);
                }

                // Draw grid lines
                gc.setStroke(Color.rgb(100, 100, 100, 0.5));
                gc.setLineWidth(1);
                gc.strokeRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    private void drawTileWithFallback(int x, int y, String imageKey, Color fallbackColor) {
        if (imageManager.isUsingImages() && imageManager.hasImage(imageKey)) {
            gc.drawImage(imageManager.getImage(imageKey), x, y, TILE_SIZE, TILE_SIZE);
        } else {
            gc.setFill(fallbackColor);
            gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
        }
    }

    private String getStationImageKey(Station station) {
        if (station.getType() == StationType.INGREDIENT_STORAGE) {
            // Get specific ingredient type
            if (station instanceof IngredientStorage storage) {
                IngredientType type = storage.getIngredientType();
                return switch (type) {
                    case TOMATO -> "station_ingredient_tomato";
                    case CHEESE -> "station_ingredient_cheese";
                    case DOUGH -> "station_ingredient_dough";
                    case SAUSAGE -> "station_ingredient_sausage";
                    case CHICKEN -> "station_ingredient_chicken";
                };
            }
            return "station_ingredient";
        }

        return switch (station.getType()) {
            case CUTTING -> "station_cutting";
            case COOKING -> "station_cooking";
            case ASSEMBLY -> "station_assembly";
            case SERVING_COUNTER -> "station_serving";
            case WASHING -> "station_washing";
            case PLATE_STORAGE -> "station_plate";
            case TRASH -> "station_trash";
            default -> "floor";
        };
    }

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
        };
    }

    private void drawStationLabel(int x, int y, Station station) {

        String label = "";
        Color labelColor = Color.WHITE;

        if (station instanceof AssemblyStation assembly) {
            drawAssemblyStationVisual(x, y, assembly);
            return;
        } else if (station instanceof CuttingStation cutting) {
            drawCuttingStationVisual(x, y, cutting);
            return;
        } else if (station instanceof WashingStation washStation) {
            return;
        }

        switch (station.getType()) {
            case COOKING -> {
                label = "OVEN";
                labelColor = Color.ORANGE;
            }
            case SERVING_COUNTER -> {
                label = "SERVE";
                labelColor = Color.GOLD;
            }
            case INGREDIENT_STORAGE -> {
                if (station instanceof IngredientStorage storage) {
                    // Draw the station image/color first
                    // Then draw any plate/ingredients on top
                    drawIngredientStorageVisual(x, y, storage);
                    return;
                }
            }
            case PLATE_STORAGE -> {
                label = "PLATE";
                labelColor = Color.LIGHTGRAY;
            }
            case TRASH -> {
                label = "TRASH";
                labelColor = Color.RED;
            }
            default -> label = "?";
        }

        // Draw label background
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(x + 2, y + TILE_SIZE - 16, TILE_SIZE - 4, 14);

        // Draw label text
        gc.setFill(labelColor);
        gc.setFont(Font.font("Inter", FontWeight.BOLD, 9));
        gc.fillText(label, x + 4, y + TILE_SIZE - 5);
    }

    private void drawWashingStationVisual(int x, int y, WashingStation washStation) {
        Position currentPos = new Position(x / TILE_SIZE, y / TILE_SIZE);
        Position washPos = washStation.getWashPosition();
        Position cleanPos = washStation.getCleanPosition();

        boolean isWashSide = currentPos.equals(washPos);
        boolean isCleanSide = currentPos.equals(cleanPos);

        if (isWashSide) {
            // Draw WASH side
            int centerX = x + TILE_SIZE / 2;
            int centerY = y + TILE_SIZE / 2;

            // Draw dirty plate being washed
            if (washStation.hasDirtyPlate()) {
                Plate dirtyPlate = washStation.getDirtyPlateBeingWashed();

                if (imageManager.isUsingImages() && imageManager.hasImage("plate_dirty")) {
                    gc.drawImage(imageManager.getImage("plate_dirty"), centerX - 20, centerY - 20, 40, 40);
                } else {
                    gc.setFill(Color.GRAY);
                    gc.fillOval(centerX - 20, centerY - 20, 40, 40);
                }

                // Draw progress bar if washing
                double progress = washStation.getWashProgressPercent();
                if (progress > 0) {
                    drawProgressBar(centerX - 20, centerY + 25, 40, progress);
                }
            }

            // Draw label
            gc.setFill(Color.rgb(0, 0, 0, 0.7));
            gc.fillRect(x + 2, y + TILE_SIZE - 16, TILE_SIZE - 4, 14);
            gc.setFill(Color.LIGHTBLUE);
            gc.setFont(Font.font("Inter", FontWeight.BOLD, 9));
            gc.fillText("WASH", x + 4, y + TILE_SIZE - 5);

        } else if (isCleanSide) {
            // Draw CLEAN side
            int centerX = x + TILE_SIZE / 2;
            int centerY = y + TILE_SIZE / 2;

            // Draw stack of clean plates
            int cleanCount = washStation.getCleanPlateCount();
            if (cleanCount > 0) {
                // Draw stacked plates
                for (int i = 0; i < Math.min(cleanCount, 3); i++) {
                    int offsetY = i * 3;

                    if (imageManager.isUsingImages() && imageManager.hasImage("plate_empty")) {
                        gc.drawImage(imageManager.getImage("plate_empty"),
                                centerX - 18, centerY - 18 - offsetY, 36, 36);
                    } else {
                        gc.setFill(Color.WHITE);
                        gc.fillOval(centerX - 18, centerY - 18 - offsetY, 36, 36);
                    }
                }

                // Draw count
                if (cleanCount > 1) {
                    gc.setFill(Color.rgb(0, 0, 0, 0.8));
                    gc.fillRoundRect(x + TILE_SIZE - 20, y + 5, 15, 15, 3, 3);
                    gc.setFill(Color.WHITE);
                    gc.setFont(Font.font("Inter", FontWeight.BOLD, 10));
                    gc.fillText(String.valueOf(cleanCount), x + TILE_SIZE - 16, y + 16);
                }
            }

            // Draw label
            gc.setFill(Color.rgb(0, 0, 0, 0.7));
            gc.fillRect(x + 2, y + TILE_SIZE - 16, TILE_SIZE - 4, 14);
            gc.setFill(Color.LIGHTGREEN);
            gc.setFont(Font.font("Inter", FontWeight.BOLD, 9));
            gc.fillText("CLEAN", x + 4, y + TILE_SIZE - 5);
        }
    }

    // Helper to draw progress bar
    private void drawProgressBar(int x, int y, int width, double progress) {
        // Background
        gc.setFill(Color.rgb(60, 60, 60));
        gc.fillRoundRect(x, y, width, 6, 3, 3);

        // Progress
        gc.setFill(Color.rgb(30, 144, 255));
        gc.fillRoundRect(x, y, width * progress, 6, 3, 3);
    }

    private void drawIngredientStorageVisual(int x, int y, IngredientStorage storage) {
        int centerX = x + TILE_SIZE / 2;
        int centerY = y + TILE_SIZE / 2;

        // Draw plate first if exists
        if (storage.hasPlate()) {
            Plate plate = storage.getPlateOnStation();

            if (imageManager.isUsingImages() && imageManager.hasImage("plate_empty")) {
                gc.drawImage(imageManager.getImage("plate_empty"), centerX - 20, centerY - 20, 40, 40);
            } else {
                gc.setFill(Color.WHITE);
                gc.fillOval(centerX - 20, centerY - 20, 40, 40);
            }

            // Check if plate has dish with ingredients (moved inside the hasPlate block)
            if (plate.hasDish()) {
                Dish dish = plate.getDish();
                List<Preparable> dishComponents = dish.getComponents();

                if (!dishComponents.isEmpty()) {
                    List<Ingredient> ingredientsToRender = new ArrayList<>();
                    for (Preparable p : dishComponents) {
                        if (p instanceof Ingredient ing) {
                            ingredientsToRender.add(ing);
                        }
                    }

                    if (!ingredientsToRender.isEmpty()) {
                        drawStackedIngredients(centerX, centerY, ingredientsToRender);
                    }
                }
            }
        }

        // Draw ingredients on top
        List<Ingredient> ingredients = storage.getIngredientsOnStation();
        if (!ingredients.isEmpty()) {
            drawStackedIngredients(centerX, centerY, ingredients);
        }

        // Draw label
        IngredientType type = storage.getIngredientType();
        String label = type.name().substring(0, Math.min(4, type.name().length()));
        drawStationLabelOnly(x, y, label, Color.ORANGE);
    }

    // Draw Assembly Station with visual stacking
    private void drawAssemblyStationVisual(int x, int y, AssemblyStation assembly) {
        int centerX = x + TILE_SIZE / 2;
        int centerY = y + TILE_SIZE / 2;

        // Draw plate first (di bawah)
        if (assembly.hasPlate()) {
            Plate plate = assembly.getPlateOnStation();

            if (imageManager.isUsingImages() && imageManager.hasImage("plate_empty")) {
                gc.drawImage(imageManager.getImage("plate_empty"), centerX - 20, centerY - 20, 40, 40);
            } else {
                // Fallback: draw circle for plate
                gc.setFill(Color.WHITE);
                gc.fillOval(centerX - 20, centerY - 20, 40, 40);
            }

            // Check if plate has a dish with ingredients
            if (plate.hasDish()) {
                Dish dish = plate.getDish();

                // If it's a finished baked pizza, show pizza image
                if (dish instanceof PizzaDish pizza && pizza.isBaked()) {
                    drawFinishedPizza(centerX, centerY, pizza);
                    drawStationLabelOnly(x, y, "PIZZA", Color.ORANGE);
                    return;
                }

                // Otherwise, show stacked ingredients from the dish
                List<Preparable> dishComponents = dish.getComponents();
                if (!dishComponents.isEmpty()) {
                    // Convert Preparable to Ingredient for rendering
                    List<Ingredient> ingredientsToRender = new ArrayList<>();
                    for (Preparable p : dishComponents) {
                        if (p instanceof Ingredient ing) {
                            ingredientsToRender.add(ing);
                        }
                    }

                    if (!ingredientsToRender.isEmpty()) {
                        drawStackedIngredients(centerX, centerY, ingredientsToRender);
                        drawStationLabelOnly(x, y, "P+" + ingredientsToRender.size(), Color.LIGHTGREEN);
                        return;
                    }
                }
            }
        }

        // Draw stacked ingredients (berlapis dari bawah ke atas)
        List<Ingredient> ingredients = assembly.getIngredientsOnStation();
        if (!ingredients.isEmpty()) {
            drawStackedIngredients(centerX, centerY, ingredients);
        }

        // Draw label at the bottom
        String label = "ASSEM";
        Color labelColor = Color.LIGHTGREEN;

        if (assembly.hasPlate() && assembly.hasIngredient()) {
            label = "P+" + ingredients.size();
        } else if (assembly.hasPlate()) {
            Plate plate = assembly.getPlateOnStation();
            if (plate.hasDish() && !plate.getDish().getComponents().isEmpty()) {
                label = "DISH";
            } else {
                label = "PLATE";
            }
        } else if (assembly.hasIngredient()) {
            label = "ING:" + ingredients.size();
        }

        drawStationLabelOnly(x, y, label, labelColor);
    }

    private void drawCuttingStationVisual(int x, int y, CuttingStation cutting) {
        int centerX = x + TILE_SIZE / 2;
        int centerY = y + TILE_SIZE / 2;

        // Draw plate first (if exists)
        if (cutting.hasPlate()) {
            Plate plate = cutting.getPlateOnStation();

            if (imageManager.isUsingImages() && imageManager.hasImage("plate_empty")) {
                gc.drawImage(imageManager.getImage("plate_empty"), centerX - 20, centerY - 20, 40, 40);
            } else {
                gc.setFill(Color.WHITE);
                gc.fillOval(centerX - 20, centerY - 20, 40, 40);
            }

            // Check if plate has dish with ingredients
            if (plate.hasDish()) {
                Dish dish = plate.getDish();
                List<Preparable> dishComponents = dish.getComponents();

                if (!dishComponents.isEmpty()) {
                    List<Ingredient> ingredientsToRender = new ArrayList<>();
                    for (Preparable p : dishComponents) {
                        if (p instanceof Ingredient ing) {
                            ingredientsToRender.add(ing);
                        }
                    }

                    if (!ingredientsToRender.isEmpty()) {
                        drawStackedIngredients(centerX, centerY, ingredientsToRender);
                    }
                }
            }
        }

        // Draw ingredient being cut (separate, always visible)
        Ingredient beingCut = cutting.getIngredientBeingCut();
        if (beingCut != null) {
            String imageKey = getIngredientImageKey(beingCut);

            int cutX = centerX - 15;
            int cutY = centerY - 18;

            if (imageManager.isUsingImages() && imageManager.hasImage(imageKey)) {
                gc.drawImage(imageManager.getImage(imageKey), cutX, cutY, 30, 30);
            } else {
                Color ingColor = getIngredientColor(beingCut);
                gc.setFill(ingColor);
                gc.fillOval(cutX, cutY, 30, 30);
            }

            // Draw progress bar if being cut
            double progress = cutting.getCutProgressPercent();
            if (progress > 0) {
                drawProgressBar(cutX, cutY + 32, 30, progress);

            }
        }
        // Draw stacked ingredients
        List<Ingredient> ingredients = cutting.getIngredientsOnStation();
        if (!ingredients.isEmpty()) {
            drawStackedIngredients(centerX, centerY, ingredients);
        }

        // Draw label
        drawStationLabelOnly(x, y, "CUT", Color.WHITE);
    }

    private void drawStationLabelOnly(int x, int y, String label, Color labelColor) {
        // Draw label background
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(x + 2, y + TILE_SIZE - 16, TILE_SIZE - 4, 14);

        // Draw label text
        gc.setFill(labelColor);
        gc.setFont(Font.font("Inter", FontWeight.BOLD, 9));
        gc.fillText(label, x + 4, y + TILE_SIZE - 5);
    }

    // Draw ingredients yang di-stack (berlapis)
    private void drawStackedIngredients(int centerX, int centerY, List<Ingredient> ingredients) {
        int stackOffset = 0; // Offset untuk efek stacking

        for (int i = 0; i < ingredients.size(); i++) {
            Ingredient ing = ingredients.get(i);
            String imageKey = getIngredientImageKey(ing);

            // Draw dengan offset untuk efek stacking
            int drawX = centerX - 15 + stackOffset;
            int drawY = centerY - 15 - (i * 3); // Naik 3 pixel per ingredient

            if (imageManager.isUsingImages() && imageManager.hasImage(imageKey)) {
                gc.drawImage(imageManager.getImage(imageKey), drawX, drawY, 30, 30);
            } else {
                // Fallback: colored circles
                Color ingColor = getIngredientColor(ing);
                gc.setFill(ingColor);
                gc.fillOval(drawX, drawY, 30, 30);

                // Draw state indicator
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 8));
                String stateChar = ing.getState() == IngredientState.RAW ? "R" : "C";
                gc.fillText(stateChar, drawX + 10, drawY + 18);
            }

            stackOffset += 2; // Slight horizontal offset
        }
    }

    // Get ingredient image key berdasarkan nama & state
    private String getIngredientImageKey(Ingredient ing) {
        String name = ing.getName().toLowerCase();
        String state = ing.getState() == IngredientState.RAW ? "raw" : "chopped";

        String ingredientName;
        if (name.contains("dough")) ingredientName = "dough";
        else if (name.contains("tomato")) ingredientName = "tomato";
        else if (name.contains("cheese")) ingredientName = "cheese";
        else if (name.contains("sausage")) ingredientName = "sausage";
        else if (name.contains("chicken")) ingredientName = "chicken";
        else ingredientName = "unknown";

        return "ingredient_" + ingredientName + "_" + state;
    }

    // Get ingredient color for fallback
    private Color getIngredientColor(Ingredient ing) {
        String name = ing.getName().toLowerCase();

        if (name.contains("dough")) return Color.rgb(255, 228, 196);
        else if (name.contains("tomato")) return Color.rgb(255, 69, 0);
        else if (name.contains("cheese")) return Color.rgb(255, 223, 0);
        else if (name.contains("sausage")) return Color.rgb(139, 69, 19);
        else if (name.contains("chicken")) return Color.rgb(255, 160, 122);
        else return Color.ORANGE;
    }

    // Draw finished pizza (setelah baked)
    private void drawFinishedPizza(int centerX, int centerY, PizzaDish pizza) {
        String imageKey = null;

        if (pizza.isBurned()) {
            imageKey = "pizza_burned";
        } else {
            // Detect pizza type dari components
            String pizzaType = detectPizzaType(pizza);
            imageKey = pizzaType;
        }

        if (imageManager.isUsingImages() && imageKey != null && imageManager.hasImage(imageKey)) {
            // Draw pizza image (ukuran lebih besar dari plate)
            gc.drawImage(imageManager.getImage(imageKey), centerX - 22, centerY - 22, 44, 44);
        } else {
            // Fallback: draw colored circle
            Color pizzaColor = pizza.isBurned() ? Color.rgb(50, 25, 0) : Color.rgb(255, 140, 0);
            gc.setFill(pizzaColor);
            gc.fillOval(centerX - 22, centerY - 22, 44, 44);

            // Draw status text
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 8));
            gc.fillText(pizza.isBurned() ? "BURN" : "PIZZA", centerX - 15, centerY + 2);
        }
    }

    // Detect pizza type dari ingredients
    private String detectPizzaType(PizzaDish pizza) {
        List<Preparable> components = pizza.getComponents();

        boolean hasDough = false;
        boolean hasTomato = false;
        boolean hasCheese = false;
        boolean hasSausage = false;
        boolean hasChicken = false;

        for (Preparable p : components) {
            if (p instanceof Ingredient ing) {
                String name = ing.getName().toLowerCase();
                if (name.contains("dough")) hasDough = true;
                else if (name.contains("tomato")) hasTomato = true;
                else if (name.contains("cheese")) hasCheese = true;
                else if (name.contains("sausage")) hasSausage = true;
                else if (name.contains("chicken")) hasChicken = true;
            }
        }

        // Detect recipe
        if (hasDough && hasTomato && hasCheese && hasSausage) {
            return "pizza_sosis";
        } else if (hasDough && hasTomato && hasCheese && hasChicken) {
            return "pizza_ayam";
        } else if (hasDough && hasTomato && hasCheese) {
            return "pizza_margherita";
        }

        return null; // Unknown pizza
    }
    // ==================== DRAW FLOOR ITEMS ====================

    private void drawFloorItems() {
        Map<Position, Item> itemsOnFloor = gameController.getItemsOnFloor();

        for (Map.Entry<Position, Item> entry : itemsOnFloor.entrySet()) {
            Position pos = entry.getKey();
            Item item = entry.getValue();

            int x = pos.getX() * TILE_SIZE;
            int y = pos.getY() * TILE_SIZE;

            if (item instanceof Ingredient ing) {
                // Get ingredient image key
                String name = ing.getName().toLowerCase();
                String state = ing.getState() == IngredientState.RAW ? "raw" : "chopped";

                String ingredientName;
                if (name.contains("dough")) ingredientName = "dough";
                else if (name.contains("tomato")) ingredientName = "tomato";
                else if (name.contains("cheese")) ingredientName = "cheese";
                else if (name.contains("sausage")) ingredientName = "sausage";
                else if (name.contains("chicken")) ingredientName = "chicken";
                else ingredientName = "unknown";

                String imageKey = "ingredient_" + ingredientName + "_" + state;

                if (imageManager.isUsingImages() && imageManager.hasImage(imageKey)) {
                    // Draw ingredient image
                    gc.drawImage(imageManager.getImage(imageKey), x + 10, y + 10, TILE_SIZE - 20, TILE_SIZE - 20);
                } else {
                    // Fallback to colored circle
                    Color itemColor = switch (ing.getState()) {
                        case RAW -> Color.rgb(255, 140, 0);
                        case CHOPPED -> Color.rgb(255, 215, 0);
                        case COOKED -> Color.rgb(34, 139, 34);
                        case BURNED -> Color.rgb(139, 0, 0);
                        default -> Color.ORANGE;
                    };

                    gc.setFill(Color.rgb(255, 255, 255, 0.3));
                    gc.fillRect(x + 5, y + 5, TILE_SIZE - 10, TILE_SIZE - 10);

                    gc.setFill(itemColor);
                    gc.fillOval(x + 12, y + 12, TILE_SIZE - 24, TILE_SIZE - 24);
                }

                // Draw state indicator
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 8));
                String stateChar = switch (ing.getState()) {
                    case RAW -> "R";
                    case CHOPPED -> "C";
                    case COOKED -> "K";
                    case BURNED -> "B";
                    default -> "?";
                };
                gc.fillText(stateChar, x + TILE_SIZE - 12, y + 12);

                String shortName = ing.getName().substring(0, Math.min(4, ing.getName().length()));
                gc.fillText(shortName, x + 10, y + TILE_SIZE - 8);

            } else if (item instanceof Plate plate) {
                // Try to use plate image
                String plateKey = plate.isClean() ? "plate_empty" : "plate_dirty";

                if (imageManager.isUsingImages() && imageManager.hasImage(plateKey)) {
                    gc.drawImage(imageManager.getImage(plateKey), x + 10, y + 10, TILE_SIZE - 20, TILE_SIZE - 20);
                } else {
                    // Fallback
                    gc.setFill(plate.isClean() ? Color.WHITE : Color.GRAY);
                    gc.fillOval(x + 10, y + 10, TILE_SIZE - 20, TILE_SIZE - 20);
                }

                gc.setFill(Color.BLACK);
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 8));
                gc.fillText(plate.isClean() ? "C" : "D", x + TILE_SIZE - 15, y + 15);

            } else {
                gc.setFill(Color.LIGHTGRAY);
                gc.fillRect(x + 10, y + 10, TILE_SIZE - 20, TILE_SIZE - 20);
                gc.setFill(Color.BLACK);
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 8));
                String shortName = item.getName().substring(0, Math.min(4, item.getName().length()));
                gc.fillText(shortName, x + 12, y + TILE_SIZE / 2);
            }
        }
    }

    // ==================== DRAW PLAYERS ====================

    private void drawPlayers() {
        List<ChefPlayer> chefs = gameStage.getChefs();
        ChefPlayer activeChef = gameStage.getActiveChef();

        for (ChefPlayer chef : chefs) {
            // Use VISUAL position for smooth animation
            int x = (int) (chef.getVisualX() * TILE_SIZE);
            int y = (int) (chef.getVisualY() * TILE_SIZE);
            boolean isActive = chef == activeChef;
            boolean isChef1 = chef.getName().equals("Chef 1");

            // Get the appropriate image key
            String imageKey = getChefImageKey(chef, isChef1);

            boolean imageDrawn = false;

            if (imageManager.isUsingImages() && imageManager.hasImage(imageKey)) {
                // Draw chef image - smoothly scaled
                gc.drawImage(imageManager.getImage(imageKey), x, y, TILE_SIZE, TILE_SIZE);
                imageDrawn = true;
            }
            // UBAH: Fallback untuk SEMUA chef, bukan hanya Chef1
            else if (imageManager.isUsingImages()) {
                // Try base direction image (without item)
                String baseKey = getChefBaseImageKey(chef, isChef1);
                if (imageManager.hasImage(baseKey)) {
                    gc.drawImage(imageManager.getImage(baseKey), x, y, TILE_SIZE, TILE_SIZE);
                    imageDrawn = true;
                }
            }
            // Fallback terakhir: gambar bulatan
            else if (imageManager.isUsingImages() && !isChef1 && imageManager.hasImage("chef2")) {
                gc.drawImage(imageManager.getImage("chef2"), x, y, TILE_SIZE, TILE_SIZE);
                imageDrawn = true;
            }

            if (!imageDrawn) {
                drawChefFallback(chef, x, y, isActive);
            }

            // Active chef highlight
            if (isActive) {
                gc.setStroke(Color.YELLOW);
                gc.setLineWidth(2);
                gc.strokeRect(x - 1, y - 1, TILE_SIZE + 2, TILE_SIZE + 2);

                // Add dash trail effect if dashing
                if (chef.isDashing()) {
                    gc.setStroke(Color.rgb(255, 255, 0, 0.5));
                    gc.setLineWidth(3);
                    gc.strokeRect(x - 2, y - 2, TILE_SIZE + 4, TILE_SIZE + 4);
                }
            }

            // Draw chef name above
            gc.setFill(isActive ? Color.YELLOW : Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            String label = isActive ? "★ " + chef.getName() : chef.getName();

            double textWidth = label.length() * 6;
            gc.setFill(Color.rgb(0, 0, 0, 0.7));
            gc.fillRoundRect(x - 2, y - 18, textWidth + 4, 14, 3, 3);
            gc.setFill(isActive ? Color.YELLOW : Color.WHITE);
            gc.fillText(label, x, y - 6);

            drawChefInventory(chef, x, y);

            if (chef.isBusy() && !chef.isMoving()) {
                drawBusyProgressBar(chef, x, y);
            }
        }
    }

    private String getChefImageKey(ChefPlayer chef, boolean isChef1) {
        // UBAH: karena pas load image untuk chef2, tetep gak muncul di gamenya
        String chefPrefix = isChef1 ? "chef1" : "chef2";

        String direction = switch (chef.getDirection()) {
            case UP -> "back";
            case DOWN -> "front";
            case LEFT -> "left";
            case RIGHT -> "right";
        };


        // Check if chef is holding an item
        if (chef.hasItem()) {
            Item item = chef.getInventory();
            String itemSuffix = getItemImageSuffix(item);
            if (itemSuffix != null) {
                return chefPrefix + "_" + direction + "_" + itemSuffix;
            }
        }


        return chefPrefix + "_" + direction;
    }


    private String getChefBaseImageKey(ChefPlayer chef, boolean isChef1) {
        // UBAH: karena pas load image untuk chef2, tetep gak muncul di gamenya
        String chefPrefix = isChef1 ? "chef1" : "chef2";

        String direction = switch (chef.getDirection()) {
            case UP -> "back";
            case DOWN -> "front";
            case LEFT -> "left";
            case RIGHT -> "right";
        };


        return chefPrefix + "_" + direction;
    }


    private String getItemImageSuffix(Item item) {
        if (item instanceof Plate) {
            return "plate";
        } else if (item instanceof Ingredient ing) {
            String name = ing.getName().toLowerCase();

            if (name.contains("chicken") && ing.getState() == IngredientState.COOKED) {
                return "cooked_chicken";
            }

            if (name.contains("cheese") && ing.getState() == IngredientState.RAW) return "cheese_raw";
            if (name.contains("cheese") && ing.getState() == IngredientState.CHOPPED) return "cheese_chopped";
            if (name.contains("chicken") && ing.getState() == IngredientState.RAW) return "chicken_raw";
            if (name.contains("dough") && ing.getState() == IngredientState.RAW) return "dough_raw";
            if (name.contains("sausage") && ing.getState() == IngredientState.RAW) return "sausage_raw";
            if (name.contains("tomato") && ing.getState() == IngredientState.RAW) return "tomato_raw";

        }
        return null;
    }

    private void drawChefFallback(ChefPlayer chef, int x, int y, boolean isActive) {
        // Draw shadow
        gc.setFill(Color.rgb(0, 0, 0, 0.3));
        gc.fillOval(x + 6, y + TILE_SIZE - 12, TILE_SIZE - 12, 8);

        // Draw body
        gc.setFill(isActive ? Color.rgb(50, 50, 50) : Color.rgb(80, 80, 80));
        int padding = 6;
        gc.fillOval(x + padding, y + padding, TILE_SIZE - 2 * padding, TILE_SIZE - 2 * padding);

        // Draw outline
        gc.setStroke(isActive ? Color.rgb(255, 200, 0) : Color.rgb(150, 150, 150));
        gc.setLineWidth(2);
        gc.strokeOval(x + padding, y + padding, TILE_SIZE - 2 * padding, TILE_SIZE - 2 * padding);

        // Draw direction indicator
        gc.setFill(Color.WHITE);
        drawDirectionIndicator(chef, x, y);
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

    private void drawChefInventory(ChefPlayer chef, int x, int y) {
        if (!chef.hasItem()) return;

        Item item = chef.getInventory();

        if (item instanceof Plate plate) {
            // Draw plate icon
            int iconX = x + TILE_SIZE - 18;
            int iconY = y - 18;

            if (plate.hasDish()) {
                Dish dish = plate.getDish();

                // If pizza is baked, show pizza image
                if (dish instanceof PizzaDish pizza && pizza.isBaked()) {
                    drawFinishedPizza(iconX + 8, iconY + 8, pizza);
                } else {
                    // Show plate + stacked ingredients
                    if (imageManager.isUsingImages() && imageManager.hasImage("plate_empty")) {
                        gc.drawImage(imageManager.getImage("plate_empty"), iconX, iconY, 16, 16);
                    } else {
                        gc.setFill(Color.WHITE);
                        gc.fillOval(iconX, iconY, 16, 16);
                    }

                    // Draw mini stacked ingredients
                    List<Preparable> components = dish.getComponents();
                    if (!components.isEmpty()) {
                        gc.setFill(Color.rgb(255, 140, 0, 0.8));
                        gc.fillOval(iconX + 4, iconY + 4, 8, 8);

                        // Show count
                        gc.setFill(Color.WHITE);
                        gc.setFont(Font.font("Arial", FontWeight.BOLD, 8));
                        gc.fillText(String.valueOf(components.size()), iconX + 6, iconY + 11);
                    }
                }
            } else {
                // Empty plate
                if (imageManager.isUsingImages() && imageManager.hasImage("plate_empty")) {
                    gc.drawImage(imageManager.getImage("plate_empty"), iconX, iconY, 16, 16);
                } else {
                    gc.setFill(plate.isClean() ? Color.WHITE : Color.GRAY);
                    gc.fillOval(iconX, iconY, 16, 16);
                }
            }

            // Draw text label below
            String displayText = plate.hasDish() ? "Plate+Food" : (plate.isClean() ? "Plate(C)" : "Plate(D)");
            int boxWidth = Math.max(TILE_SIZE, displayText.length() * 6 + 10);

            gc.setFill(Color.rgb(0, 0, 0, 0.8));
            gc.fillRect(x - 2, y + TILE_SIZE + 2, boxWidth, 16);

            gc.setFill(Color.YELLOW);
            gc.setFont(Font.font("Inter", FontWeight.BOLD, 9));
            gc.fillText(displayText, x + 2, y + TILE_SIZE + 13);

        } else if (item instanceof Ingredient ing) {
            // Draw ingredient icon
            int iconX = x + TILE_SIZE - 18;
            int iconY = y - 18;

            String imageKey = getIngredientImageKey(ing);
            if (imageManager.isUsingImages() && imageManager.hasImage(imageKey)) {
                gc.drawImage(imageManager.getImage(imageKey), iconX, iconY, 16, 16);
            } else {
                Color ingColor = getIngredientColor(ing);
                gc.setFill(ingColor);
                gc.fillOval(iconX, iconY, 16, 16);
            }

            // Draw text label
            String stateChar = switch (ing.getState()) {
                case RAW -> "R";
                case CHOPPED -> "C";
                case COOKED -> "K";
                case BURNED -> "B";
                default -> "?";
            };
            String displayText = ing.getName().substring(0, Math.min(4, ing.getName().length())) + "(" + stateChar + ")";

            int boxWidth = Math.max(TILE_SIZE, displayText.length() * 6 + 10);

            gc.setFill(Color.rgb(0, 0, 0, 0.8));
            gc.fillRect(x - 2, y + TILE_SIZE + 2, boxWidth, 16);

            gc.setFill(Color.YELLOW);
            gc.setFont(Font.font("Inter", FontWeight.BOLD, 9));
            gc.fillText(displayText, x + 2, y + TILE_SIZE + 13);
        } else {
            // TAMBAHKAN: Draw icon untuk item lain (tomato, dll)
            int iconX = x + TILE_SIZE - 18;
            int iconY = y - 18;

            // Draw generic item icon
            gc.setFill(Color.ORANGE);
            gc.fillRect(iconX, iconY, 16, 16);

            // Draw first letter of item name
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            String firstLetter = item.getName().substring(0, 1).toUpperCase();
            gc.fillText(firstLetter, iconX + 4, iconY + 12);

            // Draw text label below (kode yang sudah ada)
            String displayText = item.getName().substring(0, Math.min(6, item.getName().length()));
            int boxWidth = Math.max(TILE_SIZE, displayText.length() * 6 + 10);

            gc.setFill(Color.rgb(0, 0, 0, 0.8));
            gc.fillRect(x - 2, y + TILE_SIZE + 2, boxWidth, 16);

            gc.setFill(Color.YELLOW);
            gc.setFont(Font.font("Inter", FontWeight.BOLD, 9));
            gc.fillText(displayText, x + 2, y + TILE_SIZE + 13);
        }
    }

    private void drawBusyProgressBar(ChefPlayer chef, int x, int y) {
        double progress = chef.getBusyProgress();
        CurrentAction action = chef.getCurrentAction();
        int timeRemaining = chef.getBusyTimeRemaining();

        int barWidth = TILE_SIZE - 4;
        int barHeight = 8;
        int barX = x + 2;
        int barY = y - 20;

        gc.setFill(Color.rgb(40, 40, 40, 0.9));
        gc.fillRoundRect(barX - 2, barY - 2, barWidth + 4, barHeight + 14, 4, 4);

        gc.setFill(Color.rgb(60, 60, 60));
        gc.fillRoundRect(barX, barY, barWidth, barHeight, 3, 3);

        Color progressColor = switch (action) {
            case CUTTING -> Color.rgb(255, 165, 0);
            case COOKING -> Color.rgb(255, 69, 0);
            case WASHING -> Color.rgb(30, 144, 255);
            default -> Color.rgb(46, 204, 113);
        };

        gc.setFill(progressColor);
        gc.fillRoundRect(barX, barY, barWidth * progress, barHeight, 3, 3);

        String actionLabel = switch (action) {
            case CUTTING -> "CUT";
            case COOKING -> "COOK";
            case WASHING -> "WASH";
            default -> "BUSY";
        };

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Inter", FontWeight.BOLD, 8));
        gc.fillText(actionLabel + " " + timeRemaining + "s", barX, barY + barHeight + 10);
    }

    // ==================== PAUSE & RESULT ====================

    private void showPauseMenu(Stage primaryStage) {
        gameController.togglePause();
        if (!gameController.isPaused()) return;

        Stage pauseStage = new Stage();

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #2D2D2D;");

        Label title = new Label("PAUSED");
        title.setFont(Font.font("Inter", FontWeight.BOLD, 32));
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
        btn.setFont(Font.font("Inter", FontWeight.BOLD, 14));
        btn.setStyle("-fx-background-color: #4682B4; -fx-text-fill: white; -fx-background-radius: 5;");
        return btn;
    }

    private void showResultScreen(Stage primaryStage) {
        gameLoop.stop();
        ResultView resultView = new ResultView(gameController);
        resultView.show(primaryStage);
    }
}