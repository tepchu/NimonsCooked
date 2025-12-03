package controllers;

import models.map.*;
import models.player.ChefPlayer;
import models.core.Position;
import models.core.Direction;
import models.station.Station;
import javafx.scene.input.KeyCode;

public class GameController {
    private Stage stage;

    public GameController() {
        GameMap map = MapLoader.loadPizzaMap();

        stage = new Stage("type_d_pizza", MapType.PIZZA, map);
        stage.initStage();
    }

    public void startGame() {
        stage.startGame();
    }

    public void handleInput(KeyCode key) {
        ChefPlayer activeChef = stage.getActiveChef();
        if (activeChef == null || activeChef.isBusy()) return;

        GameMap map = stage.getGameMap();
        Position currentPos = activeChef.getPosition();

        switch (key) {
            case W -> attemptMove(activeChef, Direction.UP, map);
            case A -> attemptMove(activeChef, Direction.LEFT, map);
            case S -> attemptMove(activeChef, Direction.DOWN, map);
            case D -> attemptMove(activeChef, Direction.RIGHT, map);
            case C, V -> handleInteract(activeChef, map);
            case B -> stage.switchActiveChef();
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
        }
    }

    public Stage getStage() {
        return stage;
    }
}
