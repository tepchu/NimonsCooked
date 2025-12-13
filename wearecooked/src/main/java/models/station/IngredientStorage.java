package models.station;

import models.enums.IngredientState;
import models.item.*;
import models.item.kitchenutensils.Plate;
import models.player.ChefPlayer;
import models.factory.IngredientFactory;
import models.enums.IngredientType;
import models.core.Position;
import models.enums.StationType;

import java.util.ArrayList;
import java.util.List;

public class IngredientStorage extends Station {

    private final IngredientType type;

    private Plate plateOnStation;
    private List<Ingredient> ingredientsOnStation;

    public IngredientStorage(Position pos, IngredientType type) {
        super(StationType.INGREDIENT_STORAGE, pos);
        this.type = type;
        this.plateOnStation = null;
        this.ingredientsOnStation = new ArrayList<>();
    }

    @Override
    public void interact(ChefPlayer chef) {
        Item chefItem = chef.getInventory();

        // ========== ASSEMBLY FUNCTIONS (SESUAI SPESIFIKASI) ==========

        // 1. Chef taruh plate bersih ke station
        if (chefItem instanceof Plate plate && plate.isClean() && plateOnStation == null) {
            chef.drop();
            plateOnStation = plate;
            System.out.println("[STATION] Plate placed on station");

            // If there are ingredients already on station, add them to plate
            if (!ingredientsOnStation.isEmpty()) {
                assembleAllIngredients();
            }
            return;
        }

        // 2. Chef places ingredient on station (RAW or CHOPPED, but cannot mix)
        if (chefItem instanceof Ingredient ing &&
                (ing.getState() == IngredientState.RAW || ing.getState() == IngredientState.CHOPPED)) {

            // Check if adding this ingredient would mix states
            if (!ingredientsOnStation.isEmpty()) {
                IngredientState existingState = ingredientsOnStation.get(0).getState();
                if (existingState != ing.getState()) {
                    System.out.println("[INGREDIENT_STORAGE] ✗ Cannot mix RAW and CHOPPED ingredients!");
                    return;
                }
            }

            // Check if plate has dish with different state ingredients
            if (plateOnStation != null && plateOnStation.hasDish()) {
                IngredientState plateState = getDishIngredientState(plateOnStation.getDish());
                if (plateState != null && plateState != ing.getState()) {
                    System.out.println("[INGREDIENT_STORAGE] ✗ Cannot mix RAW and CHOPPED ingredients!");
                    return;
                }
            }

            chef.drop();
            ingredientsOnStation.add(ing);
            System.out.println("[INGREDIENT_STORAGE] ✓ Added " + ing.getName() + " (" + ing.getState() + ") to station. Total: " + ingredientsOnStation.size());

            // Auto-assemble if plate exists
            if (plateOnStation != null) {
                assembleAllIngredients();
            }
            return;
        }

        // NEW: 2.5 Chef picks up ONLY RAW ingredients from plate
        if (!chef.hasItem() && plateOnStation != null && plateOnStation.hasDish()) {
            Dish dish = plateOnStation.getDish();
            List<Preparable> components = dish.getComponents();

            // ONLY pick up RAW ingredients (CHOPPED are immovable)
            for (Preparable p : components) {
                if (p instanceof Ingredient ing && ing.getState() == IngredientState.RAW) {
                    dish.getComponents().remove(p);
                    chef.pickUp(ing);
                    System.out.println("[INGREDIENT_STORAGE] ✓ Removed RAW " + ing.getName() + " from plate");

                    // If dish is now empty, remove it from plate
                    if (dish.getComponents().isEmpty()) {
                        plateOnStation.setDish(null);
                    }
                    return;
                }
            }

            // If no RAW ingredients, cannot pick up
            System.out.println("[INGREDIENT_STORAGE] CHOPPED ingredients cannot be removed from plate");
        }

        // 3. Chef punya plate dengan dish, mau tambah ingredients dari station
        if (chefItem instanceof Plate plate && plate.isClean() && !ingredientsOnStation.isEmpty()) {
            for (Ingredient ing : ingredientsOnStation) {
                if (isPizzaIngredient(ing)) {
                    addToPizzaDish(plate, ing);
                } else {
                    addToRegularDish(plate, ing);
                }
            }
            ingredientsOnStation.clear();
            System.out.println("[INGREDIENT_STORAGE] ✓ All ingredients added to chef's plate");
            return;
        }

        // 4. Plate + ingredients di station, chef kosong → assemble & pickup
        if (plateOnStation != null && !ingredientsOnStation.isEmpty() && !chef.hasItem()) {
            assembleAllIngredients();
            chef.pickUp(plateOnStation);
            plateOnStation = null;
            System.out.println("[INGREDIENT_STORAGE] ✓ Assembled dish picked up");
            return;
        }

        // 5. Cuma plate di station, chef kosong → pickup plate
        if (!chef.hasItem() && plateOnStation != null && ingredientsOnStation.isEmpty()) {
            chef.pickUp(plateOnStation);
            plateOnStation = null;
            System.out.println("[INGREDIENT_STORAGE] ✓ Plate picked up from station");
            return;
        }

        // 6. Cuma ingredients di station, chef kosong → pickup ingredient pertama
        if (!chef.hasItem() && !ingredientsOnStation.isEmpty() && plateOnStation == null) {
            Ingredient ing = ingredientsOnStation.remove(0);
            chef.pickUp(ing);
            System.out.println("[INGREDIENT_STORAGE] ✓ Picked up " + ing.getName() + " from station");
            return;
        }

        // 7. Chef punya plate dengan dish yang sudah ada, tambah ingredients
        if (chefItem instanceof Plate plate && plate.hasDish() && !ingredientsOnStation.isEmpty()) {
            for (Ingredient ing : ingredientsOnStation) {
                if (plate.getDish() instanceof PizzaDish pizza) {
                    pizza.addComponent(ing);
                } else {
                    plate.getDish().addComponent(ing);
                }
            }
            ingredientsOnStation.clear();
            System.out.println("[INGREDIENT_STORAGE] ✓ Added ingredients to existing dish");
            return;
        }

        // ========== PRIMARY FUNCTION: INGREDIENT STORAGE ==========

        // 8. Chef ambil ingredient baru dari storage (unlimited stock)
        if (!chef.hasItem()) {
            Ingredient newIngredient = IngredientFactory.createIngredient(type);
            chef.pickUp(newIngredient);
            System.out.println("[INGREDIENT_STORAGE] ✓ Picked up fresh " + type.name() + " (unlimited stock)");
            return;
        }

        System.out.println("[INGREDIENT_STORAGE] ✗ No valid interaction");
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
        System.out.println("[INGREDIENT_STORAGE] ✓ All ingredients assembled into dish");
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

    public IngredientType getIngredientType() {
        return type;
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
