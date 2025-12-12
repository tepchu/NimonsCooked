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
    private ChefPlayer chefCutting;
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
        if (chefItem instanceof Ingredient ing && ing.canBeChopped() && ing.getState() == IngredientState.RAW) {
            // Check if this is the same ingredient that was being cut
            if (ingredientBeingCut == ing && savedProgress > 0) {
                // Continue cutting the same ingredient
                continueCutting(chef, ing);
            } else if (ingredientBeingCut == null) {
                // Start new cutting
                chef.drop(); // DROP THE ITEM - stays on station
                ingredientBeingCut = ing;
                startCutting(chef, ing);
            } else {
                System.out.println("[CUTTING] Station is busy with another ingredient");
            }
            return;
        }


        // Case 6: Pick up previously placed ingredient for cutting
        if (!chef.hasItem() && ingredientBeingCut != null) {
            // Stop any cutting in progress
            if (chefCutting != null && chefCutting.isBusy() && chefCutting.getCurrentAction() == CurrentAction.CUTTING) {
                // Save elapsed progress before interrupting
                long elapsed = System.currentTimeMillis() - lastCutTime;
                savedProgress += (int) elapsed;
                savedProgress = Math.min(savedProgress, CUT_DURATION_SEC * 1000);

                chefCutting.interruptBusy();
            }

            chef.pickUp(ingredientBeingCut);
            ingredientBeingCut = null;
            chefCutting = null;
            System.out.println("[CUTTING] Picked up unfinished ingredient (progress: " + (savedProgress / 1000) + "s saved)");
            return;
        }

        // Case 7: Empty-handed chef interacts with ingredient on station to resume cutting
        if (!chef.hasItem() && ingredientBeingCut != null && !chef.isBusy()) {
            // Resume cutting without picking up
            continueCutting(chef, ingredientBeingCut);
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
            ingredientsOnStation.add(ing); // Move to finished ingredients
            ingredientBeingCut = null;
            chefCutting = null;
            savedProgress = 0;
            System.out.println("[CUTTING] ✓ Cutting complete! Ingredient moved to finished stack.");
        });
    }

    /**
     * Continue cutting with saved progress
     */
    private void continueCutting(ChefPlayer chef, Ingredient ing) {
        chefCutting = chef;
        int remainingTime = CUT_DURATION_SEC - (savedProgress / 1000);
        lastCutTime = System.currentTimeMillis();

        System.out.println("[CUTTING] Continuing cut: " + remainingTime + "s remaining (saved: " + savedProgress / 1000 + "s)");

        chef.startBusy(CurrentAction.CUTTING, remainingTime, () -> {
            ing.chop();
            ingredientsOnStation.add(ing); // Move to finished ingredients
            ingredientBeingCut = null;
            chefCutting = null;
            savedProgress = 0;
            System.out.println("[CUTTING] ✓ Cutting complete! Ingredient moved to finished stack.");
        });
    }

    /**
     * Save progress when chef leaves or stops cutting
     * This is called by Stage.update()
     */
    public void saveProgress(ChefPlayer chef) {
        if (ingredientBeingCut == null || chefCutting != chef) return;

        if (chef.isBusy() && chef.getCurrentAction() == CurrentAction.CUTTING) {
            // Update progress
            long elapsed = System.currentTimeMillis() - lastCutTime;
            savedProgress += (int) elapsed;
            savedProgress = Math.min(savedProgress, CUT_DURATION_SEC * 1000);
            lastCutTime = System.currentTimeMillis();
        } else if (!chef.isBusy() && savedProgress > 0) {
            // Chef walked away - progress saved
            System.out.println("[CUTTING] Progress saved: " + (savedProgress / 1000) + "s / " + CUT_DURATION_SEC + "s");
            chefCutting = null; // Clear chef reference so anyone can continue
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
