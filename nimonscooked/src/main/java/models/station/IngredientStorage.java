package models.station;

import models.player.ChefPlayer;
import models.factory.IngredientFactory;
import models.enums.IngredientType;
import models.core.Position;
import models.enums.StationType;

public class IngredientStorage extends Station {

    private final IngredientType type;

    public IngredientStorage(Position pos, IngredientType type) {
        super(StationType.INGREDIENT_STORAGE, pos);
        this.type = type;
    }

    @Override
    public void interact(ChefPlayer chef) {
        if (!chef.hasItem()) {
           chef.pickUp(IngredientFactory.createIngredient(type));

        }
    }
}
