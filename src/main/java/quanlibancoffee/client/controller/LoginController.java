package quanlibancoffee.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblMessage;
    @FXML private TextField txtPasswordVisible;
    @FXML private CheckBox chkShowPassword;

    @FXML
    private void handleLogin() {
        String loginInput = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (loginInput.isEmpty() || password.isEmpty()) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("❌ Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // 1. Đóng gói dữ liệu thành chuỗi theo định dạng Server quy định
        String requestStr = "LOGIN;" + loginInput + ";" + password;

        try {
            // 2. Bắn chuỗi này qua Socket Server thông qua ClientService
            String responseStr = quanlibancoffee.client.service.ClientService.sendRequest(requestStr);

            // 3. Xử lý kết quả phản hồi từ Server trả về
            if ("LOGIN_SUCCESS".equals(responseStr)) {
                lblMessage.setStyle("-fx-text-fill: green;");
                lblMessage.setText("✔ Đăng nhập thành công!");

                // Chuyển sang màn hình chính (bỏ chữ /image/ đi như mình sửa lúc trước nhé)
                switchScene("/quanlibancoffee/client/view/home.fxml", "Trang chủ - Coffee POS");

            } else if (responseStr != null && responseStr.startsWith("LOGIN_FAIL")) {
                String[] tokens = responseStr.split(";");
                String errorMsg = tokens.length > 1 ? tokens[1] : "Sai tài khoản hoặc mật khẩu!";
                lblMessage.setStyle("-fx-text-fill: red;");
                lblMessage.setText("❌ " + errorMsg);
            } else {
                lblMessage.setStyle("-fx-text-fill: red;");
                lblMessage.setText("❌ Lỗi hệ thống: " + responseStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("❌ Lỗi kết nối đến Server!");
        }
    }

    @FXML
    private void switchToRegister() {
        // ĐÃ SỬA: Bỏ chữ /image/
        switchScene("/quanlibancoffee/client/view/register.fxml", "Đăng ký tài khoản");
    }

    private void switchScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1200, 800);

            // ĐÃ SỬA: File style.css nằm ở thư mục view chứ không nằm trong image
            scene.getStylesheets().add(
                    getClass().getResource("/quanlibancoffee/client/view/style.css").toExternalForm()
            );

            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);

            if (fxmlPath.contains("home.fxml") || fxmlPath.contains("trangchu.fxml")) {
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
        try {
            // ĐÃ SỬA: Bỏ chữ /image/ cho cả cụm đăng ký bằng nút bấm này
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/quanlibancoffee/client/view/register.fxml")
            );
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                    getClass().getResource("/quanlibancoffee/client/view/style.css").toExternalForm()
            );

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Đăng ký tài khoản");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}