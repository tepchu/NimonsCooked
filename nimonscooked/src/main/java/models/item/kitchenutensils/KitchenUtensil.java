package models.item.kitchenutensils;

import models.item.Item;
import models.item.Preparable;
import java.util.ArrayList;
import java.util.List;

public abstract class KitchenUtensil extends Item {
    protected List<Preparable> contents;

    public KitchenUtensil(String name){
        super(name);
        this.contents = new ArrayList<>();
    }

    public void addIngredient(Preparable ingredient){
        contents.add(ingredient);
    }

    public void removeIngredient(Preparable ingredient){
        contents.remove(ingredient);
    }

    public void clearContents(){
        contents.clear();
    }

    public List<Preparable> getContents(){
        return new ArrayList<>(contents);
    }

    public boolean isEmpty(){
        return contents.isEmpty();
    }

    @Override
    public void showItem(){
        System.out.println(getName() + " contains " + contents.size() + " items");
    }
}
