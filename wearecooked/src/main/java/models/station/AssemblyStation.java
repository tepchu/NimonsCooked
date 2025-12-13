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
    private List<Ingredient> ingredientsOnStation;

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
            if (!ingredientsOnStation.isEmpty()) {
                if (canAssembleIngredients()) {
                    assembleAllIngredients();
                } else {
                    System.out.println("[ASSEMBLY] ✗ Cannot mix RAW and CHOPPED ingredients!");
                }
            }
            return;
        }

        // Case 2: Chef places ingredient on station (RAW or CHOPPED)
        if (chefItem instanceof Ingredient ing &&
                (ing.getState() == IngredientState.RAW || ing.getState() == IngredientState.CHOPPED)) {

            // Check if adding this ingredient would mix states
            if (!ingredientsOnStation.isEmpty()) {
                IngredientState existingState = ingredientsOnStation.get(0).getState();
                if (existingState != ing.getState()) {
                    System.out.println("[ASSEMBLY] ✗ Cannot mix RAW and CHOPPED ingredients!");
                    System.out.println("[ASSEMBLY] Station has " + existingState + " ingredients");
                    return;
                }
            }

            // Check if plate has dish with different state ingredients
            if (plateOnStation != null && plateOnStation.hasDish()) {
                IngredientState plateState = getDishIngredientState(plateOnStation.getDish());
                if (plateState != null && plateState != ing.getState()) {
                    System.out.println("[ASSEMBLY] ✗ Cannot mix RAW and CHOPPED ingredients!");
                    System.out.println("[ASSEMBLY] Plate has " + plateState + " ingredients");
                    return;
                }
            }

            chef.drop();
            ingredientsOnStation.add(ing);
            System.out.println("[ASSEMBLY] Added " + ing.getName() + " (" + ing.getState() + ") to station. Total: " + ingredientsOnStation.size());

            // Auto-assemble if plate is present and states match
            if (plateOnStation != null && canAssembleIngredients()) {
                assembleAllIngredients();
            }
            return;
        }

        // NEW: Case 2.5: Chef picks up ONLY RAW ingredients from plate's dish
        if (!chef.hasItem() && plateOnStation != null && plateOnStation.hasDish()) {
            Dish dish = plateOnStation.getDish();
            List<Preparable> components = dish.getComponents();

            // ONLY pick up RAW ingredients (CHOPPED are immovable)
            for (Preparable p : components) {
                if (p instanceof Ingredient ing && ing.getState() == IngredientState.RAW) {
                    dish.getComponents().remove(p);
                    chef.pickUp(ing);
                    System.out.println("[ASSEMBLY] ✓ Removed RAW " + ing.getName() + " from plate");

                    // If dish is now empty, remove it from plate
                    if (dish.getComponents().isEmpty()) {
                        plateOnStation.setDish(null);
                    }
                    return;
                }
            }

            // If no RAW ingredients, cannot pick up anything
            System.out.println("[ASSEMBLY] CHOPPED ingredients cannot be removed from plate");
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

        // Case 4: Both plate and ingredients on station - pick up assembled plate
        if (plateOnStation != null && !chef.hasItem()) {
            // Check if plate has dish (already assembled)
            if (plateOnStation.hasDish()) {
                // CHANGED: Cannot pick up if it has RAW ingredients
                if (hasRawIngredients(plateOnStation.getDish())) {
                    System.out.println("[ASSEMBLY] ✗ Plate has RAW ingredients! Remove them first (press X).");
                    return;
                }

                // All CHOPPED, can pick up
                chef.pickUp(plateOnStation);
                plateOnStation = null;
                System.out.println("[ASSEMBLY] Picked up assembled dish");
            } else if (ingredientsOnStation.isEmpty()) {
                // Empty plate, no ingredients
                chef.pickUp(plateOnStation);
                plateOnStation = null;
                System.out.println("[ASSEMBLY] Picked up empty plate");
            } else {
                // Plate without dish but ingredients on station - assemble first
                if (canAssembleIngredients()) {
                    assembleAllIngredients();

                    // Check again if we can pick up after assembly
                    if (hasRawIngredients(plateOnStation.getDish())) {
                        System.out.println("[ASSEMBLY] ✗ Assembled plate has RAW ingredients! Remove them first.");
                        return;
                    }

                    chef.pickUp(plateOnStation);
                    plateOnStation = null;
                    System.out.println("[ASSEMBLY] Assembled and picked up dish");
                } else {
                    System.out.println("[ASSEMBLY] ✗ Cannot assemble - ingredients on station and plate have different states!");
                }
            }
            return;
        }

        // Case 5: Only ingredients on station, no plate, chef is empty - pick up ingredient
        if (!chef.hasItem() && !ingredientsOnStation.isEmpty() && (plateOnStation == null)) {
            Ingredient ing = ingredientsOnStation.remove(0);
            chef.pickUp((Item) ing);
            System.out.println("[ASSEMBLY] Picked up " + ing.getName() + " from station");
            return;
        }

        // Case 6: Chef has plate that already has a dish, add ingredients to it
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
     * NEW: Check if dish has any RAW ingredients
     */
    private boolean hasRawIngredients(Dish dish) {
        for (Preparable p : dish.getComponents()) {
            if (p instanceof Ingredient ing && ing.getState() == IngredientState.RAW) {
                return true;
            }
        }
        return false;
    }

    /**
     * NEW: Get the ingredient state of a dish (returns null if empty or mixed)
     */
    private IngredientState getDishIngredientState(Dish dish) {
        IngredientState state = null;
        for (Preparable p : dish.getComponents()) {
            if (p instanceof Ingredient ing) {
                if (state == null) {
                    state = ing.getState();
                } else if (state != ing.getState()) {
                    return null; // Mixed states
                }
            }
        }
        return state;
    }

    /**
     * NEW: Check if ingredients on station can be assembled with plate
     */
    private boolean canAssembleIngredients() {
        if (ingredientsOnStation.isEmpty()) {
            return true;
        }

        if (plateOnStation == null || !plateOnStation.hasDish()) {
            return true; // Can create new dish
        }

        // Check if states match
        IngredientState stationState = ingredientsOnStation.get(0).getState();
        IngredientState plateState = getDishIngredientState(plateOnStation.getDish());

        return plateState == null || plateState == stationState;
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