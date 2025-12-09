package models.station;

import models.player.ChefPlayer;
import models.item.Item;
import models.item.Ingredient;
import models.item.kitchenutensils.KitchenUtensil;
import models.item.kitchenutensils.Plate;
import models.item.Dish;
import models.core.Position;
import models.enums.StationType;

public class TrashStation extends Station {

    public TrashStation(Position position) {
        super(StationType.TRASH, position);
    }

    @Override
    public void interact(ChefPlayer chef) {
        if (!chef.hasItem()) {
            System.out.println("[TRASH] Chef has nothing to throw away");
            return;
        }

        Item item = chef.getInventory();

        // Case 1: Ingredient - can be thrown away
        if (item instanceof Ingredient) {
            chef.drop(); // Remove from inventory (item is discarded)
            System.out.println("[TRASH] Threw away ingredient:  " + item.getName());
            return;
        }

        // Case 2: Plate with dish - throw away the dish contents, keep the plate
        if (item instanceof Plate plate) {
            if (plate.hasDish()) {
                Dish dish = plate.getDish();
                plate.setDish(null); // Remove dish from plate
                plate.markDirty();   // Plate becomes dirty after use
                System.out.println("[TRASH] Threw away dish: " + dish.getDishName() + ". Plate is now dirty.");
            } else {
                System.out.println("[TRASH] Plate is empty, nothing to throw away");
            }
            return;
        }

        // Case 3: Other KitchenUtensil - cannot throw away the utensil itself
        if (item instanceof KitchenUtensil) {
            System.out.println("[TRASH] Cannot throw away kitchen utensils");
            return;
        }

        // Case 4: Other items - can be thrown away
        chef.drop();
        System.out.println("[TRASH] Threw away:  " + item.getName());
    }
}
