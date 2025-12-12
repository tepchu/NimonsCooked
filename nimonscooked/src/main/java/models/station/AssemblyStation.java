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
    private List<Ingredient> ingredientsOnStation; // Changed to List for multiple ingredients

    public AssemblyStation(Position position) {
        super(StationType.ASSEMBLY, position);
        this.plateOnStation = null;
        this.ingredientsOnStation = new ArrayList<>();
    }

    @Override
    public void interact(ChefPlayer chef) {
        Item chefItem = chef.getInventory();

        // Case 1: Chef places a clean plate on empty station
        if (chefItem instanceof Plate plate && plate.isClean() && plateOnStation == null) {
            chef.drop();
            plateOnStation = plate;
            System.out.println("[ASSEMBLY] Plate placed on station");
            return;
        }

        // Case 2: Chef places a chopped and raw ingredient on station (can stack multiple)
        if (chefItem instanceof Ingredient ing && 
            (ing.getState() == IngredientState.CHOPPED || ing.getState() == IngredientState.RAW)) {
            chef.drop();
            ingredientsOnStation.add(ing);
            System.out.println("[ASSEMBLY] Added " + ing.getName() + " to station.  Total ingredients: " + ingredientsOnStation.size());

            // Auto-assemble if plate is present
            if (plateOnStation != null) {
                assembleAllIngredients();
            }
            return;
        }

        // Case 3: Chef has plate with dish, wants to add more ingredients from station
        if (chefItem instanceof Plate plate && plate.isClean() && !ingredientsOnStation.isEmpty()) {
            // Add all ingredients from station to the plate
            for (Ingredient ing : ingredientsOnStation) {
                if (isPizzaIngredient(ing)) {
                    addToPizzaDish(plate, ing);
                } else {
                    addToRegularDish(plate, ing);
                }
            }
            ingredientsOnStation.clear();
            System.out.println("[ASSEMBLY] All ingredients added to chef's plate");
            return;
        }

        // Case 4: Both plate and ingredients on station, chef is empty - assemble and pick up
        if (plateOnStation != null && !ingredientsOnStation.isEmpty() && !chef.hasItem()) {
            assembleAllIngredients();
            chef.pickUp(plateOnStation);
            plateOnStation = null;
            System.out.println("[ASSEMBLY] Assembled dish picked up");
            return;
        }

        // Case 5: Only plate on station, no ingredients, chef is empty - pick up plate
        if (!chef.hasItem() && plateOnStation != null && ingredientsOnStation.isEmpty()) {
            chef.pickUp(plateOnStation);
            plateOnStation = null;
            System.out.println("[ASSEMBLY] Plate picked up from station");
            return;
        }

        // Case 6: Only ingredients on station, no plate, chef is empty - pick up first ingredient
        if (!chef.hasItem() && !ingredientsOnStation.isEmpty() && plateOnStation == null) {
            Ingredient ing = ingredientsOnStation.remove(0);
            chef.pickUp((Item) ing);
            System.out.println("[ASSEMBLY] Picked up " + ing.getName() + " from station");
            return;
        }

        // Case 7: Chef has plate that already has a dish, add ingredients to it
        if (chefItem instanceof Plate plate && plate.hasDish() && !ingredientsOnStation.isEmpty()) {
            for (Ingredient ing : ingredientsOnStation) {
                if (plate.getDish() instanceof PizzaDish pizza) {
                    pizza.addComponent(ing);
                } else {
                    plate.getDish().addComponent(ing);
                }
            }
            ingredientsOnStation.clear();
            System.out.println("[ASSEMBLY] Added ingredients to existing dish on plate");
            return;
        }

        System.out.println("[ASSEMBLY] No valid interaction available");
    }

    /**
     * Assemble all ingredients on station into the plate
     */
    private void assembleAllIngredients() {
        if (plateOnStation == null || ingredientsOnStation.isEmpty()) {
            return;
        }

        for (Ingredient ing : ingredientsOnStation) {
            if (isPizzaIngredient(ing)) {
                addToPizzaDish(plateOnStation, ing);
            } else {
                addToRegularDish(plateOnStation, ing);
            }
        }
        ingredientsOnStation.clear();
        System.out.println("[ASSEMBLY] All ingredients assembled into dish");
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
            System.out.println("[ASSEMBLY] Added " + ingredient.getName() + " to existing pizza");
        } else {
            List<Preparable> ingredients = new ArrayList<>();
            ingredients.add(ingredient);
            PizzaDish pizza = new PizzaDish("Unbaked Pizza", ingredients);
            plate.setDish(pizza);
            System.out.println("[ASSEMBLY] Created new pizza with " + ingredient.getName());
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

    public List<Ingredient> getIngredientsOnStation() {
        return new ArrayList<>(ingredientsOnStation);
    }

    public boolean hasPlate() {
        return plateOnStation != null;
    }

    public boolean hasIngredient() {
        return !ingredientsOnStation.isEmpty();
    }

    public int getIngredientCount() {
        return ingredientsOnStation.size();
    }
}