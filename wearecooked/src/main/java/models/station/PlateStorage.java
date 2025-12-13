package models.station;

import java.util.ArrayDeque;
import java.util.Deque;

import models.player.ChefPlayer;
import models.item.kitchenutensils.Plate;
import models.core.Position;
import models.enums.StationType;
import models.item.Item;

public class PlateStorage extends Station {

    private final Deque<Plate> cleanPlates = new ArrayDeque<>();
    private final Deque<Plate> dirtyPlates = new ArrayDeque<>();

    public PlateStorage(Position position, int initialCleanPlates) {
        super(StationType.PLATE_STORAGE, position);
        for (int i = 0; i < initialCleanPlates; i++) {
            cleanPlates.push(new Plate());
        }
    }

    @Override
    public void interact(ChefPlayer chef) {
        Item chefItem = chef.getInventory();

        // Case 1: Chef holding dirty plate(s) - deposit them
        if (chefItem instanceof Plate plate && !plate.isClean()) {
            chef.drop();
            dirtyPlates.push(plate);
            System.out.println("[PLATE_STORAGE] Deposited dirty plate. Total dirty: " + dirtyPlates.size());
            return;
        }

        // Case 2: Chef picks up clean plate
        if (!chef.hasItem() && !cleanPlates.isEmpty()) {
            chef.pickUp(cleanPlates.pop());
            System.out.println("[PLATE_STORAGE] Picked up clean plate. Remaining: " + cleanPlates.size());
            return;
        }

        // Case 3: Chef picks up ALL dirty plates at once
        if (!chef.hasItem() && !dirtyPlates.isEmpty()) {
            // Create a bundle - just pick the top one (represents all)
            Plate topDirty = dirtyPlates.pop();
            chef.pickUp(topDirty);
            System.out.println("[PLATE_STORAGE] Picked up dirty plate. Remaining dirty: " + dirtyPlates.size());
            return;
        }

        System.out.println("[PLATE_STORAGE] No valid interaction");
    }

    public void pushDirtyPlate(Plate plate) {
        plate.markDirty();
        plate.setDish(null);
        dirtyPlates.push(plate);
        System.out.println("[PLATE_STORAGE] Dirty plate returned. Total dirty: " + dirtyPlates.size());
    }

    public void pushCleanPlate(Plate plate) {
        plate.setClean(true);
        cleanPlates.push(plate);
        System.out.println("[PLATE_STORAGE] Clean plate added. Total clean: " + cleanPlates.size());
    }

    public int getCleanPlateCount() {
        return cleanPlates.size();
    }

    public int getDirtyPlateCount() {
        return dirtyPlates.size();
    }

    public int getTotalPlateCount() {
        return cleanPlates.size() + dirtyPlates.size();
    }

    public boolean hasCleanPlates() {
        return !cleanPlates.isEmpty();
    }

    public boolean hasDirtyPlates() {
        return !dirtyPlates.isEmpty();
    }
}