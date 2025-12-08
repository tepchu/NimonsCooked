package models.station;

import models.player.ChefPlayer;
import models.player.CurrentAction;
import models.item.kitchenutensils.Plate;
import models.core.Position;
import models.enums.StationType;

public class WashingStation extends Station {

    public static final int WASH_DURATION_SEC = 3;

    private Plate dirtyStackTop; // simpel: anggap cuma satu stack posisi ini
    private Plate cleanStackTop;

    public WashingStation(Position position) {
        super(StationType.WASHING, position);
    }

    @Override
    public void interact(ChefPlayer chef) {
        // letakkan piring kotor ke stack dirty
        if (chef.getInventory() instanceof Plate p && !p.isClean()) {
            // stack sederhana: tumpuk di atas
            p.setDish(null);
            p.markDirty();
            dirtyStackTop = p;
            chef.drop();
        }
        // cuci 1 plate kotor dari dirty stack
        else if (!chef.isBusy() && dirtyStackTop != null) {
            Plate toWash = dirtyStackTop;

            chef.startBusy(CurrentAction.WASHING, WASH_DURATION_SEC, () -> {
                toWash.setClean(true);
                cleanStackTop = toWash;
                dirtyStackTop = null; // versi simple
            });
        }
        // ambil plate bersih dari clean stack
        else if (chef.getInventory() == null && cleanStackTop != null) {
            chef.pickUp(cleanStackTop);
            cleanStackTop = null;
        }
    }
}
