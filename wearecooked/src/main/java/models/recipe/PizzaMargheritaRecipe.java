package models.recipe;

import java.util.ArrayList;
import java.util.List;

import models.enums.IngredientState;
import models.ingredients.Dough;
import models.ingredients.Tomato;
import models.ingredients.Cheese;

public class PizzaMargheritaRecipe extends Recipe {

    public static final int SERVE_TIME_SECONDS = 75;

    public PizzaMargheritaRecipe() {
        super(
                "PIZZA_MARGHERITA",
                "Pizza Margherita",
                "Pizza klasik dengan adonan, tomat, dan keju.",
                buildRequirements(),
                120,
                -50,
                12,
                true,
                SERVE_TIME_SECONDS
        );
    }

    private static List<RecipeIngredientRequirement> buildRequirements() {
        List<RecipeIngredientRequirement> req = new ArrayList<>();
        req.add(new RecipeIngredientRequirement(Dough.class, IngredientState.CHOPPED, 1));
        req.add(new RecipeIngredientRequirement(Tomato.class, IngredientState.CHOPPED, 1));
        req.add(new RecipeIngredientRequirement(Cheese.class, IngredientState.CHOPPED, 1));
        return req;
    }
}
