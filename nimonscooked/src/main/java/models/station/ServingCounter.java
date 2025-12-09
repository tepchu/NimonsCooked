package models.station;

import models.player.ChefPlayer;
import models.item.Dish;
import models.item.PizzaDish;
import models.item.kitchenutensils.Plate;
import controllers.Stage;
import models.core.Position;
import models.enums.StationType;

public class ServingCounter extends Station {

    private Stage stage; // untuk validasi order

    public ServingCounter(Position position, Stage stage) {
        super(StationType.SERVING_COUNTER, position);
        this.stage = stage;
    }

    @Override
    public void interact(ChefPlayer chef) {
        if (!(chef.getInventory() instanceof Plate plate)) {
            System.out.println("[SERVE] Chef doesn't have a plate");
            return;
        }

        if (!plate.isClean()) {
            System.out.println("[SERVE] Plate is dirty, cannot serve");
            return;
        }

        if (plate.getDish() == null) {
            System.out.println("[SERVE] Plate has no dish");
            return;
        }

        Dish dish = plate.getDish();

        // Check if pizza is baked
        if (dish instanceof PizzaDish pizza && !pizza.isBaked()) {
            System.out.println("[SERVE] Pizza is not baked yet!");
            return;
        }

        // Remove plate from chef's inventory
        chef.drop();

        // Validate and serve - pass the plate so it can be returned later
        int result = stage.validateServe(dish, plate);

        if (result > 0) {
            System.out.println("[SERVE] Order completed!  Reward: $" + result);
        } else if (result < 0) {
            System.out.println("[SERVE] Wrong dish!  Penalty: $" + Math.abs(result));
        } else {
            System.out.println("[SERVE] Serve failed");
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
