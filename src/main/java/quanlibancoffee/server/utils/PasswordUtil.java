package quanlibancoffee.server.utils;

import java.security.MessageDigest;

public class PasswordUtil {

    // Hàm băm mật khẩu thô thành chuỗi SHA-256 an toàn
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString(); // Trả về chuỗi 64 ký tự đã mã hóa
        } catch (Exception ex) {
            throw new RuntimeException("❌ Lỗi xảy ra trong quá trình băm mật khẩu: " + ex.getMessage());
        }
    }
}