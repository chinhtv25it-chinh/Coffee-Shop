package quanlibancoffee.shared.model;

import java.io.Serializable;

public class CoffeeTable implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String tableName;
    private String status;

    public CoffeeTable(int id, String tableName, String status) {
        this.id = id;
        this.tableName = tableName;
        this.status = status;
    }
    // Getters và Setters...
    public String getTableName() { return tableName; }
    public String getStatus() { return status; }

    public int getId() {
        return id;
    }
}
