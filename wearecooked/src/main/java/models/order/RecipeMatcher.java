package models.order;

import models.item.Dish;
import models.recipe.Recipe;

public class RecipeMatcher {
    public static boolean doesDishMatchRecipe(Dish dish, Recipe recipe) {
        if (dish == null || recipe == null) {
            return false;
        }
        return recipe.matchesDish(dish);
    }
}
