package models.command;

import models.player.ChefPlayer;
import models.core.Direction;
import models.core.Position;
import models.map.GameMap;
import models.player.CurrentAction;

import java.util.List;

/*
 * @version 1.0
 */

public class MoveCommand implements ChefCommand {
    private final ChefPlayer chef;
    private final Direction direction;
    private final GameMap map;
    private final List<ChefPlayer> allChefs;

    private Position previousPosition;
    private Direction previousDirection;
    private boolean wasExecuted;
    private boolean positionChanged; // Track if position actually changed


    public MoveCommand(ChefPlayer chef, Direction direction, GameMap map, List<ChefPlayer> allChefs) {
        this.chef = chef;
        this.direction = direction;
        this.map = map;
        this.allChefs = allChefs;
        this.wasExecuted = false;
        this.positionChanged = false;
    }

    @Override
    public boolean canExecute() {
        return !chef.isBusy();
    }

    /**
     * Check if the target position is actually walkable
     */
    private boolean canMoveTo(int newX, int newY) {
        if (!map.inBounds(newX, newY)) {
            return false;
        }

        if (!map.isWalkable(newX, newY)) {
            return false;
        }

        for (ChefPlayer other : allChefs) {
            if (other != chef) {
                Position otherPos = other.getPosition();
                if (otherPos.getX() == newX && otherPos.getY() == newY) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean execute() {
        if (!canExecute()) {
            return false;
        }

        Position currentPos = chef.getPosition();
        previousPosition = new Position(currentPos.getX(), currentPos.getY());
        previousDirection = chef.getDirection();

        chef.setDirection(direction);

        int newX = currentPos.getX();
        int newY = currentPos.getY();

        switch (direction) {
            case UP -> newY--;
            case DOWN -> newY++;
            case LEFT -> newX--;
            case RIGHT -> newX++;
        }

        if (canMoveTo(newX, newY)) {
            // NEW: If chef is busy (cutting/washing), interrupt and save progress
            if (chef.isBusy()) {
                CurrentAction action = chef.getCurrentAction();
                if (action == CurrentAction.CUTTING || action == CurrentAction.WASHING) {
                    System.out.println("[MOVE] Chef walking away from " + action + " - saving progress");
                    chef.interruptBusy();
                }
            }
            // Change to smooth movement
            chef.startMove(newX, newY, false);
            positionChanged = true;
        } else {
            positionChanged = false;
        }

        wasExecuted = true;
        return true;
    }

    @Override
    public void undo() {
        if (!wasExecuted || previousPosition == null) {
            return;
        }

        // Instant teleport for undo
        chef.teleportTo(previousPosition.getX(), previousPosition.getY());
        chef.setDirection(previousDirection);

        wasExecuted = false;
        positionChanged = false;
    }

    @Override
    public String getDescription() {
        if (positionChanged) {
            return String.format("%s moves %s to (%d, %d)",
                    chef.getName(),
                    direction.toString(),
                    chef.getPosition().getX(),
                    chef.getPosition().getY());
        } else {
            return String.format("%s faces %s (blocked)",
                    chef.getName(),
                    direction.toString());
        }
    }

    /**
     * Get the chef performing this command
     */
    public ChefPlayer getChef() {
        return chef;
    }

    /**
     * Get the direction of movement
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Check if command was executed
     */
    public boolean wasExecuted() {
        return wasExecuted;
    }

    /**
     * Check if position actually changed (not just direction)
     */
    public boolean didPositionChange() {
        return positionChanged;
    }
}