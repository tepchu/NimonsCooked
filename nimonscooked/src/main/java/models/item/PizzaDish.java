package models.item;

import models.enums.IngredientType;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

public class PizzaDish extends Dish {
    private boolean baked;
    private boolean burned;

    private static final List<IngredientType> LAYER_ORDER = List.of(
            IngredientType.DOUGH,    // Bottom
            IngredientType.TOMATO,   // Sauce
            IngredientType.CHEESE,   // Cheese
            IngredientType.CHICKEN,  // Toppings
            IngredientType.SAUSAGE   // Toppings
    );

    public PizzaDish(String dishName) {
        super(dishName);
        this.baked = false;
        this.burned = false;
    }

    public PizzaDish(String dishName, List<Preparable> choppedIngredients) {
        super(dishName);
        this.components.addAll(choppedIngredients);
        sortIngredients();
        this.baked = false;
        this.burned = false;
    }

    @Override
    public void addComponent(Preparable ingredient) {
        components.add(ingredient);
        sortIngredients();
    }

    private void sortIngredients() {
        components.sort(new Comparator<Preparable>() {
            @Override
            public int compare(Preparable p1, Preparable p2) {
                if (!(p1 instanceof Ingredient) || !(p2 instanceof Ingredient)) {
                    return 0;
                }

                Ingredient i1 = (Ingredient) p1;
                Ingredient i2 = (Ingredient) p2;

                int order1 = LAYER_ORDER.indexOf(i1.getType());
                int order2 = LAYER_ORDER.indexOf(i2.getType());

                if (order1 == -1) order1 = 999;
                if (order2 == -1) order2 = 999;

                return Integer.compare(order1, order2);
            }
        });
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