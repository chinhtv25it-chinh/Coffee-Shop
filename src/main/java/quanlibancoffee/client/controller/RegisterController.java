package quanlibancoffee.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import quanlibancoffee.server.utils.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;

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

        // 1. Kiểm tra trống
        if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            lblMessage.setText("⚠ Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // 2. Kiểm tra định dạng Email
        String emailPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        if (!email.matches(emailPattern)) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("❌ Email không đúng định dạng!");
            return;
        }

        // 3. Thực hiện lưu vào Database nếu email hợp lệ
        String sql = "INSERT INTO users(email, username, password) VALUES (?, ?, ?)";

        try (Connection con = Database.connectDB();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, username);
            ps.setString(3, password);
            ps.executeUpdate();

            lblMessage.setStyle("-fx-text-fill: green;");
            lblMessage.setText("✔ Đăng ký thành công!");

        } catch (Exception e) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("❌ Tài khoản hoặc Email đã tồn tại!");
        }
    }

    @FXML
    private void goLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/quanlibancoffee/client/view/login.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
