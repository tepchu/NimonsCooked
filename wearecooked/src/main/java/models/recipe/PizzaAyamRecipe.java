package models.recipe;

import java.util.ArrayList;
import java.util.List;

import models.enums.IngredientState;
import models.ingredients.Dough;
import models.ingredients.Tomato;
import models.ingredients.Cheese;
import models.ingredients.Chicken;

public class PizzaAyamRecipe extends Recipe {

    public static final int SERVE_TIME_SECONDS = 90;

    public PizzaAyamRecipe() {
        super(
                "PIZZA_AYAM",
                "Pizza Ayam",
                "Pizza dengan adonan, tomat, keju, dan ayam cincang.",
                buildRequirements(),
                160,
                -50,
                15,
                true,
                SERVE_TIME_SECONDS
        );
    }

    private static List<RecipeIngredientRequirement> buildRequirements() {
        List<RecipeIngredientRequirement> req = new ArrayList<>();
        req.add(new RecipeIngredientRequirement(Dough.class, IngredientState.CHOPPED, 1));
        req.add(new RecipeIngredientRequirement(Tomato.class, IngredientState.CHOPPED, 1));
        req.add(new RecipeIngredientRequirement(Cheese.class, IngredientState.CHOPPED, 1));
        req.add(new RecipeIngredientRequirement(Chicken.class, IngredientState.CHOPPED, 1));
        return req;
    }
}
