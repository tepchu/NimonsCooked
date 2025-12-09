package models.item.kitchenutensils;

import models.item.Preparable;
import models.item.PizzaDish;

public class Oven extends KitchenUtensil implements CookingDevice {

    private PizzaDish currentPizza;
    private boolean isCooking;
    private int cookingProgress;
    private static final int BAKE_TIME = 12;

    public Oven() {
        super("Oven");
        this.currentPizza = null;
        this.isCooking = false;
        this.cookingProgress = 0;
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
            return true;
        }
        return false;
    }

    public PizzaDish removePizza() {
        PizzaDish pizza = currentPizza;
        currentPizza = null;
        isCooking = false;
        cookingProgress = 0;
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
            if (cookingProgress >= BAKE_TIME) {
                finishBaking();
            }
        }
    }

    public void finishBaking() {
        if (currentPizza != null && isCooking) {
            currentPizza.bake();
            isCooking = false;
        }
    }

    @Override
    public void showItem() {
        if (currentPizza != null) {
            String status = isCooking ?
                    "Baking... (" + cookingProgress + "/" + BAKE_TIME + "s)" :
                    (currentPizza.isBaked() ? "Ready!" : "Waiting to bake");
            System.out.println("Oven: " + currentPizza.getDishName() + " - " + status);
        } else {
            System.out.println("Oven: Empty");
        }
    }
}