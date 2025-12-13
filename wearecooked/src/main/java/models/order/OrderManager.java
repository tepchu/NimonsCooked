package models.order;

import java.util.ArrayList;
import java.util.List;

public class OrderManager {

    private static OrderManager instance = null;
    private List<Order> activeOrders;

    private OrderManager() {
        activeOrders = new ArrayList<>();
    }

    public static OrderManager getInstance() {
        if (instance == null) {
            instance = new OrderManager();
        }
        return instance;
    }

    public void addOrder(Order order) {
        activeOrders.add(order);
    }

    public void removeOrder(Order order) {
        activeOrders.remove(order);
    }

    public List<Order> getActiveOrders() {
        return activeOrders;
    }

    public Order getNextOrder() {
        if (activeOrders.isEmpty()) return null;
        return activeOrders.get(0);
    }
}
