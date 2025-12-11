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
            System.out.println("[INGREDIENT_STORAGE] ✓ Plate placed on station");
            return;
        }

        // 2. Chef taruh chopped ingredient ke station (bisa stack)
        if (chefItem instanceof Ingredient ing && ing.getState() == IngredientState.CHOPPED) {
            chef.drop();
            ingredientsOnStation.add(ing);
            System.out.println("[INGREDIENT_STORAGE] ✓ Added " + ing.getName() + " to station. Total: " + ingredientsOnStation.size());

            // Auto-assemble jika ada plate
            if (plateOnStation != null) {
                assembleAllIngredients();
            }
            return;
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
