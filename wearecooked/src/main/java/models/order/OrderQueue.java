package models.order;

import java.util.LinkedList;
import java.util.Queue;
import java.util.List;
import java.util.ArrayList;

public class OrderQueue {
    private Queue<Order> orders;

    public OrderQueue() {
        this.orders = new LinkedList<>();
    }

    public void addOrder(Order order) {
        orders.offer(order);
    }

    public Order poll() {
        return orders.poll();
    }

    public Order peek() {
        return orders.peek();
    }

    public boolean isEmpty() {
        return orders.isEmpty();
    }

    public int size() {
        return orders.size();
    }

    public List<Order> getAll() {
        return new ArrayList<>(orders);
    }
}
