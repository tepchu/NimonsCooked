package models.station;

import models.player.ChefPlayer;
import models.item.Dish;
import models.item.Preparable;
import models.item.kitchenutensils.Plate;
import models.core.Position;
import models.enums.StationType;

public class AssemblyStation extends Station {

    private Plate plateOnStation;      // bisa null
    private Preparable looseIngredient; // ingredient/dish di atas meja

    public AssemblyStation(Position position) {
        super(StationType.ASSEMBLY, position);
    }

    @Override
    public void interact(ChefPlayer chef) {
        // Logic plating basic:
        // - kalau chef pegang plate bersih & ada ingredient di station -> buat dish di situ
        if (chef.getInventory() instanceof Plate p && looseIngredient != null && !p.isDirty()) {
            Dish dish = p.getDish();
            if (dish == null) {
                dish = new Dish("Custom Dish");
                p.setDish(dish);
            }
            dish.addComponent(looseIngredient);
            looseIngredient = null;
        }
        // kalau chef pegang ingredient & ada plate di station
        else if (chef.getInventory() instanceof Preparable prep && plateOnStation != null && !plateOnStation.isDirty()) {
            Dish dish = plateOnStation.getDish();
            if (dish == null) {
                dish = new Dish("Custom Dish");
                plateOnStation.setDish(dish);
            }
            dish.addComponent(prep);
            chef.drop();
        }
        // taruh plate di station
        else if (chef.getInventory() instanceof Plate p2 && plateOnStation == null) {
            plateOnStation = p2;
            chef.drop();
        }
        // angkat plate dari station
        else if (chef.getInventory() == null && plateOnStation != null) {
            chef.pickUp(plateOnStation);
            plateOnStation = null;
        }
        // taruh ingredient di station (tanpa plate)
        else if (chef.getInventory() instanceof Preparable prep2 && looseIngredient == null && plateOnStation == null) {
            looseIngredient = prep2;
            chef.drop();
        }
    }
}
