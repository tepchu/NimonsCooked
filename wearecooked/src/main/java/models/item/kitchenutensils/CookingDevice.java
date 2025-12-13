package models.item.kitchenutensils;

import models.item.Preparable;

public interface CookingDevice {
    boolean isPortable();
    int capacity();
    boolean canAccept(Preparable ingredient);
    void addIngredient(Preparable ingredient);
    void startCooking();
}
