package models.command;

import models.player.ChefPlayer;
import models.core.Direction;
import models.core.Position;
import models.map.GameMap;
import models.item.Item;

import java.util.Map;

public class PickupDropCommand implements ChefCommand {
    private final ChefPlayer chef;
    private final GameMap map;
    private final Map<Position, Item> itemsOnFloor;

    private Item lastItem;
    private Position lastItemPosition;
    private boolean wasPickup;

    public PickupDropCommand(ChefPlayer chef, GameMap map, Map<Position, Item> itemsOnFloor) {
        this.chef = chef;
        this.map = map;
        this.itemsOnFloor = itemsOnFloor;
    }

    @Override
    public boolean canExecute() {
        return !chef.isBusy();
    }

    @Override
    public boolean execute() {
        if (!canExecute()) return false;

        Position chefPos = chef.getPosition();
        Direction dir = chef.getDirection();

        // Calculate front position
        int frontX = chefPos.getX();
        int frontY = chefPos.getY();

        switch (dir) {
            case UP -> frontY--;
            case DOWN -> frontY++;
            case LEFT -> frontX--;
            case RIGHT -> frontX++;
        }

        Position frontPos = new Position(frontX, frontY);

        // Priority 1: Pickup from current position
        if (!chef.hasItem() && itemsOnFloor.containsKey(chefPos)) {
            Item item = itemsOnFloor.remove(chefPos);
            chef.pickUp(item);
            lastItem = item;
            lastItemPosition = chefPos;
            wasPickup = true;
            System.out.println("[CMD] Picked up " + item.getName() + " from current position");
            return true;
        }

        // Priority 2: Pickup from front
        if (!chef.hasItem() && itemsOnFloor.containsKey(frontPos)) {
            Item item = itemsOnFloor.remove(frontPos);
            chef.pickUp(item);
            lastItem = item;
            lastItemPosition = frontPos;
            wasPickup = true;
            System.out.println("[CMD] Picked up " + item.getName() + " from front");
            return true;
        }

        // Priority 3: Drop to front
        if (chef.hasItem()) {
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
            itemsOnFloor.put(frontPos, droppedItem);
            lastItem = droppedItem;
            lastItemPosition = frontPos;
            wasPickup = false;
            System.out.println("[CMD] Dropped " + droppedItem.getName() + " at front");
            return true;
        }

        System.out.println("[CMD] No valid pickup/drop action");
        return false;
    }

    @Override
    public void undo() {
        if (wasPickup && lastItem != null) {
            chef.drop();
            itemsOnFloor.put(lastItemPosition, lastItem);
            System.out.println("[CMD] Undone pickup");
        } else if (!wasPickup && lastItem != null) {
            itemsOnFloor.remove(lastItemPosition);
            chef.pickUp(lastItem);
            System.out.println("[CMD] Undone drop");
        }
    }

    @Override
    public String getDescription() {
        return wasPickup ? "Picked up item" : "Dropped item";
    }
}