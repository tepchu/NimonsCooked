package models.item.kitchenutensils;

import models.item.Preparable;
import models.item.PizzaDish;

public class Oven extends KitchenUtensil implements CookingDevice {

    private PizzaDish currentPizza;
    private boolean isCooking;
    private int cookingProgress;
    private static final int BAKE_TIME = 12;
    private static final int BURN_TIME = 24; // Burns after 24 seconds total
    private boolean burned;

    public Oven() {
        super("Oven");
        this.currentPizza = null;
        this.isCooking = false;
        this.cookingProgress = 0;
        this.burned = false;
    }

    @Override
    public boolean isPortable() {
        return false;
    }

    @Override
    public int capacity() {
        return 1;
    }

    @Override
    public boolean canAccept(Preparable ingredient) {
        return false;
    }

    public boolean canAcceptPizza(PizzaDish pizza) {
        return currentPizza == null && !pizza.isBaked();
    }

    public boolean placePizza(PizzaDish pizza) {
        if (canAcceptPizza(pizza)) {
            this.currentPizza = pizza;
            this.burned = false;
            return true;
        }
        return false;
    }

    public PizzaDish removePizza() {
        PizzaDish pizza = currentPizza;
        currentPizza = null;
        isCooking = false;
        cookingProgress = 0;
        burned = false;
        return pizza;
    }

    public PizzaDish getCurrentPizza() {
        return currentPizza;
    }

    public boolean hasPizza() {
        return currentPizza != null;
    }

    public boolean isEmpty() {
        return currentPizza == null;
    }

    @Override
    public void addIngredient(Preparable ingredient) {
        // Not used for oven
    }

    @Override
    public void startCooking() {
        if (currentPizza != null && !isCooking && !currentPizza.isBaked()) {
            isCooking = true;
            cookingProgress = 0;
            burned = false;
        }
    }

    public boolean isCooking() {
        return isCooking;
    }

    public int getCookingProgress() {
        return cookingProgress;
    }

    public int getBakeTime() {
        return BAKE_TIME;
    }

    public void updateCooking() {
        if (isCooking && currentPizza != null) {
            cookingProgress++;

            // Pizza is done baking
            if (cookingProgress == BAKE_TIME && !currentPizza.isBaked()) {
                finishBaking();
            }

            // Pizza burns if left too long
            if (cookingProgress >= BURN_TIME && !burned) {
                burnPizza();
            }
        }
    }

    public void finishBaking() {
        if (currentPizza != null && !burned) {
            currentPizza.bake();
            // Keep cooking to track burn time
            System.out.println("[OVEN] Pizza finished baking!");
        }
    }

    private void burnPizza() {
        if (currentPizza != null) {
            burned = true;
            currentPizza.burn(); // Mark the pizza itself as burned
            System.out.println("[OVEN] Pizza has BURNED!");
        }
    }

    public boolean isBurned() {
        return burned;
    }

    @Override
    public void showItem() {
        if (currentPizza != null) {
            if (burned) {
                System.out.println("Oven: BURNED PIZZA - Remove immediately!");
            } else {
                String status = isCooking ?
                        "Baking... (" + cookingProgress + "/" + BAKE_TIME + "s)" :
                        (currentPizza.isBaked() ? "Ready! Pick up soon!" : "Waiting to bake");
                System.out.println("Oven: " + currentPizza.getDishName() + " - " + status);
            }
        } else {
            System.out.println("Oven: Empty");
        }
    }
}