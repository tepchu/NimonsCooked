package models.station;

import models.player.ChefPlayer;
import models.item.kitchenutensils.*;
import models.item.*;
import models.player.CurrentAction;
import models.core.Position;
import models.enums.StationType;

public class CookingStation extends Station {

    private CookingDevice device;
    private static final int BURN_TIME = 24; // Pizza burns after 24 seconds total (12s after done)

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
                    // Place pizza in oven
                    oven.placePizza(pizza);
                    plate.setDish(null);
                    oven.startCooking();

                    // Chef is NOT busy - oven works automatically!
                    System.out.println("[OVEN] Pizza placed in oven - now baking automatically");
                    System.out.println("[OVEN] Pizza will be ready in " + oven.getBakeTime() + " seconds");
                    return;
                }
            } else if (plate.isClean() && !plate.hasDish()) {
                if (oven.hasPizza()) {
                    PizzaDish pizza = oven.getCurrentPizza();

                    // Check if burned
                    if (oven.isBurned()) {
                        System.out.println("[OVEN] ✗ Pizza is BURNED! Pick it up to throw it away.");
                        // Pick up burned pizza - it still goes on the plate
                        PizzaDish burnedPizza = oven.removePizza();
                        plate.setDish(burnedPizza);
                        System.out.println("[OVEN] Picked up burned pizza. Take it to the trash!");
                        return;
                    }

                    if (pizza.isBaked()) {
                        PizzaDish bakedPizza = oven.removePizza();
                        plate.setDish(bakedPizza);
                        System.out.println("[OVEN] ✓ Picked up baked pizza!");
                    } else {
                        System.out.println("[OVEN] Pizza is still cooking...");
                    }
                }
            }
        } else if (!chef.hasItem() && oven.hasPizza()) {
            if (oven.isBurned()) {
                System.out.println("[OVEN] ✗ Burned pizza! Need a plate to remove it.");
            } else if (oven.getCurrentPizza().isBaked()) {
                System.out.println("[OVEN] Need a plate to pick up the pizza!");
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

            // Check for warnings
            if (oven.isCooking()) {
                int progress = oven.getCookingProgress();
                int bakeTime = oven.getBakeTime();

                if (progress == bakeTime) {
                    System.out.println("[OVEN] ⚠ Pizza is DONE! Pick it up now!");
                } else if (progress == bakeTime + 6) {
                    System.out.println("[OVEN] ⚠⚠ WARNING! Pizza will burn in 6 seconds!");
                } else if (progress >= BURN_TIME) {
                    if (!oven.isBurned()) {
                        System.out.println("[OVEN] ✗✗✗ PIZZA BURNED! ✗✗✗");
                    }
                }
            }
        }
    }

    public boolean hasBurnedPizza() {
        if (device instanceof Oven oven) {
            return oven.isBurned();
        }
        return false;
    }
}