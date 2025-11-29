package models.ingredients;

import models.enums.IngredientState;
import models.enums.IngredientType;
import models.item.Ingredient;

public class Tomato extends Ingredient {
    public Tomato(){
        super("Tomato", IngredientType.TOMATO);
    }

    @Override
    public boolean canBeChopped(){
        return state == IngredientState.RAW;
    }

    @Override
    public boolean canBeCooked(){
        return state == IngredientState.CHOPPED;
    }

    @Override
    public boolean canBePlacedOnPlate(){
        return state == IngredientState.CHOPPED;
    }
}
