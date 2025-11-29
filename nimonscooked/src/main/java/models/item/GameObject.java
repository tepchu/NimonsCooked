package models.item;

import models.core.Position;

public abstract class GameObject {

    protected String id;
    protected Position position;

    public GameObject(String id, Position position) {
        this.id = id;
        this.position = position;
    }

    public abstract void update();

    public String getId() {
        return id;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }
}
