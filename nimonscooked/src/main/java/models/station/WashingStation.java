package models.station;

import models.player.ChefPlayer;
import models.player.CurrentAction;
import models.item.kitchenutensils.Plate;
import models.core.Position;
import models.enums.StationType;
import models.item.*;

import java.util.Stack;

public class WashingStation extends Station {

    public static final int WASH_DURATION_SEC = 3;

    private Position washPosition;      // Left/first block - for washing
    private Position cleanPosition;     // Right/second block - for clean plates

    private Plate dirtyPlateBeingWashed;
    private Stack<Plate> cleanPlatesStack; // Stack of clean plates at clean position

    private int savedProgress; // Progress in milliseconds
    private long lastWashTime;

    private ChefPlayer chefWashing;

    public WashingStation(Position washPos, Position cleanPos) {
        super(StationType.WASHING, washPos); // Main position is wash position
        this.washPosition = washPos;
        this.cleanPosition = cleanPos;
        this.cleanPlatesStack = new Stack<>();
        this.savedProgress = 0;
        this.lastWashTime = 0;
    }

    // Keep old constructor for backwards compatibility
    public WashingStation(Position position) {
        this(position, new Position(position.getX() + 1, position.getY()));
    }

    @Override
    public void interact(ChefPlayer chef) {
        Position chefPos = chef.getPosition();
        Item chefItem = chef.getInventory();

        // Determine which side chef is interacting with
        boolean atWashSide = isAdjacentTo(chefPos, washPosition);
        boolean atCleanSide = isAdjacentTo(chefPos, cleanPosition);

        if (atWashSide) {
            handleWashSideInteraction(chef, chefItem);
        } else if (atCleanSide) {
            handleCleanSideInteraction(chef, chefItem);
        } else {
            System.out.println("[WASHING] Not adjacent to washing station");
        }
    }

    private void handleWashSideInteraction(ChefPlayer chef, Item chefItem) {
        // Case 1: Chef places dirty plate to wash
        if (chefItem instanceof Plate plate && !plate.isClean() && dirtyPlateBeingWashed == null) {
            chef.drop(); // DROP - plate stays on station
            dirtyPlateBeingWashed = plate;
            dirtyPlateBeingWashed.setDish(null);
            savedProgress = 0;
            System.out.println("[WASHING] Dirty plate placed for washing");
            return;
        }

        // Case 2: Chef starts/continues washing (must be at wash side, empty handed)
        if (!chef.hasItem() && dirtyPlateBeingWashed != null) {
            if (chef.isBusy()) {
                System.out.println("[WASHING] Chef is busy with other action");
                return;
            }

            if (savedProgress > 0) {
                continueWashing(chef);
            } else {
                startWashing(chef);
            }
            return;
        }

        // Case 3: Chef picks up unfinished dirty plate
        if (!chef.hasItem() && dirtyPlateBeingWashed != null && !chef.isBusy()) {
            chef.pickUp(dirtyPlateBeingWashed);
            dirtyPlateBeingWashed = null;
            System.out.println("[WASHING] Picked up unfinished dirty plate");
            return;
        }

        System.out.println("[WASHING] No valid wash side interaction");
    }

    private void handleCleanSideInteraction(ChefPlayer chef, Item chefItem) {
        // Case 1: Chef picks up clean plate from stack
        if (!chef.hasItem() && !cleanPlatesStack.isEmpty()) {
            Plate cleanPlate = cleanPlatesStack.pop();
            chef.pickUp(cleanPlate);
            System.out.println("[WASHING] Picked up clean plate. Remaining: " + cleanPlatesStack.size());
            return;
        }

        System.out.println("[WASHING] No clean plates available");
    }

    /**
     * Start washing new plate
     */
    private void startWashing(ChefPlayer chef) {
        chefWashing = chef;
        savedProgress = 0;
        lastWashTime = System.currentTimeMillis();

        System.out.println("[WASHING] Starting wash: " + WASH_DURATION_SEC + "s");

        chef.startBusy(CurrentAction.WASHING, WASH_DURATION_SEC, () -> {
            dirtyPlateBeingWashed.setClean(true);
            cleanPlatesStack.push(dirtyPlateBeingWashed);
            dirtyPlateBeingWashed = null;
            chefWashing = null;
            savedProgress = 0;
            System.out.println("[WASHING] ✓ Plate cleaned and moved to clean side!");
        });
    }

    /**
     * Continue washing with saved progress
     */
    private void continueWashing(ChefPlayer chef) {
        int remainingTime = WASH_DURATION_SEC - (savedProgress / 1000);
        lastWashTime = System.currentTimeMillis();

        System.out.println("[WASHING] Continuing wash: " + remainingTime + "s remaining (saved: " + savedProgress / 1000 + "s)");

        chef.startBusy(CurrentAction.WASHING, remainingTime, () -> {
            dirtyPlateBeingWashed.setClean(true);
            cleanPlatesStack.push(dirtyPlateBeingWashed);
            dirtyPlateBeingWashed = null;
            chefWashing = null;
            savedProgress = 0;
            System.out.println("[WASHING] ✓ Plate cleaned and moved to clean side!");
        });
    }

    private boolean isAdjacentTo(Position chefPos, Position targetPos) {
        int dx = Math.abs(chefPos.getX() - targetPos.getX());
        int dy = Math.abs(chefPos.getY() - targetPos.getY());
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }

    /**
     * Save progress when chef leaves or stops washing
     * This is called by Stage.update()
     */
    public void saveProgress(ChefPlayer chef) {
        if (dirtyPlateBeingWashed == null) return;

        if (chef.isBusy() && chef.getCurrentAction() == CurrentAction.WASHING) {
            // Chef is actively washing - update elapsed time
            long elapsed = System.currentTimeMillis() - lastWashTime;
            savedProgress += (int) elapsed;
            savedProgress = Math.min(savedProgress, WASH_DURATION_SEC * 1000);
            lastWashTime = System.currentTimeMillis();
        } else if (!chef.isBusy() && savedProgress > 0) {
            // Chef stopped washing - progress is already saved
            System.out.println("[WASHING] Progress saved: " + (savedProgress / 1000) + "s / " + WASH_DURATION_SEC + "s");
            chefWashing = null;
        }
    }

    // Getters
    public Position getWashPosition() {
        return washPosition;
    }

    public Position getCleanPosition() {
        return cleanPosition;
    }

    public Plate getDirtyPlateBeingWashed() {
        return dirtyPlateBeingWashed;
    }

    public Stack<Plate> getCleanPlatesStack() {
        return cleanPlatesStack;
    }

    public int getCleanPlateCount() {
        return cleanPlatesStack.size();
    }

    public int getSavedProgress() {
        return savedProgress;
    }

    public double getWashProgressPercent() {
        if (dirtyPlateBeingWashed == null) return 0.0;
        return (double) savedProgress / (WASH_DURATION_SEC * 1000);
    }

    public boolean hasDirtyPlate() {
        return dirtyPlateBeingWashed != null;
    }

    public boolean hasCleanPlates() {
        return !cleanPlatesStack.isEmpty();
    }
}
