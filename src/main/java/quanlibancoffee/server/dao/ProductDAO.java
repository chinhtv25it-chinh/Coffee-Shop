package quanlibancoffee.server.dao;

import quanlibancoffee.server.utils.Database;
import quanlibancoffee.shared.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class ProductDAO {
    private static Product mapProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getDouble("price"),
                rs.getString("image"),
                rs.getString("category"),
                rs.getInt("status")
        );
    }

    public static List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE status = 1";
        try (Connection conn = Database.connectDB();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapProduct(rs));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // Thêm hàm cập nhật trạng thái (Xóa mềm)
    public static boolean updateStatus(int id, int status) {
        String sql = "UPDATE products SET status = ? WHERE id = ?";
        try (Connection conn = Database.connectDB();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }
}
