package quanlibancoffee.shared.model;

import java.io.Serializable;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    private String action; // Ví dụ: "LOGIN", "GET_PRODUCTS", "CREATE_ORDER"
    private Object data;   // Dữ liệu đi kèm (chuỗi, hoặc đối tượng User, Product...)

    public Request(String action, Object data) {
        this.action = action;
        this.data = data;
    }

    public String getAction() { return action; }
    public Object getData() { return data; }
}