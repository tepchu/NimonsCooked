package controllers;

import models.command.CommandInvoker;
import models.level.Level;
import models.level.LevelManager;
import models.map.*;
import models.player.ChefPlayer;
import models.core.Position;
import models.core.Direction;
import javafx.scene.input.KeyCode;
import models.command.*;

import models.item.Item;
import models.item.Ingredient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GameController {
    private Stage stage;
    private LevelManager levelManager;
    private boolean isPaused;
    private Map<Position, Item> itemsOnFloor;
    private CommandInvoker commandInvoker;


    public GameController() {
        this.levelManager = LevelManager.getInstance();
        this.isPaused = false;
        this.itemsOnFloor = new HashMap<>();
        this.commandInvoker = new CommandInvoker(100);
    }

    public void startLevel(Level level) {
        levelManager.setCurrentLevel(level);

        GameMap map = MapLoader.loadPizzaMap();

        stage = new Stage("stage_" + level.getId(), MapType.PIZZA, map);
        stage.applyLevelSettings(level);
        stage.initStage();
        stage.startGame();
    }

    public void startGame() {
        if (stage != null) {
            stage.startGame();
        }
    }

    public void handleInput(KeyCode key) {
        if (stage == null || !stage.isGameRunning()) return;
        if (isPaused && key != KeyCode.ESCAPE) return;

        if (key == KeyCode.B) {
            stage.switchActiveChef();
            System.out.println("[INPUT] Switched to:  " + stage.getActiveChef().getName());
            return;
        }

        if (key == KeyCode.ESCAPE) {
            togglePause();
            return;
        }

        ChefPlayer chef = stage.getActiveChef();
        if (chef == null) return;

        GameMap map = stage.getGameMap();
        List<ChefPlayer> chefs = stage.getChefs();

        ChefCommand command = null;

        switch (key) {
            case W -> command = new MoveCommand(chef, Direction.UP, map, chefs);
            case S -> command = new MoveCommand(chef, Direction.DOWN, map, chefs);
            case A -> command = new MoveCommand(chef, Direction.LEFT, map, chefs);
            case D -> command = new MoveCommand(chef, Direction.RIGHT, map, chefs);
            case C -> {
                if (!chef.isBusy() || !chef.isMoving()) {  // Keep busy check for non-movement
                    command = new PickupDropCommand(chef, map, itemsOnFloor);
                }
            }
            case X -> {
                if (!chef.isBusy() || !chef.isMoving()) {  // Keep busy check for non-movement
                    command = new InteractCommand(chef, map, itemsOnFloor);
                }
            }
            case SPACE -> {
                if (!chef.isBusy() || !chef.isMoving()) {  // Keep busy check for non-movement
                    command = new ThrowCommand(chef, map, chefs, itemsOnFloor);
                }
            }
            case Z -> {
                commandInvoker.undo();
                return;
            }
            case Y -> {
                commandInvoker.redo();
                return;
            }
        }

        if (command != null) {
            commandInvoker.executeCommand(command);
        }
    }

    private void handleFloorInteraction(ChefPlayer chef, int x, int y) {
        Position targetPos = new Position(x, y);

        if (!chef.hasItem() && itemsOnFloor.containsKey(targetPos)) {
            Item item = itemsOnFloor.remove(targetPos);
            chef.pickUp(item);
            System.out.println("Picked up " + item.getName() + " from floor");
        } else if (chef.hasItem() && !itemsOnFloor.containsKey(targetPos)) {
            GameMap map = stage.getGameMap();
            if (map.isWalkable(x, y)) {
                Item dropped = chef.drop();
                itemsOnFloor.put(targetPos, dropped);
                System.out.println("Dropped " + dropped.getName() + " on floor at (" + x + ", " + y + ")");
            }
        }
    }

    /**
     * Handle dash input (Shift + WASD)
     * This is called directly from GameView when shift is pressed
     */
    public void handleDashInput(KeyCode key, boolean shiftPressed) {
        if (!shiftPressed || stage == null || !stage.isGameRunning()) return;
        if (isPaused) return;

        ChefPlayer activeChef = stage.getActiveChef();
        if (activeChef == null || activeChef.isBusy()) return;
        if (!activeChef.canDash()) {
            System.out.println("[DASH] Dash on cooldown!");
            return;
        }

        GameMap map = stage.getGameMap();
        List<ChefPlayer> chefs = stage.getChefs();
        Direction dashDir = null;

        switch (key) {
            case W -> dashDir = Direction.UP;
            case A -> dashDir = Direction.LEFT;
            case S -> dashDir = Direction.DOWN;
            case D -> dashDir = Direction.RIGHT;
        }

        if (dashDir != null) {
            // Create and execute dash command
            DashCommand dashCommand = new DashCommand(activeChef, dashDir, map, chefs);
            commandInvoker.executeCommand(dashCommand);
        }
    }

    private void handleThrow(ChefPlayer chef, GameMap map) {
        if (!chef.hasItem()) {
            System.out.println("Nothing to throw!");
            return;
        }

        Item item = chef.getInventory();

        if (!(item instanceof Ingredient)) {
            System.out.println("Can only throw ingredients!");
            return;
        }

        Position chefPos = chef.getPosition();
        Direction dir = chef.getDirection();

        int throwDistance = 3;
        int landX = chefPos.getX();
        int landY = chefPos.getY();

        for (int i = 1; i <= throwDistance; i++) {
            int checkX = chefPos.getX();
            int checkY = chefPos.getY();

            switch (dir) {
                case UP -> checkY -= i;
                case DOWN -> checkY += i;
                case LEFT -> checkX -= i;
                case RIGHT -> checkX += i;
            }

            if (!map.inBounds(checkX, checkY) || map.getTile(checkX, checkY) == 'X') {
                break;
            }

            boolean isStation = map.getStationAt(checkX, checkY) != null;

            ChefPlayer catchingChef = null;
            for (ChefPlayer other : stage.getChefs()) {
                if (other != chef) {
                    Position otherPos = other.getPosition();
                    if (otherPos.getX() == checkX && otherPos.getY() == checkY) {
                        catchingChef = other;
                        break;
                    }
                }
            }

            if (catchingChef != null) {
                if (!catchingChef.hasItem()) {
                    catchingChef.pickUp(chef.drop());
                    System.out.println("Caught by " + catchingChef.getName());
                    return;
                } else {
                    if (!map.isWalkable(checkX, checkY)) {
                        break;
                    }
                }
            }

            if (map.isWalkable(checkX, checkY)) {
                landX = checkX;
                landY = checkY;
            } else if (!isStation) {
                break;
            }
        }

        Item thrownItem = chef.drop();
        Position landPos = new Position(landX, landY);
        itemsOnFloor.put(landPos, thrownItem);
        System.out.println("Threw " + thrownItem.getName() + " to (" + landX + ", " + landY + ")");
    }

    public void togglePause() {
        isPaused = !isPaused;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public Stage getStage() {
        return stage;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public Map<Position, Item> getItemsOnFloor() {
        return itemsOnFloor;
    }
}
