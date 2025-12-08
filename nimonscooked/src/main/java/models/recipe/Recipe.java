package models.recipe;

import java.util.List;
import models.item.Dish;
import models.item.Preparable;

public class Recipe {
    private String id;
    private String name;
    private String description;
    private List<RecipeIngredientRequirement> requiredComponents;
    private int baseReward;
    private int basePenalty;
    private int bakeTimeSeconds;
    private boolean plateRequired;

    public Recipe(String id, String name, String description,
                  List<RecipeIngredientRequirement> requiredComponents,
                  int baseReward, int basePenalty,
                  int bakeTimeSeconds, boolean plateRequired) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.requiredComponents = requiredComponents;
        this.baseReward = baseReward;
        this.basePenalty = basePenalty;
        this.bakeTimeSeconds = bakeTimeSeconds;
        this.plateRequired = plateRequired;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<RecipeIngredientRequirement> getRequiredComponents() {
        return requiredComponents;
    }

    public int getBaseReward() {
        return baseReward;
    }

    public int getBasePenalty() {
        return basePenalty;
    }

    public int getBakeTimeSeconds() {
        return bakeTimeSeconds;
    }

    public boolean isPlateRequired() {
        return plateRequired;
    }

    public boolean matchesDish(Dish dish) {
        return isSatisfiedBy(dish.getComponents());
    }

    public boolean isSatisfiedBy(List<Preparable> components) {
        for (RecipeIngredientRequirement req : requiredComponents) {
            int count = 0;
            for (Preparable p : components) {
                if (req.matches(p)) {
                    count++;
                }
            }
            if (count < req.getQuantity()) {
                return false;
            }
        }
        return true;
    }

    /** Returns missing recipe requirements */
    public List<RecipeIngredientRequirement> missingComponents(List<Preparable> components) {
        List<RecipeIngredientRequirement> missing = new java.util.ArrayList<>();

        for (RecipeIngredientRequirement req : requiredComponents) {
            int count = 0;
            for (Preparable p : components) {
                if (req.matches(p)) {
                    count++;
                }
            }
            if (count < req.getQuantity()) {
                missing.add(req);
            }
        }
        return missing;
    }
}
