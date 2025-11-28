package models.item.kitchenutensils;

import models.item.Preparable;

public class Oven extends KitchenUtensil implements CookingDevice {
    private static final int MAX_CAPACITY = 5;

    public Oven() {
        super("Oven");
    }

    @Override
    public boolean isPortable() {
        return false;
    }

    @Override
    public int capacity() {
        return MAX_CAPACITY;
    }

    @Override
    public boolean canAccept(Preparable ingredient) {
        return contents.size() < MAX_CAPACITY;
    }

    @Override
    public void addIngredient(Preparable ingredient) {
        if (canAccept(ingredient)) {
            contents.add(ingredient);
        }
    }

    @Override
    public void startCooking() {

    }
}
