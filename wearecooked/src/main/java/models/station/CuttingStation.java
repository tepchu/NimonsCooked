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
    private int savedProgress;
    private long lastCutTime;
    private boolean isCutting;
    private Plate plateOnStation;
    private List<Ingredient> ingredientsOnStation;

    public CuttingStation(Position position) {
        super(StationType.CUTTING, position);
        this.ingredientBeingCut = null;
        this.savedProgress = 0;
        this.lastCutTime = 0;
        this.isCutting = false;
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

            // If there are ingredients already on station, add them to plate
            if (!ingredientsOnStation.isEmpty()) {
                assembleAllIngredients();
            }
            return;
        }

        // Case 2: Chef places ingredient on station (RAW goes to cutting, CHOPPED stacks)
        if (chefItem instanceof Ingredient ing) {
            // If it's CHOPPED, add to stack
            if (ing.getState() == IngredientState.CHOPPED) {
                // Check if this would mix with different state on plate
                if (plateOnStation != null && plateOnStation.hasDish()) {
                    IngredientState plateState = getDishIngredientState(plateOnStation.getDish());
                    if (plateState != null && plateState == IngredientState.RAW) {
                        System.out.println("[CUTTING] ✗ Cannot mix RAW and CHOPPED ingredients!");
                        return;
                    }
                }

                chef.drop();
                ingredientsOnStation.add(ing);
                System.out.println("[CUTTING] Added " + ing.getName() + " to station. Total ingredients: " + ingredientsOnStation.size());

                // Auto-assemble if plate is present
                if (plateOnStation != null) {
                    assembleAllIngredients();
                }
                return;
            }
        }

        // NEW: Case 2.5: Chef picks up ONLY RAW ingredients from plate
        if (!chef.hasItem() && plateOnStation != null && plateOnStation.hasDish()) {
            models.item.Dish dish = plateOnStation.getDish();
            List<Preparable> components = dish.getComponents();

            // ONLY pick up RAW ingredients (CHOPPED are immovable)
            for (Preparable p : components) {
                if (p instanceof Ingredient ing && ing.getState() == IngredientState.RAW) {
                    dish.getComponents().remove(p);
                    chef.pickUp(ing);
                    System.out.println("[CUTTING] ✓ Removed RAW " + ing.getName() + " from plate");

                    // If dish is now empty, remove it from plate
                    if (dish.getComponents().isEmpty()) {
                        plateOnStation.setDish(null);
                    }
                    return;
                }
            }

            // If no RAW ingredients, cannot pick up
            System.out.println("[CUTTING] CHOPPED ingredients cannot be removed from plate");
        }

        // Case 3: Chef picks up assembled plate
        if (!chef.hasItem() && plateOnStation != null && ingredientsOnStation.isEmpty()) {
            // CHANGED: Cannot pick up if it has RAW ingredients
            if (plateOnStation.hasDish() && hasRawIngredients(plateOnStation.getDish())) {
                System.out.println("[CUTTING] ✗ Plate has RAW ingredients! Remove them first (press X).");
                return;
            }

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

        // Case 5: Chef places RAW ingredient to start/continue cutting
        if (chefItem instanceof Ingredient ing && ing.canBeChopped() && ing.getState() == IngredientState.RAW) {
            if (ingredientBeingCut == ing && savedProgress > 0) {
                // Continue cutting same ingredient
                continueCutting(chef, ing);
            } else if (ingredientBeingCut == null) {
                // Start new cutting - DROP ingredient on station
                chef.drop();
                ingredientBeingCut = ing;
                startCutting(chef, ing);
            } else {
                System.out.println("[CUTTING] Another ingredient is being cut");
            }
            return;
        }

        // Case 6: Empty-handed chef resumes cutting ingredient on station
        if (!chef.hasItem() && ingredientBeingCut != null && !chef.isBusy()) {
            if (savedProgress > 0) {
                continueCutting(chef, ingredientBeingCut);
            } else {
                startCutting(chef, ingredientBeingCut);
            }
            return;
        }

        // Case 7: Pick up unfinished ingredient (cancel cutting)
        if (!chef.hasItem() && ingredientBeingCut != null && !isCutting) {
            chef.pickUp(ingredientBeingCut);
            ingredientBeingCut = null;
            savedProgress = 0;
            System.out.println("[CUTTING] Picked up unfinished ingredient");
            return;
        }

        System.out.println("[CUTTING] No valid interaction available");
    }

    /**
     * NEW: Check if dish has any RAW ingredients
     */
    private boolean hasRawIngredients(models.item.Dish dish) {
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
    private IngredientState getDishIngredientState(models.item.Dish dish) {
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
     * Start cutting new ingredient
     */
    private void startCutting(ChefPlayer chef, Ingredient ing) {
        isCutting = true;
        savedProgress = 0;
        lastCutTime = System.currentTimeMillis();

        System.out.println("[CUTTING] Starting cut: " + CUT_DURATION_SEC + "s (ingredient on station)");

        chef.startBusy(CurrentAction.CUTTING, CUT_DURATION_SEC, () -> {
            ing.chop();
            ingredientsOnStation.add(ing);
            ingredientBeingCut = null;
            savedProgress = 0;
            isCutting = false;
            System.out.println("[CUTTING] ✓ Cutting complete!");
        });
    }


    /**
     * Continue cutting with saved progress
     */
    private void continueCutting(ChefPlayer chef, Ingredient ing) {
        isCutting = true;
        int remainingTime = CUT_DURATION_SEC - (savedProgress / 1000);
        lastCutTime = System.currentTimeMillis();

        System.out.println("[CUTTING] Continuing cut: " + remainingTime + "s remaining");

        chef.startBusy(CurrentAction.CUTTING, remainingTime, () -> {
            ing.chop();
            ingredientsOnStation.add(ing);
            ingredientBeingCut = null;
            savedProgress = 0;
            isCutting = false;
            System.out.println("[CUTTING] ✓ Cutting complete!");
        });
    }

    /**
     * Save progress when chef leaves or stops cutting
     * This is called by Stage.update()
     */
    public void saveProgress(ChefPlayer chef) {
        if (!isCutting || ingredientBeingCut == null) return;

        // Update elapsed time
        long elapsed = System.currentTimeMillis() - lastCutTime;
        savedProgress += (int) elapsed;
        savedProgress = Math.min(savedProgress, CUT_DURATION_SEC * 1000);
        lastCutTime = System.currentTimeMillis();

        // If chef walked away (not busy anymore), clear cutting flag
        if (!chef.isBusy() || chef.getCurrentAction() != CurrentAction.CUTTING) {
            isCutting = false;
            System.out.println("[CUTTING] Progress saved: " + (savedProgress / 1000) + "s");
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