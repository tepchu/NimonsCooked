package models.order;

public class OrderTest {
    public static void main(String[] args) {

        OrderManager manager = OrderManager.getInstance();

        Order o1 = new Order(1, "Pizza Margherita", 50, 10);
        Order o2 = new Order(2, "Pizza Sosis", 60, 15);

        manager.addOrder(o1);
        manager.addOrder(o2);

        System.out.println("Active Orders:");
        for (Order o : manager.getActiveOrders()) {
            System.out.println(
                o.getPosition() + " . " +
                o.getRecipe() + " | reward=" + o.getReward() +
                " | penalty=" + o.getPenalty()
            );
        }

        System.out.println("Next Order = " + manager.getNextOrder().getRecipe());

        o1.markCompleted();
        manager.removeOrder(o1);

        System.out.println("After completing order 1:");
        for (Order o : manager.getActiveOrders()) {
            System.out.println(
                o.getPosition() + " . " +
                o.getRecipe() + " | reward=" + o.getReward() +
                " | penalty=" + o.getPenalty()
            );
        }
    }
}
