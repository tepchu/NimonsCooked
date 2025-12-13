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
 * @version 1.1
 */
public class InteractCommand implements ChefCommand {
    private final ChefPlayer chef;
    private final GameMap map;
    private final Map<Position, Item> itemsOnFloor;

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
    }

    @Override
    public boolean canExecute() {
        return !chef.isBusy();
    }

    @Override
    public boolean execute() {
        if (!canExecute()) {
            return false;
        }

        Position chefPos = chef.getPosition();
        Direction dir = chef.getDirection();

        // Calculate position in front of chef
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
            System.out.println("[CMD] Interacted with " + station.getType());
            return true;
        }

        System.out.println("[CMD] No station in front");
        return false;
    }

    @Override
    public void undo() {
        // Stations don't support undo
        System.out.println("[CMD] Cannot undo station interaction");
    }

    @Override
    public String getDescription() {
        return "Interacted with station";
    }

}