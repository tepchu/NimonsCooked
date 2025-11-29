package models.station;

import models.player.ChefPlayer;
import models.player.CurrentAction;
import models.item.Ingredient;
import models.item.Preparable;
import models.core.Position;
import models.enums.StationType;

public class CuttingStation extends Station {

    public static final int CUT_DURATION_SEC = 3;

    public CuttingStation(Position position) {
        super(StationType.CUTTING, position);
    }

    @Override
    public void interact(ChefPlayer chef) {
        if (!(chef.getInventory() instanceof Ingredient ing)) return;
        Preparable p = ing;
        if (!p.canBeChopped()) return;

        chef.startBusy(CurrentAction.CUTTING, CUT_DURATION_SEC, p::chop);
        // NOTE: Untuk progress yang bisa di-pause, simpan progress di station.
    }
}
