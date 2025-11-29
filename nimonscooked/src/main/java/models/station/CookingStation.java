package models.station;

import models.player.ChefPlayer;
import models.item.kitchenutensils.CookingDevice;
import models.core.Position;
import models.enums.StationType;

public class CookingStation extends Station {

    private CookingDevice device;  // pot/pan/oven yang lagi di atas station

    public CookingStation(Position position) {
        super(StationType.COOKING, position);
    }

    public void placeDevice(CookingDevice device) {
        this.device = device;
    }

    public CookingDevice getDevice() {
        return device;
    }

    @Override
    public void interact(ChefPlayer chef) {
        // kalau chef bawa device, taruh di station
        if (chef.getInventory() instanceof CookingDevice d) {
            chef.drop();
            this.device = d;
        }
        // kalau device sudah ada, bisa diambil
        else if (device != null && !chef.hasItem()) {
            chef.pickUp((models.item.Item) device);
            device = null;
        }
        // proses masak otomatis sebaiknya dipicu dari loop kitchen / Stage
    }
}
