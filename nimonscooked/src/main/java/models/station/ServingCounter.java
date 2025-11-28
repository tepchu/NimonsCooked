package models.station;

import models.player.ChefPlayer;
import models.item.Dish;
import models.item.kitchenutensils.Plate;
import controllers.Stage;
import models.core.Position;
import models.enums.StationType;

public class ServingCounter extends Station {

    private final Stage stage; // untuk validasi order

    public ServingCounter(Position position, Stage stage) {
        super(StationType.SERVING_COUNTER, position);
        this.stage = stage;
    }

    @Override
    public void interact(ChefPlayer chef) {
        if (chef.getInventory() instanceof Plate plate && plate.getDish() != null && !plate.isDirty()) {
            Dish dish = plate.getDish();
            int score = stage.validateServe(dish);
            // kalau score > 0 berarti order match
            plate.makeDirty();
            plate.setDish(null);
            chef.drop();
            // piring kotor akan dikirim ke PlateStorage oleh kitchen loop (sesuai spek)
        }
    }
}
