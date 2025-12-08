package controllers;

import models.level.Level;
import models.level.LevelManager;
import models.map.*;
import models.player.ChefPlayer;
import models.core.Position;
import models.core.Direction;
import models.station.Station;
import javafx.scene.input.KeyCode;

import models.item.Item;
import models.item.Ingredient;
import models.enums.IngredientState;

import java.util.HashMap;
import java.util.Map;


public class GameController {
    private Stage stage;
    private LevelManager levelManager;
    private boolean isPaused;
    private Map<Position, Item> itemsOnFloor;

    public GameController() {
        this.levelManager = LevelManager.getInstance();
        this.isPaused = false;
        this.itemsOnFloor = new HashMap<>();
    }

    public void startLevel(Level level) {
        levelManager.setCurrentLevel(level);

        GameMap map = MapLoader.loadPizzaMap();

        stage = new Stage("stage_" + level.getId(), MapType.PIZZA, map);
        stage.applyLevelSettings(level);
        stage.initStage();
        stage.startGame();
    }

    public void startDefaultGame() {
        GameMap map = MapLoader.loadPizzaMap();
        stage = new Stage("type_d_pizza", MapType.PIZZA, map);
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
        if (isPaused) return;

        ChefPlayer activeChef = stage.getActiveChef();
        if (activeChef == null || activeChef.isBusy()) return;

        GameMap map = stage.getGameMap();

        switch (key) {
            case W -> attemptMove(activeChef, Direction.UP, map);
            case A -> attemptMove(activeChef, Direction.LEFT, map);
            case S -> attemptMove(activeChef, Direction.DOWN, map);
            case D -> attemptMove(activeChef, Direction.RIGHT, map);
            case SHIFT -> {
            }
            case C, V -> handleInteract(activeChef, map);
            case SPACE -> handleThrow(activeChef, map);
            case B -> stage.switchActiveChef();
            case ESCAPE -> togglePause();
        }
    }

    private void attemptMove(ChefPlayer chef, Direction dir, GameMap map) {
        Position currentPos = chef.getPosition();
        int newX = currentPos.getX();
        int newY = currentPos.getY();

        switch (dir) {
            case UP -> newY--;
            case DOWN -> newY++;
            case LEFT -> newX--;
            case RIGHT -> newX++;
        }

        chef.setDirection(dir);

        if (map.isWalkable(newX, newY)) {
            boolean blocked = false;
            for (ChefPlayer other : stage.getChefs()) {
                if (other != chef) {
                    Position otherPos = other.getPosition();
                    if (otherPos.getX() == newX && otherPos.getY() == newY) {
                        blocked = true;
                        break;
                    }
                }
            }

            if (!blocked) {
                chef.move(dir);
            }
        }
    }

    private void handleInteract(ChefPlayer chef, GameMap map) {
        Position chefPos = chef.getPosition();
        Direction dir = chef.getDirection();

        int frontX = chefPos.getX();
        int frontY = chefPos.getY();

        switch (dir) {
            case UP -> frontY--;
            case DOWN -> frontY++;
            case LEFT -> frontX--;
            case RIGHT -> frontX++;
        }

        Station station = map.getStationAt(frontX, frontY);

        if (station != null) {
            station.interact(chef);
        } else {
            handleFloorInteraction(chef, frontX, frontY);
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

    public void handleDashInput(KeyCode key, boolean shiftPressed) {
        if (!shiftPressed || stage == null || !stage.isGameRunning()) return;
        if (isPaused) return;

        ChefPlayer activeChef = stage.getActiveChef();
        if (activeChef == null || activeChef.isBusy()) return;
        if (!activeChef.canDash()) return;

        GameMap map = stage.getGameMap();
        Direction dashDir = null;

        switch (key) {
            case W -> dashDir = Direction.UP;
            case A -> dashDir = Direction.LEFT;
            case S -> dashDir = Direction.DOWN;
            case D -> dashDir = Direction.RIGHT;
        }

        if (dashDir != null) {
            attemptDash(activeChef, dashDir, map);
        }
    }

    private void attemptDash(ChefPlayer chef, Direction dir, GameMap map) {
        if (!chef.canDash()) {
            System.out.println("Dash on cooldown!");
            return;
        }

        Position currentPos = chef.getPosition();
        int targetX = currentPos.getX();
        int targetY = currentPos.getY();

        int distance = 3;
        switch (dir) {
            case UP -> targetY -= distance;
            case DOWN -> targetY += distance;
            case LEFT -> targetX -= distance;
            case RIGHT -> targetX += distance;
        }

        int finalX = currentPos.getX();
        int finalY = currentPos.getY();

        for (int i = 1; i <= distance; i++) {
            int checkX = currentPos.getX();
            int checkY = currentPos.getY();

            switch (dir) {
                case UP -> checkY -= i;
                case DOWN -> checkY += i;
                case LEFT -> checkX -= i;
                case RIGHT -> checkX += i;
            }

            if (!map.isWalkable(checkX, checkY)) {
                break;
            }

            boolean blocked = false;
            for (ChefPlayer other : stage.getChefs()) {
                if (other != chef) {
                    Position otherPos = other.getPosition();
                    if (otherPos.getX() == checkX && otherPos.getY() == checkY) {
                        blocked = true;
                        break;
                    }
                }
            }

            if (blocked) break;

            finalX = checkX;
            finalY = checkY;
        }

        if (finalX != currentPos.getX() || finalY != currentPos.getY()) {
            currentPos.setX(finalX);
            currentPos.setY(finalY);
            chef.setDirection(dir);
            chef.recordDash();
            System.out.println("Dashed to (" + finalX + ", " + finalY + ")");
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

        Ingredient ingredient = (Ingredient) item;
        if (ingredient.getState() != IngredientState.RAW || ingredient.getState() != IngredientState.CHOPPED) {
            System.out.println("Can only throw raw and chopped ingredients!");
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

            if (!map.isWalkable(checkX, checkY)) {
                break;
            }

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
                    break;
                }
            }

            landX = checkX;
            landY = checkY;
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
