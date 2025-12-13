package models.command;

import models.player.ChefPlayer;
import controllers.Stage;

import java.util.List;

/**
 * Command Pattern - Concrete Command for Switching Active Chef
 * Allows switching between Chef 1 and Chef 2
 *
 * @author Nimonscooked Team
 * @version 1.0
 */
public class SwitchChefCommand implements ChefCommand {
    private final Stage stage;
    private ChefPlayer previousActiveChef;
    private ChefPlayer newActiveChef;
    private boolean wasExecuted;

    /**
     * Constructor
     *
     * @param stage The game stage containing chefs
     */
    public SwitchChefCommand(Stage stage) {
        this.stage = stage;
        this.wasExecuted = false;
    }

    @Override
    public boolean canExecute() {
        List<ChefPlayer> chefs = stage.getChefs();
        return chefs != null && chefs.size() >= 2;
    }

    @Override
    public boolean execute() {
        if (!canExecute()) {
            System.out.println("[CMD] Cannot switch - not enough chefs");
            return false;
        }

        previousActiveChef = stage.getActiveChef();

        stage.switchActiveChef();

        newActiveChef = stage.getActiveChef();

        wasExecuted = true;

        System.out.println("[CMD] Switched from " + previousActiveChef.getName() +
                " to " + newActiveChef.getName());
        return true;
    }

    @Override
    public void undo() {
        if (!wasExecuted) {
            return;
        }

        stage.switchActiveChef();

        wasExecuted = false;
        System.out.println("[CMD] Undone chef switch");
    }

    @Override
    public String getDescription() {
        if (newActiveChef != null) {
            return "Switched to " + newActiveChef.getName();
        }
        return "Switch active chef";
    }

    /**
     * Get the chef that was active before the switch
     */
    public ChefPlayer getPreviousActiveChef() {
        return previousActiveChef;
    }

    /**
     * Get the chef that became active after the switch
     */
    public ChefPlayer getNewActiveChef() {
        return newActiveChef;
    }
}