package models.item;

import models.enums.*;

public abstract class Ingredient extends Item implements Preparable {
    protected IngredientType type;
    protected IngredientState state;
    protected int chopProgress;
    protected int cookingTime;

    public Ingredient(String name, IngredientType type){
        super(name);
        this.type = type;
        this.state = IngredientState.RAW;
        this.chopProgress = 0;
        this.cookingTime = 0;
    }

    public IngredientType getType(){
        return type;
    }

    public IngredientState getState(){
        return state;
    }

    public void setState(IngredientState newState){
        this.state = newState;
    }

    @Override
    public void cook(){
        if (canBeCooked() && (state == IngredientState.RAW || state == IngredientState.CHOPPED)){
            state = IngredientState.COOKED;
        }
    }

    @Override
    public void burn(){
        state = IngredientState.BURNED;
    }

    @Override
    public void showItem(){
        // for CLI testing purposes
        System.out.println(getName() + " (" + state + ")");
    }

    public int getChopProgress(){
        return chopProgress;
    }

    public void setChopProgress(int progress){
        this.chopProgress = progress;
    }

    public int getCookingTime(){
        return cookingTime;
    }

    public void setCookingTime(int time){
        this.cookingTime = time;
    }
}
