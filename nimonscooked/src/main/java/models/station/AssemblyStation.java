package models.station;

import models.enums.IngredientState;
import models.player.ChefPlayer;
import models.item.*;
import models.item.kitchenutensils.Plate;
import models.core.Position;
import models.enums.StationType;
import java.util.ArrayList;
import java.util.List;

public class AssemblyStation extends Station {

    private Plate plateOnStation;
    private Ingredient ingredientOnStation;

    public AssemblyStation(Position position) {
        super(StationType.ASSEMBLY, position);
        this.plateOnStation = null;
        this.ingredientOnStation = null;
    }

    @Override
    public void interact(ChefPlayer chef) {
        Item chefItem = chef.getInventory();

        if (chefItem instanceof Plate plate && plate.isClean() && plateOnStation == null) {
            chef.drop();
            plateOnStation = plate;
            return;
        }

        if (chefItem instanceof Ingredient ing &&
                ing.getState() == IngredientState.CHOPPED &&
                ingredientOnStation == null) {
            chef.drop();
            ingredientOnStation = ing;
            return;
        }

        if (plateOnStation != null && ingredientOnStation != null) {
            if (!chef.hasItem()) {
                if (isPizzaIngredient(ingredientOnStation)) {
                    addToPizzaDish(plateOnStation, ingredientOnStation);
                } else {
                    addToRegularDish(plateOnStation, ingredientOnStation);
                }
                ingredientOnStation = null; // Inserted into plate
                return;
            }
        }

        if (!chef.hasItem() && plateOnStation != null && ingredientOnStation == null) {
            chef.pickUp(plateOnStation);
            plateOnStation = null;
            return;
        }

        if (!chef.hasItem() && ingredientOnStation != null && plateOnStation == null) {
            chef.pickUp((Item) ingredientOnStation);
            ingredientOnStation = null;
            return;
        }

        if (chefItem instanceof Plate plate && plate.isClean() &&
                ingredientOnStation != null && plateOnStation == null) {

            if (isPizzaIngredient(ingredientOnStation)) {
                addToPizzaDish(plate, ingredientOnStation);
            } else {
                addToRegularDish(plate, ingredientOnStation);
            }

            ingredientOnStation = null; // Inserted into plate
            return;
        }
    }

    private boolean isPizzaIngredient(Ingredient ing) {
        String name = ing.getClass().getSimpleName().toUpperCase();
        return name.contains("DOUGH") || name.contains("TOMATO") ||
                name.contains("CHEESE") || name.contains("SAUSAGE") ||
                name.contains("CHICKEN");
    }

    private void addToPizzaDish(Plate plate, Ingredient ingredient) {
        if (plate.getDish() instanceof PizzaDish pizza) {
            pizza.addComponent(ingredient);
        } else {
            List<Preparable> ingredients = new ArrayList<>();
            ingredients.add(ingredient);
            PizzaDish pizza = new PizzaDish("Unbaked Pizza", ingredients);
            plate.setDish(pizza);
        }
    }

    private void addToRegularDish(Plate plate, Ingredient ingredient) {
        if (plate.getDish() != null) {
            plate.getDish().addComponent(ingredient);
        } else {
            Dish dish = new Dish("Mixed Dish");
            dish.addComponent(ingredient);
            plate.setDish(dish);
        }
    }

    public Plate getPlateOnStation() {
        return plateOnStation;
    }

    public Ingredient getIngredientOnStation() {
        return ingredientOnStation;
    }

    public boolean hasPlate() {
        return plateOnStation != null;
    }

    public boolean hasIngredient() {
        return ingredientOnStation != null;
    }
}
