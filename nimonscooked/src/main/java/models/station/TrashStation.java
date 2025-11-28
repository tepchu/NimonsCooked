package models.station;

import models.player.ChefPlayer;
import models.item.kitchenutensils.KitchenUtensil;
import models.core.Position;
import models.enums.StationType;

public class TrashStation extends Station {

    public TrashStation(Position position) {
        super(StationType.TRASH, position);
    }

    @Override
    public void interact(ChefPlayer chef) {
        if (!chef.hasItem()) return;

        // spek: tidak boleh membuang kitchen utensil, hanya isinya
        if (chef.getInventory() instanceof KitchenUtensil) {
            // TODO: hapus isi utensil saja kalau ingin lebih detail
        } else {
            chef.drop(); // buang item
        }
    }
}
