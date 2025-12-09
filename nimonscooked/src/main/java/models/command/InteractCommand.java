package models.command;

import models.player.ChefPlayer;
import models.core.Direction;
import models.core.Position;
import models.map.GameMap;
import models.station.Station;
import models.item.Item;
import models.item.kitchenutensils.Plate;
import models.item.Ingredient;

import java.util.Map;

/*
 * @version 1.0
 */
public class InteractCommand implements ChefCommand {
    private final ChefPlayer chef;
    private final GameMap map;
    private final Map<Position, Item> itemsOnFloor;

    private InteractionType lastInteractionType;
    private Item lastItem;
    private Position lastItemPosition;

    private enum InteractionType {
        NONE, STATION, PICKUP, DROP
    }

    /**
     * Constructor
     *
     * @param chef         The chef performing the interaction
     * @param map          Game map for station lookup
     * @param itemsOnFloor Map of items on the floor
     */
    public InteractCommand(ChefPlayer chef, GameMap map, Map<Position, Item> itemsOnFloor) {
        this.chef = chef;
        this.map = map;
        this.itemsOnFloor = itemsOnFloor;
        this.lastInteractionType = InteractionType.NONE;
    }

    @Override
    public boolean canExecute() {
        if (chef.isBusy()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean execute() {
        if (!canExecute()) {
            return false;
        }

        Position chefPos = chef.getPosition();
        Direction dir = chef.getDirection();

        // Priority 1: Check for item at current position
        if (!chef.hasItem() && itemsOnFloor.containsKey(chefPos)) {
            return executePickupAtCurrentPosition(chefPos);
        }

        // Calculate position in front of chef
        int frontX = chefPos.getX();
        int frontY = chefPos.getY();

        switch (dir) {
            case UP -> frontY--;
            case DOWN -> frontY++;
            case LEFT -> frontX--;
            case RIGHT -> frontX++;
        }

        Position frontPos = new Position(frontX, frontY);

        // Priority 2: Check for station interaction
        Station station = map.getStationAt(frontX, frontY);
        if (station != null) {
            return executeStationInteraction(station);
        }

        // Priority 3: Check for floor interaction (pickup/drop)
        return executeFloorInteraction(frontPos);
    }

    /**
     * Execute pickup at chef's current position
     */
    private boolean executePickupAtCurrentPosition(Position pos) {
        Item item = itemsOnFloor.remove(pos);
        if (item == null) return false;

        chef.pickUp(item);

        lastInteractionType = InteractionType.PICKUP;
        lastItem = item;
        lastItemPosition = pos;

        System.out.println("[CMD] " + chef.getName() + " picked up " + item.getName() + " from current position");
        return true;
    }

    /**
     * Execute station interaction
     */
    private boolean executeStationInteraction(Station station) {
        try {
            station.interact(chef);

            lastInteractionType = InteractionType.STATION;

            System.out.println("[CMD] " + chef.getName() + " interacted with " + station.getType());
            return true;
        } catch (Exception e) {
            System.err.println("[CMD] Station interaction failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Execute floor interaction (pickup or drop)
     */
    private boolean executeFloorInteraction(Position frontPos) {
        if (chef.hasItem()) {
            return executeDropItem(frontPos);
        }

        if (itemsOnFloor.containsKey(frontPos)) {
            return executePickupItem(frontPos);
        }

        System.out.println("[CMD] No valid interaction available");
        return false;
    }

    /**
     * Execute drop item to floor
     */
    private boolean executeDropItem(Position frontPos) {
        if (!map.inBounds(frontPos.getX(), frontPos.getY())) {
            System.out.println("[CMD] Cannot drop out of bounds");
            return false;
        }

        if (!map.isWalkable(frontPos.getX(), frontPos.getY())) {
            System.out.println("[CMD] Cannot drop on non-walkable tile");
            return false;
        }

        if (itemsOnFloor.containsKey(frontPos)) {
            System.out.println("[CMD] Position already has an item");
            return false;
        }

        Item droppedItem = chef.drop();
        if (droppedItem == null) {
            System.out.println("[CMD] Failed to drop item");
            return false;
        }

        itemsOnFloor.put(frontPos, droppedItem);

        lastInteractionType = InteractionType.DROP;
        lastItem = droppedItem;
        lastItemPosition = frontPos;

        System.out.println("[CMD] " + chef.getName() + " dropped " + droppedItem.getName() + " at (" +
                frontPos.getX() + ", " + frontPos.getY() + ")");
        return true;
    }

    /**
     * Execute pickup item from floor
     */
    private boolean executePickupItem(Position frontPos) {
        Item item = itemsOnFloor.get(frontPos);
        if (item == null) return false;

        if (chef.hasItem()) {
            System.out.println("[CMD] Chef already holding an item");
            return false;
        }

        itemsOnFloor.remove(frontPos);
        chef.pickUp(item);

        lastInteractionType = InteractionType.PICKUP;
        lastItem = item;
        lastItemPosition = frontPos;

        System.out.println("[CMD] " + chef.getName() + " picked up " + item.getName() + " from floor");
        return true;
    }

    @Override
    public void undo() {
        switch (lastInteractionType) {
            case PICKUP -> {
                if (lastItem != null && lastItemPosition != null) {
                    chef.drop();
                    itemsOnFloor.put(lastItemPosition, lastItem);
                    System.out.println("[CMD] Undone pickup");
                }
            }
            case DROP -> {
                if (lastItem != null && lastItemPosition != null) {
                    itemsOnFloor.remove(lastItemPosition);
                    chef.pickUp(lastItem);
                    System.out.println("[CMD] Undone drop");
                }
            }
            case STATION -> {
                System.out.println("[CMD] Cannot undo station interaction");
            }
            default -> {
            }
        }

        lastInteractionType = InteractionType.NONE;
    }

    @Override
    public String getDescription() {
        String action = switch (lastInteractionType) {
            case PICKUP -> "picked up item";
            case DROP -> "dropped item";
            case STATION -> "interacted with station";
            default -> "attempted interaction";
        };

        return String.format("%s %s", chef.getName(), action);
    }

    /**
     * Get the chef performing this command
     */
    public ChefPlayer getChef() {
        return chef;
    }

    /**
     * Get last interaction type
     */
    public InteractionType getLastInteractionType() {
        return lastInteractionType;
    }
}