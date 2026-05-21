package quanlibancoffee.shared.model;

import java.io.Serializable;

public class Order implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String orderDate;
    private String orderTime;
    private double totalAmount;
    private String paymentMethod;

    public Order(int id, String orderDate, String orderTime, double totalAmount, String paymentMethod) {
        this.id = id;
        this.orderDate = orderDate;
        this.orderTime = orderTime;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }

    public String getOrderTime() { return orderTime; }
    public void setOrderTime(String orderTime) { this.orderTime = orderTime; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}