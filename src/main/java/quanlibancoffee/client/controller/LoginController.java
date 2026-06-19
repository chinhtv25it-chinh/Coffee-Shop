package quanlibancoffee.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

// Thêm thư viện phục vụ mã hóa mật khẩu SHA-256
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtPasswordVisible;
    @FXML private CheckBox chkShowPassword;
    @FXML private Label lblMessage;

    @FXML
    private void handleLogin() {
        // Lấy mật khẩu linh hoạt dựa theo trạng thái Checkbox đang ẩn hay hiện
        String username = txtUsername.getText().trim();
        String password = chkShowPassword.isSelected() ? txtPasswordVisible.getText().trim() : txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            lblMessage.setStyle("-fx-text-fill: #e74c3c;");
            lblMessage.setText("❌ Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        try {
            // 1. [BẢO MẬT] Mã hóa mật khẩu người dùng vừa nhập sang chuỗi SHA-256 trước khi gửi đi
            String hashedPass = hashSHA256(password);

            // 2. Đóng gói dữ liệu truyền qua Socket theo định dạng chuỗi (Mật khẩu gửi đi đã được băm an toàn)
            String requestStr = "LOGIN;" + username + ";" + hashedPass;

            // 3. Bắn chuỗi dữ liệu sang Server thông qua tầng Service
            String responseStr = quanlibancoffee.client.service.ClientService.sendRequest(requestStr);

            // 4. Xử lý phản hồi từ Server trả về (Định dạng chuẩn từ Server: LOGIN_SUCCESS;Admin hoặc LOGIN_SUCCESS;User)
            if (responseStr != null && responseStr.startsWith("LOGIN_SUCCESS")) {
                String[] tokens = responseStr.split(";");

                // [PHÂN QUYỀN] Lưu lại thông tin Tên đăng nhập và Quyền vào bộ nhớ tạm UserSession toàn cục
                UserSession.username = username;
                UserSession.role = (tokens.length > 1) ? tokens[1] : "User"; // Mặc định là quyền User nếu thiếu token quyền

                lblMessage.setStyle("-fx-text-fill: #2ecc71;");
                lblMessage.setText("✔ Đăng nhập thành công!");

                // Chuyển sang màn hình chính của Coffee App
                switchScene("/quanlibancoffee/client/view/home.fxml", "Trang chủ - Coffee POS");

            } else if (responseStr != null && responseStr.startsWith("LOGIN_FAIL")) {
                String[] tokens = responseStr.split(";");
                String errorMsg = tokens.length > 1 ? tokens[1] : "Sai tài khoản hoặc mật khẩu!";
                lblMessage.setStyle("-fx-text-fill: #e74c3c;");
                lblMessage.setText("❌ " + errorMsg);
            } else {
                lblMessage.setStyle("-fx-text-fill: #e74c3c;");
                lblMessage.setText("❌ Lỗi hệ thống: " + responseStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
            lblMessage.setStyle("-fx-text-fill: #e74c3c;");
            lblMessage.setText("❌ Lỗi kết nối đến Server!");
        }
    }

    /**
     * Hàm băm bảo mật SHA-256 đồng bộ với hệ thống cơ sở dữ liệu
     */
    private String hashSHA256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            return base; // Trả về chuỗi gốc phòng trường hợp thuật toán mã hóa lỗi
        }
    }

    @FXML
    private void togglePassword() {
        if (chkShowPassword.isSelected()) {
            txtPasswordVisible.setText(txtPassword.getText());
            txtPasswordVisible.setVisible(true);
            txtPasswordVisible.setManaged(true);

            txtPassword.setVisible(false);
            txtPassword.setManaged(false);
        } else {
            txtPassword.setText(txtPasswordVisible.getText());
            txtPassword.setVisible(true);
            txtPassword.setManaged(true);

            txtPasswordVisible.setVisible(false);
            txtPasswordVisible.setManaged(false);
        }
    }

    @FXML
    private void goRegister(ActionEvent event) {
        switchScene("/quanlibancoffee/client/view/register.fxml", "Đăng ký tài khoản");
    }

    private void switchScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = new Scene(root);

            // Tự động kiểm tra nạp file CSS
            try {
                scene.getStylesheets().add(getClass().getResource("/quanlibancoffee/client/view/style.css").toExternalForm());
            } catch (Exception cssEx) {
                System.out.println("⚠ Hệ thống bỏ qua nạp style.css (Sử dụng CSS nhúng trực tiếp FXML)");
            }

            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);

            if (fxmlPath.contains("home.fxml")) {
                stage.setMaximized(true);
            } else {
                stage.setMaximized(false);
                stage.centerOnScreen();
            }

            stage.show();
        } catch (Exception e) {
            System.out.println("❌ Lỗi không nạp được giao diện tại đường dẫn: " + fxmlPath);
            e.printStackTrace();
        }
    }
}