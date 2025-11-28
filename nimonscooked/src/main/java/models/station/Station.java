package models.station;

import models.core.Position;
import models.player.ChefPlayer;
import models.enums.StationType;

public abstract class Station {

    protected final StationType type;
    protected final Position position;

    protected Station(StationType type, Position position) {
        this.type = type;
        this.position = position;
    }

    public StationType getType() { return type; }
    public Position getPosition() { return position; }

    public abstract void interact(ChefPlayer chef);
}
