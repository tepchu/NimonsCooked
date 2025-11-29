package models.recipe;

import java.util.ArrayList;
import java.util.List;

public class PizzaRecipeFactory {

    public List<Recipe> createAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();

        recipes.add(new PizzaMargheritaRecipe());
        recipes.add(new PizzaSosisRecipe());
        recipes.add(new PizzaAyamRecipe());

        return recipes;
    }

    public Recipe getRecipeByName(String name) {
        for (Recipe r : createAllRecipes()) {
            if (r.getName().equalsIgnoreCase(name)) {
                return r;
            }
        }
        return null;
    }

    public Recipe getRecipeById(String id) {
        for (Recipe r : createAllRecipes()) {
            if (r.getId().equalsIgnoreCase(id)) {
                return r;
            }
        }
        return null;
    }
}
