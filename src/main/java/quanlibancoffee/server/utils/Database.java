package quanlibancoffee.server.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    public static Connection connectDB() {
        try {
            // Thông số kết nối của bạn
            String url = "jdbc:sqlserver://LAPTOP-25MTHQP6\\SQLEXPRESS:1433;"
                    + "databaseName=coffee_db;"
                    + "user=sa;password=Tranchinh@76;"
                    + "encrypt=true;trustServerCertificate=true;";

            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(url);
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("❌ Lỗi kết nối Database: " + e.getMessage());
            return null;
        }
    }
}