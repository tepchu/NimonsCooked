package models.ingredients;


import models.enums.IngredientState;
import models.enums.IngredientType;
import models.item.Ingredient;

public class Dough extends Ingredient {
    public Dough() {
        super("Dough", IngredientType.DOUGH);
    }

    @Override
    public boolean canBeChopped() {
        return state == IngredientState.RAW;
    }

    @Override
    public boolean canBeCooked() {
        // Must be a part of Pizza to be cooked
        return false;
    }

    @Override
    public boolean canBePlacedOnPlate() {
        return state == IngredientState.CHOPPED;
    }
}
