package models.recipe;

import models.item.Ingredient;
import models.item.Preparable;
import models.enums.IngredientState;

public class RecipeIngredientRequirement {

    private Class<? extends Ingredient> ingredientType;
    private IngredientState requiredState;
    private int quantity;

    public RecipeIngredientRequirement(Class<? extends Ingredient> ingredientType,
                                       IngredientState requiredState,
                                       int quantity) {
        this.ingredientType = ingredientType;
        this.requiredState = requiredState;
        this.quantity = quantity;
    }

    public Class<? extends Ingredient> getIngredientType() {
        return ingredientType;
    }

    public IngredientState getRequiredState() {
        return requiredState;
    }

    public int getQuantity() {
        return quantity;
    }

    /** Check if this Preparable satisfies recipe requirement */
    public boolean matches(Preparable p) {
        if (!(p instanceof Ingredient)) return false;

        Ingredient ing = (Ingredient) p;

        return ingredientType.isInstance(ing) &&
               ing.getState() == requiredState;
    }
}

