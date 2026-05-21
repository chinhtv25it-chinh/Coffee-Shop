package quanlibancoffee.server.dao;

import quanlibancoffee.shared.model.CoffeeTable;
import quanlibancoffee.server.utils.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TableDAO {
    public static List<CoffeeTable> getAllTables() {
        List<CoffeeTable> list = new ArrayList<>();
        String sql = "SELECT * FROM coffee_tables";
        try (Connection conn = Database.connectDB();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new CoffeeTable(
                        rs.getInt("id"),
                        rs.getString("table_name"),
                        rs.getString("status")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    public static void updateTableStatus(int tableId, String status) {
            String sql = "UPDATE coffee_tables SET status = ? WHERE id = ?";

            try (Connection con = Database.connectDB();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, status);
                ps.setInt(2, tableId);

                ps.executeUpdate();
                System.out.println("Đã cập nhật trạng thái bàn ID " + tableId + " thành: " + status);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
}
