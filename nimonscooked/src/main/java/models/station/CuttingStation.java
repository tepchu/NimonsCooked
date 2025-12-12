package models.station;

import models.player.ChefPlayer;
import models.player.CurrentAction;
import models.item.Ingredient;
import models.item.Preparable;
import models.core.Position;
import models.enums.StationType;
import models.enums.IngredientState;
import models.item.Item;
import models.item.kitchenutensils.Plate;

import java.util.ArrayList;
import java.util.List;

public class CuttingStation extends Station {

    public static final int CUT_DURATION_SEC = 3;

    private Ingredient ingredientBeingCut;
    private int savedProgress; // Progress in milliseconds
    private long lastCutTime;

    private Plate plateOnStation;
    private List<Ingredient> ingredientsOnStation;

    public CuttingStation(Position position) {
        super(StationType.CUTTING, position);
        this.ingredientBeingCut = null;
        this.savedProgress = 0;
        this.lastCutTime = 0;
        this.plateOnStation = null;
        this.ingredientsOnStation = new ArrayList<>();
    }

    public void interact(ChefPlayer chef) {
        Item chefItem = chef.getInventory();

        // ========== ASSEMBLY FUNCTIONS (like Assembly Station) ==========

        // Case 1: Chef places a clean plate on empty station
        if (chefItem instanceof Plate plate && plate.isClean() && plateOnStation == null) {
            chef.drop();
            plateOnStation = plate;
            System.out.println("[CUTTING] Plate placed on station");
            return;
        }

        // Case 2: Chef places a chopped ingredient on station (can stack multiple)
        if (chefItem instanceof Ingredient ing && ing.getState() == IngredientState.CHOPPED) {
            chef.drop();
            ingredientsOnStation.add(ing);
            System.out.println("[CUTTING] Added " + ing.getName() + " to station. Total ingredients: " + ingredientsOnStation.size());

            // Auto-assemble if plate is present
            if (plateOnStation != null) {
                assembleAllIngredients();
            }
            return;
        }

        // Case 3: Chef picks up assembled plate
        if (!chef.hasItem() && plateOnStation != null && ingredientsOnStation.isEmpty()) {
            chef.pickUp(plateOnStation);
            plateOnStation = null;
            System.out.println("[CUTTING] Plate picked up from station");
            return;
        }

        // Case 4: Chef picks up ingredient from station
        if (!chef.hasItem() && !ingredientsOnStation.isEmpty() && plateOnStation == null) {
            Ingredient ing = ingredientsOnStation.remove(0);
            chef.pickUp(ing);
            System.out.println("[CUTTING] Picked up " + ing.getName() + " from station");
            return;
        }

        // ========== CUTTING FUNCTIONS ==========

        // Case 5: Start or continue cutting
        // Case 5: Start or continue cutting
        if (chefItem instanceof Ingredient ing && ing.canBeChopped()) {
            // Check if continuing same ingredient
            if (ingredientBeingCut == ing && savedProgress > 0) {
                continueCutting(chef, ing);
            } else {
                startCutting(chef, ing);
            }
            return;
        }

        // Case 6: Pick up previously placed ingredient for cutting
        if (!chef.hasItem() && ingredientBeingCut != null) {
            chef.pickUp(ingredientBeingCut);
            ingredientBeingCut = null;
            savedProgress = 0;
            System.out.println("[CUTTING] Picked up unfinished ingredient");
            return;
        }

        System.out.println("[CUTTING] No valid interaction available");
    }

    /**
     * Start cutting new ingredient
     */
    private void startCutting(ChefPlayer chef, Ingredient ing) {
        ingredientBeingCut = ing;
        savedProgress = 0;
        lastCutTime = System.currentTimeMillis();

        System.out.println("[CUTTING] Starting new cut: " + CUT_DURATION_SEC + "s");

        chef.startBusy(CurrentAction.CUTTING, CUT_DURATION_SEC, () -> {
            ing.chop();
            ingredientBeingCut = null;
            savedProgress = 0;
            System.out.println("[CUTTING] ✓ Cutting complete!");
        });
    }

    /**
     * Continue cutting with saved progress
     */
    private void continueCutting(ChefPlayer chef, Ingredient ing) {
        int remainingTime = CUT_DURATION_SEC - (savedProgress / 1000);
        lastCutTime = System.currentTimeMillis();

        System.out.println("[CUTTING] Continuing cut: " + remainingTime + "s remaining (saved: " + savedProgress / 1000 + "s)");

        chef.startBusy(CurrentAction.CUTTING, remainingTime, () -> {
            ing.chop();
            ingredientBeingCut = null;
            savedProgress = 0;
            System.out.println("[CUTTING] ✓ Cutting complete!");
        });
    }

    /**
     * Save progress when chef leaves or stops cutting
     * This is called by Stage.update()
     */
    public void saveProgress(ChefPlayer chef) {
        if (ingredientBeingCut == null) return;
        if (chef.getInventory() != ingredientBeingCut) return;

        if (chef.isBusy() && chef.getCurrentAction() == CurrentAction.CUTTING) {
            // Chef is actively cutting - update elapsed time
            long elapsed = System.currentTimeMillis() - lastCutTime;
            savedProgress += (int) elapsed;
            savedProgress = Math.min(savedProgress, CUT_DURATION_SEC * 1000);
            lastCutTime = System.currentTimeMillis();
        } else if (!chef.isBusy() && savedProgress > 0) {
            // Chef stopped cutting - progress is already saved
            System.out.println("[CUTTING] Progress saved: " + (savedProgress / 1000) + "s / " + CUT_DURATION_SEC + "s");
        }
    }

    /**
     * Assemble all ingredients on station into the plate
     */
    private void assembleAllIngredients() {
        if (plateOnStation == null || ingredientsOnStation.isEmpty()) {
            return;
        }

        for (Ingredient ing : ingredientsOnStation) {
            addToDish(plateOnStation, ing);
        }
        ingredientsOnStation.clear();
        System.out.println("[CUTTING] All ingredients assembled into dish");
    }

    private void addToDish(Plate plate, Ingredient ingredient) {
        if (plate.getDish() != null) {
            plate.getDish().addComponent(ingredient);
        } else {
            models.item.Dish dish = new models.item.Dish("Mixed Dish");
            dish.addComponent(ingredient);
            plate.setDish(dish);
        }
    }

    // Getters for progress tracking
    public Ingredient getIngredientBeingCut() {
        return ingredientBeingCut;
    }

    public int getSavedProgress() {
        return savedProgress;
    }

    public double getCutProgressPercent() {
        if (ingredientBeingCut == null) return 0.0;
        return (double) savedProgress / (CUT_DURATION_SEC * 1000);
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
}
