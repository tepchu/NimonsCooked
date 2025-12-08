package controllers;

import models. item. Dish;
import models.level.*;
import models.player.ChefPlayer;
import models.core.Position;
import models. map.GameMap;
import models.map.MapType;
import models. order.*;
import models.recipe.*;

import java.util. ArrayList;
import java.util.HashMap;
import java. util.Iterator;
import java. util.List;
import java.util. Map;
import java.util.Random;

public class Stage {
    private final String id;
    private final MapType mapType;
    private final GameMap gameMap;
    private final List<ChefPlayer> chefs;
    private int activeChefIndex;
    private final OrderQueue orderQueue;
    private int score;
    private int timeRemaining;
    private boolean gameRunning;
    private int failedOrdersCount;
    private int maxFailedOrders;
    private Random random;
    private int orderSpawnInterval;
    private int maxActiveOrders;
    private int orderSpawnTimer;
    private int orderTimeout;
    private int successfulOrders;
    private int expiredOrders;
    private List<Recipe> availableRecipes;
    private Map<Order, Integer> orderTimers;

    public Stage(String id, MapType mapType, GameMap gameMap) {
        this. id = id;
        this.mapType = mapType;
        this.gameMap = gameMap;
        this.chefs = new ArrayList<>();
        this.activeChefIndex = 0;
        this. orderQueue = new OrderQueue();
        this.score = 0;
        this. timeRemaining = 180;
        this.gameRunning = false;
        this. failedOrdersCount = 0;
        this.maxFailedOrders = 5;
        this. random = new Random();
        this.orderSpawnInterval = 30;
        this. maxActiveOrders = 5;
        this. orderSpawnTimer = 0;
        this.orderTimeout = 60;
        this.successfulOrders = 0;
        this. expiredOrders = 0;
        this.orderTimers = new HashMap<>();
        initializeRecipes();
    }

    private void initializeRecipes() {
        availableRecipes = new ArrayList<>();
        availableRecipes.add(PizzaRecipeFactory.createPizzaMargherita());
        availableRecipes.add(PizzaRecipeFactory. createPizzaSosis());
        availableRecipes.add(PizzaRecipeFactory.createPizzaAyam());
    }

    public void applyLevelSettings(Level level) {
        this.timeRemaining = level. getTimeLimit();
        this. maxFailedOrders = level.getMaxFailedOrders();
        this.orderSpawnInterval = level.getOrderSpawnInterval();
        this.maxActiveOrders = level.getMaxActiveOrders();
        this.orderTimeout = level.getOrderTimeout();
        this.orderSpawnTimer = orderSpawnInterval;
    }

    public void initStage() {
        List<Position> spawns = gameMap. getChefSpawns();
        if (spawns.size() >= 2) {
            ChefPlayer chef1 = new ChefPlayer("chef_0", "Chef 1", spawns.get(0));
            ChefPlayer chef2 = new ChefPlayer("chef_1", "Chef 2", spawns.get(1));
            chefs.add(chef1);
            chefs.add(chef2);
        }

        int initialOrders = Math.max(1, maxActiveOrders / 2);
        for (int i = 0; i < initialOrders; i++) {
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

        if (failedOrdersCount >= maxFailedOrders) {
            endGame();
            return;
        }
        handleOrderTimeouts();
        updateOrderSpawning();
    }

    private void handleOrderTimeouts() {
        List<Order> expiredList = new ArrayList<>();

        for (Map.Entry<Order, Integer> entry : orderTimers. entrySet()) {
            Order order = entry.getKey();
            int timeLeft = entry.getValue() - 1;
            if (timeLeft <= 0) {
                expiredList.add(order);
            } else {
                orderTimers.put(order, timeLeft);
            }
        }

        for (Order expired : expiredList) {
            orderTimers.remove(expired);
            removeOrderFromQueue(expired);
            int expiredPenalty = calculateExpiredPenalty(expired);
            score -= expiredPenalty;
            failedOrdersCount++;
            expiredOrders++;
        }
    }

    private void removeOrderFromQueue(Order orderToRemove) {
        List<Order> remainingOrders = new ArrayList<>();
        while (!orderQueue.isEmpty()) {
            Order order = orderQueue. poll();
            if (order != orderToRemove) {
                remainingOrders.add(order);
            }
        }
        for (Order order : remainingOrders) {
            orderQueue.addOrder(order);
        }
    }

    private int calculateExpiredPenalty(Order order) {
        return (int) (order.getReward() * 0.3);
    }

    private int calculateWrongDishPenalty(Order order) {
        return (int) (order.getReward() * 0.5);
    }

    private void updateOrderSpawning() {
        orderSpawnTimer--;
        if (orderSpawnTimer <= 0) {
            orderSpawnTimer = orderSpawnInterval;
            if (orderQueue.size() < maxActiveOrders) {
                generateNewOrder();
            }
        }
    }

    private void generateNewOrder() {
        if (availableRecipes.isEmpty()) return;

        int index = random.nextInt(availableRecipes.size());
        Recipe recipe = availableRecipes.get(index);

        Order order = new Order(
                orderQueue.size() + 1,
                recipe,
                recipe.getBaseReward(),
                recipe. getBasePenalty()
        );
        orderQueue.addOrder(order);
        orderTimers.put(order, orderTimeout);
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
        chefs. add(chef);
    }

    public List<ChefPlayer> getChefs() {
        return chefs;
    }

    public OrderQueue getOrderQueue() {
        return orderQueue;
    }

    public int validateServe(Dish dish) {
        if (orderQueue.isEmpty() || dish == null)
            return 0;
        Order matchingOrder = findMatchingOrder(dish. getName());
        if (matchingOrder != null) {
            Recipe recipe = matchingOrder.getRecipe();
            boolean match = RecipeMatcher.doesDishMatchRecipe(dish, recipe);
            if (match) {
                score += matchingOrder.getReward();
                removeOrderFromQueue(matchingOrder);
                orderTimers.remove(matchingOrder);
                successfulOrders++;
                if (orderQueue.size() < maxActiveOrders) {
                    generateNewOrder();
                }
                return matchingOrder.getReward();
            }
        }

        Order firstOrder = orderQueue.peek();
        if (firstOrder != null) {
            int wrongPenalty = calculateWrongDishPenalty(firstOrder);
            score -= wrongPenalty;
            failedOrdersCount++;
            return -wrongPenalty;
        }

        return 0;
    }

    private Order findMatchingOrder(String recipeName) {
        List<Order> allOrders = getAllOrdersFromQueue();
        for (Order order : allOrders) {
            if (order. getRecipe().getName().equals(recipeName)) {
                return order;
            }
        }
        return null;
    }

    private List<Order> getAllOrdersFromQueue() {
        List<Order> orders = new ArrayList<>();
        List<Order> temp = new ArrayList<>();

        while (!orderQueue.isEmpty()) {
            Order order = orderQueue. poll();
            orders.add(order);
            temp.add(order);
        }

        for (Order order : temp) {
            orderQueue.addOrder(order);
        }

        return orders;
    }

    public int getOrderTimeRemaining(Order order) {
        return orderTimers.getOrDefault(order, 0);
    }

    public double getOrderTimeProgress(Order order) {
        int remaining = orderTimers.getOrDefault(order, 0);
        return (double) remaining / orderTimeout;
    }

    public List<Order> getAllOrders() {
        return getAllOrdersFromQueue();
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

    public int getMaxFailedOrders() {
        return maxFailedOrders;
    }

    public int getSuccessfulOrders() {
        return successfulOrders;
    }

    public int getExpiredOrders() {
        return expiredOrders;
    }

    public int getOrderSpawnInterval() {
        return orderSpawnInterval;

    }

    public int getMaxActiveOrders() {
        return maxActiveOrders;
    }

    public int getOrderTimeout() {
        return orderTimeout;
    }

    public List<Recipe> getAvailableRecipes() {
        return availableRecipes;
    }

    public void incrementFailedOrders() {
        failedOrdersCount++;
    }
}