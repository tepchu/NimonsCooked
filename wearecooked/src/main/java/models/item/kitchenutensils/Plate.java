package models.item.kitchenutensils;

import models.item.Dish;

public class Plate extends KitchenUtensil {
    private boolean clean;
    private Dish dish;

    public Plate() {
        super("Plate");
        this.clean = true;
        this.dish = null;
    }

    public boolean isClean() {
        return clean;
    }

    public void setClean(boolean clean) {
        this.clean = clean;
    }

    public void markDirty() {
        this.clean = false;
    }

    public Dish getDish() { return dish; }

    public void setDish(Dish dish) { this.dish = dish; }

    public boolean hasDish() { return dish != null; }


    @Override
    public void showItem() {
        System.out.println("Plate (" + (clean ? "Clean" : "Dirty") + ")");
    }
}
