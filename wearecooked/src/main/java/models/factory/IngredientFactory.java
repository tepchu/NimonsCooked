package models.factory;

import models.item.Ingredient;
import models.enums.IngredientType;
import models.ingredients.*;

public class IngredientFactory {
    public static Ingredient createIngredient(IngredientType type){
        switch (type) {
            case TOMATO -> {
                return new Tomato();
            }
            case DOUGH -> {
                return new Dough();
            }
            case CHEESE -> {
                return new Cheese();
            }
            case SAUSAGE -> {
                return new Sausage();
            }
            case CHICKEN -> {
                return new Chicken();
            }
            default -> throw new IllegalArgumentException("Unknown ingredient type: " + type);
        }
    }

    public static Ingredient createIngredient(String name) {
        try {
            IngredientType type = IngredientType.valueOf(name.toUpperCase());
            return createIngredient(type);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown ingredient: " + name);
        }
    }
}
