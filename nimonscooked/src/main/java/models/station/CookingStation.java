package models.station;

import models.player.ChefPlayer;
import models.item.kitchenutensils.*;
import models.item.*;
import models.player.CurrentAction;
import models.core.Position;
import models.enums.StationType;

public class CookingStation extends Station {

    private CookingDevice device;  // pot/pan/oven yang lagi di atas station

    public CookingStation(Position position) {
        super(StationType.COOKING, position);
    }

    public void placeDevice(CookingDevice device) {
        this.device = device;
    }

    public CookingDevice getDevice() {
        return device;
    }

    public boolean hasOven() {
        return device instanceof Oven;
    }

    @Override
    public void interact(ChefPlayer chef) {
        if (device instanceof Oven oven) {
            handleOvenInteraction(chef, oven);
        } else {
            handleRegularCookingDevice(chef);
        }
    }

    private void handleOvenInteraction(ChefPlayer chef, Oven oven) {
        Item chefItem = chef.getInventory();

        if (chefItem instanceof Plate plate) {
            if (plate.getDish() instanceof PizzaDish pizza && !pizza.isBaked()) {
                if (oven.isEmpty()) {
                    oven.placePizza(pizza);
                    plate.setDish(null);
                    oven.startCooking();

                    chef.startBusy(CurrentAction.COOKING, oven.getBakeTime(), oven::finishBaking);
                }
            }
            else if (plate.isClean() && !plate.hasDish()) {
                if (oven.hasPizza() && oven.getCurrentPizza().isBaked()) {
                    PizzaDish bakedPizza = oven.removePizza();
                    plate.setDish(bakedPizza);
                }
            }
        }
    }

    private void handleRegularCookingDevice(ChefPlayer chef) {
        if (chef.getInventory() instanceof CookingDevice d) {
            chef.drop();
            this.device = d;
        } else if (device != null && !chef.hasItem()) {
            chef.pickUp((Item) device);
            device = null;
        }
    }

    public void update() {
        if (device instanceof Oven oven) {
            oven.updateCooking();
        }
    }
}
