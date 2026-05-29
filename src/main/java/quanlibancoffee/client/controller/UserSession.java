package quanlibancoffee.client.controller;

public class UserSession {
    public static String username;
    public static String role; // Lưu 'Admin' hoặc 'User'

    // Hàm xóa dữ liệu khi Đăng xuất
    public static void cleanSession() {
        username = null;
        role = null;
    }
}