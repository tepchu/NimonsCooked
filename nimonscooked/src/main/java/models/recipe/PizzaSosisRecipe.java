package models.recipe;

import java.util.ArrayList;
import java.util.List;
import models.enums.IngredientState;
import models.ingredients.Dough;
import models.ingredients.Tomato;
import models.ingredients.Cheese;
import models.ingredients.Sausage;

public class PizzaSosisRecipe extends Recipe {

    public PizzaSosisRecipe() {
        super(
            "PIZZA_SOSIS",
            "Pizza Sosis",
            "Pizza dengan adonan, tomat, keju, dan sosis.",
            buildRequirements(),
            150,
            -50,
            14,
            true
        );
    }

    private static List<RecipeIngredientRequirement> buildRequirements() {
        List<RecipeIngredientRequirement> req = new ArrayList<>();
        req.add(new RecipeIngredientRequirement(Dough.class, IngredientState.CHOPPED, 1));
        req.add(new RecipeIngredientRequirement(Tomato.class, IngredientState.CHOPPED, 1));
        req.add(new RecipeIngredientRequirement(Cheese.class, IngredientState.CHOPPED, 1));
        req.add(new RecipeIngredientRequirement(Sausage.class, IngredientState.CHOPPED, 1));
        return req;
    }
}
