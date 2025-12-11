package models.station;

import java.util.ArrayDeque;
import java.util.Deque;

import models.player.ChefPlayer;
import models.item.kitchenutensils.Plate;
import models.core.Position;
import models.enums.StationType;

public class PlateStorage extends Station {

    // stack: paling atas posisi terakhir
    private final Deque<Plate> stack = new ArrayDeque<>();

    public PlateStorage(Position position, int initialCleanPlates) {
        super(StationType.PLATE_STORAGE, position);
        for (int i = 0; i < initialCleanPlates; i++) {
            stack.push(new Plate());
        }
    }

    @Override
    public void interact(ChefPlayer chef) {
        // spek: tidak bisa drop item apapun di sini, hanya mekanik stack
        if (chef.hasItem()) {
            System.out.println("[PLATE_STORAGE] ✗ Cannot drop items here! This is a plate-only storage.");
            System.out.println("[PLATE_STORAGE] Dirty plates are returned automatically after serving.");
            return;
        }

        if (!chef.hasItem() && !stack.isEmpty()) {
            // kalau top adalah clean plate → ambil 1
            Plate top = stack.peek();
            if (top.isClean()) {
                chef.pickUp(stack.pop());
            } else {
                // piring kotor di atas → bisa diambil semuanya
                // (versi simple: ambil satu bundle piring kotor)
                chef.pickUp(stack.pop());
            }
        }
        // piring kotor akan dikembalikan ke stack ini oleh kitchen loop setelah serving
    }

    public void pushDirtyPlate(Plate plate) {
        plate.markDirty();
        plate.setDish(null); // Ensure no dish attached
        stack.push(plate);
        System.out.println("[PLATE_STORAGE] Dirty plate returned to storage. Total plates: " + stack.size());
    }

    public boolean hasDirtyPlateOnTop() {
        if (stack.isEmpty()) return false;
        return !stack.peek().isClean();
    }

    public int getCleanPlateCount() {
        int count = 0;
        boolean foundDirty = false;

        for (Plate plate : stack) {
            if (!plate.isClean()) {
                foundDirty = true;
            } else if (!foundDirty) {
                count++;
            }
        }

        return count;
    }

    public int getDirtyPlateCount() {
        int count = 0;

        for (Plate plate : stack) {
            if (!plate.isClean()) {
                count++;
            } else {
                break; // Clean plates are below
            }
        }

        return count;
    }

    public int getTotalPlateCount() {
        return stack.size();
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public boolean hasCleanPlates() {
        if (stack.isEmpty()) return false;
        // Clean plates available only if top is clean OR if we count them
        return getCleanPlateCount() > 0;
    }
}
