package models.item;

public abstract class Item {
    protected String itemName;

    public Item(String itemName){
        this.itemName = itemName;
    }

    public String getName(){
        return itemName;
    }

    public abstract void showItem();

    @Override
    public String toString(){
        return itemName;
    }
}
