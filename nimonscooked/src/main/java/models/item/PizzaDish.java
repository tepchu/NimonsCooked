package models.item;

import java.util.List;
import java.util.ArrayList;

public class PizzaDish extends Dish {
    private boolean baked;
    private boolean burned;

    public PizzaDish(String dishName) {
        super(dishName);
        this.baked = false;
        this.burned = false;
    }

    public PizzaDish(String dishName, List<Preparable> choppedIngredients) {
        super(dishName);
        this.components.addAll(choppedIngredients);
        this.baked = false;
        this.burned = false;
    }

    public boolean isBaked() {
        return baked;
    }

    public void bake() {
        this.baked = true;
    }

    public boolean isBurned() {
        return burned;
    }

    public void burn() {
        this.burned = true;
        this.baked = true;
    }

    public boolean isReadyToServe() {
        return baked && !burned;
    }

    @Override
    public void showItem() {
        String status;
        if (burned) {
            status = "BURNED ✗ (throw away!)";
        } else if (baked) {
            status = "BAKED ✓";
        } else {
            status = "UNBAKED (needs oven)";
        }
        System.out.println("Pizza: " + getDishName() + " [" + status + "] with " +
                components.size() + " ingredients");
    }
}