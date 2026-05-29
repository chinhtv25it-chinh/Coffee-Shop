package quanlibancoffee.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController {

    @FXML private TextField txtEmail;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblMessage;

    @FXML
    public void handleRegister(ActionEvent event) {
        String email = txtEmail.getText().trim();
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        // 1. Kiểm tra trống đầu vào
        if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            lblMessage.setStyle("-fx-text-fill: #e74c3c;");
            lblMessage.setText("⚠ Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // 2. Kiểm tra định dạng Email chuẩn bằng RegEx
        String emailPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        if (!email.matches(emailPattern)) {
            lblMessage.setStyle("-fx-text-fill: #e74c3c;");
            lblMessage.setText("❌ Email không đúng định dạng!");
            return;
        }

        // 3. Đóng gói chuỗi dữ liệu gửi qua Socket Server theo định dạng: REGISTER;username;email;password
        String requestStr = "REGISTER;" + username + ";" + email + ";" + password;

        try {
            // Gửi yêu cầu sang Server và đợi phản hồi trả về
            String responseStr = quanlibancoffee.client.service.ClientService.sendRequest(requestStr);

            if ("REGISTER_SUCCESS".equals(responseStr)) {
                lblMessage.setStyle("-fx-text-fill: #2ecc71;");
                lblMessage.setText("✔ Đăng ký thành công tài khoản!");

                // Đăng ký xong thì xóa sạch form để tiện thao tác tiếp
                txtUsername.clear();
                txtEmail.clear();
                txtPassword.clear();
            } else if (responseStr != null && responseStr.startsWith("REGISTER_FAIL")) {
                String[] tokens = responseStr.split(";");
                String reason = tokens.length > 1 ? tokens[1] : "Tài khoản hoặc Email đã tồn tại!";
                lblMessage.setStyle("-fx-text-fill: #e74c3c;");
                lblMessage.setText("❌ " + reason);
            } else {
                lblMessage.setStyle("-fx-text-fill: #e74c3c;");
                lblMessage.setText("❌ Lỗi phản hồi từ hệ thống: " + responseStr);
            }

        } catch (Exception e) {
            e.printStackTrace();
            lblMessage.setStyle("-fx-text-fill: #e74c3c;");
            lblMessage.setText("❌ Không thể kết nối đến máy chủ Server!");
        }
    }

    @FXML
    private void goLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/quanlibancoffee/client/view/login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Đăng nhập hệ thống");
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            System.out.println("❌ Lỗi không quay lại được trang đăng nhập!");
            e.printStackTrace();
        }
    }
}