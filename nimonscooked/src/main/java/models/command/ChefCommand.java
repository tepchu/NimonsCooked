package models.command;

import models.player.ChefPlayer;
import models.map.GameMap;

public interface ChefCommand {

    default void undo() {
        // Default: do nothing
    }

    boolean execute();

    boolean canExecute();

    String getDescription();

    default String getType() {
        return this.getClass().getSimpleName();
    }
}