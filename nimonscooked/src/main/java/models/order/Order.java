package models.order;

import models.recipe.Recipe;

public class Order {

    private int position;
    private Recipe recipe;
    private int reward;
    private int penalty;
    private boolean completed;

    public Order(int position, Recipe recipe, int reward, int penalty){
        this.position = position;
        this.recipe = recipe;
        this.reward = reward;
        this.penalty = penalty;
        this.completed = false;
    }

    public int getPosition(){ return position; }
    public Recipe getRecipe(){ return recipe; }
    public int getReward(){ return reward; }
    public int getPenalty(){ return penalty; }
    public boolean isCompleted(){ return completed; }

    public void markCompleted(){
        this.completed = true;
    }
}
