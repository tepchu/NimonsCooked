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
        stack.push(plate);
    }
}
