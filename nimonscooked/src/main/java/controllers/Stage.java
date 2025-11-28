package controllers;

import java.util.ArrayList;
import java.util.List;

import models.player.ChefPlayer;
import models.core.Position;
import models.item.Dish;
import models.map.GameMap;
import models.order.Order;
import models.order.OrderQueue;
import models.order.Recipe;


public class Stage {

    private final String id;
    private final MapType mapType;

    private final GameMap gameMap;              
    private final List<ChefPlayer> chefs;        
    private int activeChefIndex;                 

    private final OrderQueue orderQueue;         
    private final List<Recipe> availableRecipes; 
    private int score;                           

    
    public Stage(String id, MapType mapType, GameMap gameMap) {
        this.id = id;
        this.mapType = mapType;
        this.gameMap = gameMap;

        this.chefs = new ArrayList<>();
        this.activeChefIndex = 0;

        this.orderQueue = new OrderQueue();
        this.availableRecipes = new ArrayList<>();

        this.score = 0;
    }

    
    public void initStage() {
    }

    public void update() {
    }

    public ChefPlayer getActiveChef() {
        if (chefs.isEmpty()) return null;
        return chefs.get(activeChefIndex);
    }

    public void switchActiveChef() {
        if (chefs.isEmpty()) return;
        activeChefIndex = (activeChefIndex + 1) % chefs.size();
    }

    public void addChef(ChefPlayer chef) {
        chefs.add(chef);
    }

    public List<ChefPlayer> getChefs() {
        return chefs;
    }

    public OrderQueue getOrderQueue() {
        return orderQueue;
    }

    public List<Recipe> getAvailableRecipes() {
        return availableRecipes;
    }

    public void addRecipe(Recipe r) {
        availableRecipes.add(r);
    }

    public void addOrder(Order order) {
        orderQueue.addOrder(order);
    }

    public int validateServe(Dish dish) {
        if (orderQueue.isEmpty() || dish == null)
            return 0;

        Order current = orderQueue.peek();
        Recipe recipe = current.getRecipe();

        boolean match = RecipeMatcher.doesDishMatchRecipe(dish, recipe);

        if (match) {
            score += recipe.getReward();
            orderQueue.poll(); // remove served order
            return recipe.getReward();
        } else {
            score -= recipe.getPenalty();
            return -recipe.getPenalty();
        }
    }

    public String getId() { 
        return id; 
    }

    public MapType getMapType() { 
        return mapType; 
    }

    public GameMap getGameMap() { 
        return gameMap; 
    }
    
    public int getScore() { 
        return score; 
    }
}
