package models.ingredients;

import models.item.Ingredient;
import models.enums.IngredientType;
import models.enums.IngredientState;

public class Sausage extends Ingredient {
    public Sausage() {
        super("Sausage", IngredientType.SAUSAGE);
    }

    @Override
    public boolean canBeChopped() {
        return state == IngredientState.RAW;
    }

    @Override
    public boolean canBeCooked() {
        return false;
    }

    @Override
    public boolean canBePlacedOnPlate() {
        return state == IngredientState.CHOPPED;
    }
}
