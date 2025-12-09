package models.command;

import models.player.ChefPlayer;
import models.core.Direction;
import models.core.Position;
import models.map.GameMap;
import models.item.Item;
import models.item.Ingredient;

import java.util.List;
import java.util.Map;

/*
 * @version 1.0
 */

public class DashCommand implements ChefCommand {
    private final ChefPlayer chef;
    private final Direction direction;
    private final GameMap map;
    private final List<ChefPlayer> allChefs;

    private static final int DASH_DISTANCE = 3; // Maximum dash distance
    private static final int DASH_COOLDOWN_MS = 3000; // 3 seconds cooldown

    // State for undo
    private Position previousPosition;
    private Direction previousDirection;
    private boolean wasExecuted;

    /**
     * Constructor
     *
     * @param chef      The chef performing the dash
     * @param direction Direction to dash
     * @param map       Game map for collision detection
     * @param allChefs  List of all chefs for collision detection
     */
    public DashCommand(ChefPlayer chef, Direction direction, GameMap map, List<ChefPlayer> allChefs) {
        this.chef = chef;
        this.direction = direction;
        this.map = map;
        this.allChefs = allChefs;
        this.wasExecuted = false;
    }

    @Override
    public boolean canExecute() {
        if (chef.isBusy()) {
            return false;
        }

        if (!chef.canDash()) {
            return false;
        }

        return true;
    }

    @Override
    public boolean execute() {
        if (!canExecute()) {
            System.out.println("[CMD] Dash on cooldown or chef busy!");
            return false;
        }

        Position currentPos = chef.getPosition();

        previousPosition = new Position(currentPos.getX(), currentPos.getY());
        previousDirection = chef.getDirection();

        // Calculate maximum dash distance considering obstacles
        int finalX = currentPos.getX();
        int finalY = currentPos.getY();
        int actualDistance = 0;

        for (int i = 1; i <= DASH_DISTANCE; i++) {
            int checkX = currentPos.getX();
            int checkY = currentPos.getY();

            switch (direction) {
                case UP -> checkY -= i;
                case DOWN -> checkY += i;
                case LEFT -> checkX -= i;
                case RIGHT -> checkX += i;
            }

            if (!map.inBounds(checkX, checkY)) {
                break;
            }

            if (!map.isWalkable(checkX, checkY)) {
                break;
            }

            boolean blocked = false;
            for (ChefPlayer other : allChefs) {
                if (other != chef) {
                    Position otherPos = other.getPosition();
                    if (otherPos.getX() == checkX && otherPos.getY() == checkY) {
                        blocked = true;
                        break;
                    }
                }
            }

            if (blocked) {
                break;
            }

            // Position is valid, update final position
            finalX = checkX;
            finalY = checkY;
            actualDistance = i;
        }

        if (actualDistance > 0) {
            currentPos.setX(finalX);
            currentPos.setY(finalY);
            chef.setDirection(direction);
            chef.recordDash(); // Start cooldown

            wasExecuted = true;

            System.out.println("[CMD] " + chef.getName() + " dashed " + actualDistance +
                    " tiles to (" + finalX + ", " + finalY + ")");
            return true;
        }

        System.out.println("[CMD] Cannot dash - blocked immediately");
        return false;
    }

    @Override
    public void undo() {
        if (!wasExecuted || previousPosition == null) {
            return;
        }

        Position currentPos = chef.getPosition();
        currentPos.setX(previousPosition.getX());
        currentPos.setY(previousPosition.getY());
        chef.setDirection(previousDirection);

        wasExecuted = false;
        System.out.println("[CMD] Undone dash (cooldown not restored)");
    }

    @Override
    public String getDescription() {
        return String.format("%s dashes %s", chef.getName(), direction.toString());
    }

    /**
     * Get dash distance constant
     */
    public static int getDashDistance() {
        return DASH_DISTANCE;
    }

    /**
     * Get dash cooldown in milliseconds
     */
    public static int getDashCooldown() {
        return DASH_COOLDOWN_MS;
    }
}