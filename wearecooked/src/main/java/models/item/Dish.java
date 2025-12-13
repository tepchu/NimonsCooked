package models.item;

import java.util.List;
import java.util.ArrayList;

public class Dish extends Item {
    private String dishName;
    protected List<Preparable> components;

    public Dish(String dishName) {
        super(dishName);
        this.dishName = dishName;
        this.components = new ArrayList<>();
    }

    public void addComponent(Preparable ingredient) {
        components.add(ingredient);
    }

    public List<Preparable> getComponents() {
        return new ArrayList<>(components);
    }

    public String getDishName() {
        return dishName;
    }

    @Override
    public void showItem() {
        System.out.println("Dish: " + dishName + " currently has " + components.size() + " components");
    }
}
