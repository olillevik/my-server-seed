package net.openright.simpleserverseed.domain.orders;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class Order {

    private String title;
    private Integer id;
    private List<OrderLine> orderLines = new ArrayList<>();

    Order(String title) {
        this.title = title;
    }


    int getId() {
        return id;
    }

    String getTitle() {
        return title;
    }
    
    void addOrderLine(Long productId, int amount) {
        this.orderLines.add(new OrderLine(productId, amount));
    }

    double getTotalAmount() {
        return orderLines.stream().map(line -> line.getPrice()).reduce(0.0, (a,b) -> a+b);
    }

    void setOrderLines(List<OrderLine> orderLines) {
        this.orderLines = orderLines;
    }

    void setId(int id) {
        this.id = id;
    }

    void setTitle(String title) {
        this.title = title;
    }

    List<OrderLine> getOrderLines() {
        return orderLines;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Order)) {
            return false;
        }

        Order other = (Order) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(title, other.title) && Objects.equals(orderLines, other.orderLines);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title);
    }

    @Override
    public String toString() {
        return "Order {id = " + id
                + ", title = " + title
                + "," + orderLines.toString()
                + "}";
    }
}
