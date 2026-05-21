package quanlibancoffee.shared.model;

import java.io.Serializable;

public class OrderSummary implements Serializable {
    private static final long serialVersionUID = 1L;
    private String time;
    private double total;
    private String method;

    public OrderSummary(String time, double total, String method) {
        this.time = time;
        this.total = total;
        this.method = method;
    }

    public String getTime() { return time; }
    public double getTotal() { return total; }
    public String getMethod() { return method; }
}
