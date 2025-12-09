package models.command;

import models.player.ChefPlayer;

import models.player.ChefPlayer;
import models.core.Direction;
import models.core.Position;
import models.map.GameMap;
import models.item.Item;
import models.item.Ingredient;

import java.util.List;
import java.util.Map;


/**
 * Command Pattern - Concrete Command for Throw Action
 * Bonus Feature: Chef throws item to distant position
 *
 * @author Nimonscooked Team
 * @version 1.0
 */
public class ThrowCommand implements ChefCommand {
    private final ChefPlayer chef;
    private final GameMap map;
    private final List<ChefPlayer> allChefs;
    private final Map<Position, Item> itemsOnFloor;

    private static final int THROW_DISTANCE = 3; // Maximum throw distance

    // State for undo
    private Item thrownItem;
    private Position landPosition;
    private boolean wasCaught;
    private ChefPlayer catchingChef;
    private boolean wasExecuted;

    /**
     * Constructor
     *
     * @param chef         The chef throwing the item
     * @param map          Game map for trajectory calculation
     * @param allChefs     List of all chefs (for catching)
     * @param itemsOnFloor Map of items on floor
     */
    public ThrowCommand(ChefPlayer chef, GameMap map, List<ChefPlayer> allChefs,
                        Map<Position, Item> itemsOnFloor) {
        this.chef = chef;
        this.map = map;
        this.allChefs = allChefs;
        this.itemsOnFloor = itemsOnFloor;
        this.wasExecuted = false;
    }

    @Override
    public boolean canExecute() {
        // Chef must not be busy
        if (chef.isBusy()) {
            return false;
        }

        // Chef must be holding an item
        if (!chef.hasItem()) {
            return false;
        }

        // Only uncooked ingredients can be thrown (safety rule)
        Item heldItem = chef.getInventory();
        if (!(heldItem instanceof Ingredient)) {
            return false;
        }

        return true;
    }

    @Override
    public boolean execute() {
        if (!canExecute()) {
            System.out.println("[CMD] Cannot throw - not holding throwable item!");
            return false;
        }

        Position chefPos = chef.getPosition();
        Direction dir = chef.getDirection();

        // Calculate throw trajectory
        int landX = chefPos.getX();
        int landY = chefPos.getY();

        for (int i = 1; i <= THROW_DISTANCE; i++) {
            int checkX = chefPos.getX();
            int checkY = chefPos.getY();

            // Calculate position at distance i
            switch (dir) {
                case UP -> checkY -= i;
                case DOWN -> checkY += i;
                case LEFT -> checkX -= i;
                case RIGHT -> checkX += i;
            }

            // Check bounds
            if (!map.inBounds(checkX, checkY)) {
                break; // Out of bounds, item lands before boundary
            }

            // Check if hit wall
            if (map.getTile(checkX, checkY) == 'X') {
                break; // Hit wall, item lands before wall
            }

            // Check for catching chef
            ChefPlayer potentialCatcher = null;
            for (ChefPlayer other : allChefs) {
                if (other != chef) {
                    Position otherPos = other.getPosition();
                    if (otherPos.getX() == checkX && otherPos.getY() == checkY) {
                        potentialCatcher = other;
                        break;
                    }
                }
            }

            // If chef can catch, throw is caught
            if (potentialCatcher != null && !potentialCatcher.hasItem()) {
                thrownItem = chef.drop();
                potentialCatcher.pickUp(thrownItem);

                wasCaught = true;
                catchingChef = potentialCatcher;
                wasExecuted = true;

                System.out.println("[CMD] " + chef.getName() + " threw " + thrownItem.getName() +
                        " - caught by " + potentialCatcher.getName() + "!");
                return true;
            }

            // Check if hit station
            boolean isStation = map.getStationAt(checkX, checkY) != null;

            // If position is walkable (not station), update landing position
            if (map.isWalkable(checkX, checkY)) {
                landX = checkX;
                landY = checkY;
            } else if (!isStation) {
                // Hit non-walkable, non-station tile
                break;
            }
        }

        // Item lands on floor
        thrownItem = chef.drop();
        landPosition = new Position(landX, landY);

        // Check if position already has item (shouldn't happen, but safe check)
        if (itemsOnFloor.containsKey(landPosition)) {
            // Can't throw here, return item to chef
            chef.pickUp(thrownItem);
            System.out.println("[CMD] Cannot throw - landing position occupied");
            return false;
        }

        itemsOnFloor.put(landPosition, thrownItem);

        wasCaught = false;
        wasExecuted = true;

        System.out.println("[CMD] " + chef.getName() + " threw " + thrownItem.getName() +
                " to (" + landX + ", " + landY + ")");
        return true;
    }

    @Override
    public void undo() {
        if (!wasExecuted) {
            return;
        }

        if (wasCaught && catchingChef != null && thrownItem != null) {
            // Undo caught throw
            catchingChef.drop();
            chef.pickUp(thrownItem);
            System.out.println("[CMD] Undone throw (caught)");
        } else if (!wasCaught && landPosition != null && thrownItem != null) {
            // Undo floor throw
            itemsOnFloor.remove(landPosition);
            chef.pickUp(thrownItem);
            System.out.println("[CMD] Undone throw (floor)");
        }

        wasExecuted = false;
    }

    @Override
    public String getDescription() {
        if (wasCaught) {
            return String.format("%s threw item to %s", chef.getName(),
                    catchingChef != null ? catchingChef.getName() : "another chef");
        } else {
            return String.format("%s threw item to floor", chef.getName());
        }
    }

    /**
     * Get throw distance constant
     */
    public static int getThrowDistance() {
        return THROW_DISTANCE;
    }

    /**
     * Check if throw was caught
     */
    public boolean wasCaught() {
        return wasCaught;
    }

    /**
     * Get the chef that caught the item (if caught)
     */
    public ChefPlayer getCatchingChef() {
        return catchingChef;
    }
}

