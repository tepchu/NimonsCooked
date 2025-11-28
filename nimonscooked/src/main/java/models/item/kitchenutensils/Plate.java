package models.item.kitchenutensils;

public class Plate extends KitchenUtensil {
    private boolean clean;

    public Plate() {
        super("Plate");
        this.clean = true;
    }

    public boolean isClean() {
        return clean;
    }

    public void setClean(boolean clean) {
        this.clean = clean;
    }

    public void markDirty() {
        this.clean = false;
    }

    @Override
    public void showItem() {
        System.out.println("Plate (" + (clean ? "Clean" : "Dirty") + ")");
    }
}
