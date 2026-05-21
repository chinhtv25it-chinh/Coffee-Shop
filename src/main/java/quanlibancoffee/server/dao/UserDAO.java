package quanlibancoffee.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import quanlibancoffee.server.utils.Database;

public class UserDAO {

    // Hàm kiểm tra đăng nhập trả về true nếu đúng, false nếu sai
    public static boolean checkLogin(String usernameOrEmail, String password) {
        String sql = "SELECT * FROM users WHERE (username = ? OR email = ?) AND password = ?";

        try (Connection con = Database.connectDB();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usernameOrEmail);
            ps.setString(2, usernameOrEmail);
            ps.setString(3, password);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // Nếu có dữ liệu trả về true, ngược lại false
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}