package models.item;

import java.util.List;
import java.util.ArrayList;

public class PizzaDish extends Dish {
    private boolean baked;

    public PizzaDish(String dishName) {
        super(dishName);
        this.baked = false;
    }

    public PizzaDish(String dishName, List<Preparable> choppedIngredients) {
        super(dishName);
        this.components.addAll(choppedIngredients);
        this.baked = false;
    }

    public boolean isBaked() {
        return baked;
    }

    public void bake() {
        this.baked = true;
    }

    public boolean isReadyToServe() {
        return baked;
    }

    @Override
    public void showItem() {
        String status = baked ? "BAKED ✓" : "UNBAKED (needs oven)";
        System.out.println("Pizza: " + getDishName() + " [" + status + "] with " +
                components.size() + " ingredients");
    }
}