package controllers;


import models.player.ChefPlayer;
import models.core.Position;
import models.map.GameMap;
import models.map.MapType;
import models.order.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Stage {
    private final String id;
    private final MapType mapType;
    private final GameMap gameMap;
    private final List<ChefPlayer> chefs;
    private int activeChefIndex;
    private final OrderQueue orderQueue;
    private int score;
    private int timeRemaining; // seconds
    private boolean gameRunning;
    private int failedOrdersCount;
    private static final int MAX_FAILED_ORDERS = 5;
    private Random random;

    public Stage(String id, MapType mapType, GameMap gameMap) {
        this.id = id;
        this.mapType = mapType;
        this.gameMap = gameMap;
        this.chefs = new ArrayList<>();
        this.activeChefIndex = 0;
        this.orderQueue = new OrderQueue();
        this.score = 0;
        this.timeRemaining = 180; // 3 minutes
        this.gameRunning = false;
        this.failedOrdersCount = 0;
        this.random = new Random();
    }


    public void initStage() {
        List<Position> spawns = gameMap.getChefSpawns();
        if (spawns.size() >= 2) {
            ChefPlayer chef1 = new ChefPlayer("chef_0", "Chef 1", spawns.get(0));
            ChefPlayer chef2 = new ChefPlayer("chef_1", "Chef 2", spawns.get(1));
            chefs.add(chef1);
            chefs.add(chef2);
        }
        for (int i = 0; i < 3; i++) {
            generateNewOrder();
        }
        gameRunning = true;
    }

    public void update() {
        if (!gameRunning) return;
        timeRemaining--;
        if (timeRemaining <= 0) {
            endGame();
            return;
        }
        if (failedOrdersCount >= MAX_FAILED_ORDERS) {
            endGame();
            return;
        }

        // TODO: add update order timers
    }

    private void generateNewOrder() {
        String[] pizzas = {"Pizza Margherita", "Pizza Sosis", "Pizza Ayam"};
        int[] rewards = {100, 150, 150};
        int[] penalties = {50, 70, 70};
        int[] timeLimits = {60, 90, 90};

        int index = random.nextInt(pizzas.length);
        Order order = new Order(
                orderQueue.size() + 1,
                pizzas[index],
                rewards[index],
                penalties[index]
        );

        orderQueue.addOrder(order);
    }

    public void startGame() {
        gameRunning = true;
    }

    public void endGame() {
        gameRunning = false;
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

//    public List<Recipe> getAvailableRecipes() {
//        return availableRecipes;
//    }
//
//    public void addRecipe(Recipe r) {
//        availableRecipes.add(r);
//    }
//
//    public void addOrder(Order order) {
//        orderQueue.addOrder(order);
//    }

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

    public int getTimeRemaining() {
        return timeRemaining;
    }

    public boolean isGameRunning() {
        return gameRunning;
    }

    public int getFailedOrdersCount() {
        return failedOrdersCount;
    }

    public void incrementFailedOrders() {
        failedOrdersCount++;
    }
}
