package models.station;

import models.player.ChefPlayer;
import models.item.Ingredient;
import models.enums.IngredientType;
import models.core.Position;
import models.enums.StationType;

public class IngredientStorage extends Station {

    private final IngredientType type;

    public IngredientStorage(Position position, IngredientType type) {
        super(StationType.INGREDIENT_STORAGE, position);
        this.type = type;
    }

    @Override
    public void interact(ChefPlayer chef) {
        if (!chef.hasItem()) {
            chef.pickUp(new Ingredient(type));
        }
    }
}
