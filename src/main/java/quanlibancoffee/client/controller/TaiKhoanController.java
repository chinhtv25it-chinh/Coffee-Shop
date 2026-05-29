package quanlibancoffee.client.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ResourceBundle;

public class TaiKhoanController implements Initializable {

    @FXML private TableView<UserBean> tableUsers;
    @FXML private TableColumn<UserBean, String> colUsername;
    @FXML private TableColumn<UserBean, String> colEmail;
    @FXML private TableColumn<UserBean, String> colRole;

    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cbRole;
    @FXML private Label lblStatus;

    private ObservableList<UserBean> userList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Cấu hình các cột ánh xạ đúng thuộc tính của UserBean
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Lắng nghe sự kiện click vào dòng trên bảng để đẩy ngược lên form nhập
        tableUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtUsername.setText(newSelection.getUsername());
                txtEmail.setText(newSelection.getEmail());
                cbRole.setValue(newSelection.getRole());
                txtUsername.setEditable(false); // Không cho sửa username (vì là khóa chính)
            }
        });

        loadAllUsers();
    }

    // Hàm gọi Server lấy toàn bộ danh sách tài khoản nạp vào bảng
    private void loadAllUsers() {
        userList.clear();
        txtUsername.setEditable(true);
        try {
            String response = quanlibancoffee.client.service.ClientService.sendRequest("FETCH_USERS");
            if (response.startsWith("FETCH_USERS_SUCCESS")) {
                String[] tokens = response.split(";");
                for (int i = 1; i < tokens.length; i++) {
                    String[] fields = tokens[i].split(",");
                    if (fields.length >= 3) {
                        userList.add(new UserBean(fields[0], fields[1], fields[2]));
                    }
                }
                tableUsers.setItems(userList);
            }
        } catch (Exception e) {
            lblStatus.setText("❌ Không thể kết nối lấy dữ liệu tài khoản!");
        }
    }

    @FXML
    private void handleAdd() {
        String uname = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String pass = txtPassword.getText().trim();
        String role = cbRole.getValue();

        if (uname.isEmpty() || email.isEmpty() || pass.isEmpty() || role == null) {
            lblStatus.setText("⚠ Vui lòng điền đủ thông tin để thêm!");
            return;
        }

        // Bảo mật nâng cao: Băm mật khẩu bằng SHA-256 trước khi gửi qua mạng
        String hashedPass = hashSHA256(pass);
        String request = "ADD_USER;" + uname + ";" + email + ";" + hashedPass + ";" + role;
        String response = quanlibancoffee.client.service.ClientService.sendRequest(request);

        if ("ADD_USER_SUCCESS".equals(response)) {
            lblStatus.setText("✔ Thêm tài khoản thành công!");
            clearForm();
            loadAllUsers();
        } else {
            lblStatus.setText("❌ " + response.split(";")[1]);
        }
    }

    @FXML
    private void handleUpdate() {
        String uname = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String role = cbRole.getValue();

        if (uname.isEmpty() || email.isEmpty() || role == null) {
            lblStatus.setText("⚠ Vui lòng chọn tài khoản cần sửa!");
            return;
        }

        String request = "UPDATE_USER;" + uname + ";" + email + ";" + role;
        String response = quanlibancoffee.client.service.ClientService.sendRequest(request);

        if ("UPDATE_USER_SUCCESS".equals(response)) {
            lblStatus.setText("✔ Cập nhật thành công!");
            clearForm();
            loadAllUsers();
        } else {
            lblStatus.setText("❌ Cập nhật thất bại!");
        }
    }

    @FXML
    private void handleDelete() {
        String uname = txtUsername.getText().trim();
        if (uname.isEmpty()) {
            lblStatus.setText("⚠ Vui lòng chọn tài khoản muốn xóa!");
            return;
        }

        String request = "DELETE_USER;" + uname;
        String response = quanlibancoffee.client.service.ClientService.sendRequest(request);

        if ("DELETE_USER_SUCCESS".equals(response)) {
            lblStatus.setText("✔ Đã xóa tài khoản!");
            clearForm();
            loadAllUsers();
        } else {
            lblStatus.setText("❌ Không thể xóa tài khoản này!");
        }
    }

    private void clearForm() {
        txtUsername.clear();
        txtEmail.clear();
        txtPassword.clear();
        cbRole.setValue(null);
    }

    // Hàm băm bảo mật SHA-256 đồng bộ với hệ thống
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
            throw new RuntimeException(ex);
        }
    }

    // --- LỚP BEAN ĐẠI DIỆN DỮ LIỆU ---
    public static class UserBean {
        private final String username;
        private final String email;
        private final String role;

        public UserBean(String username, String email, String role) {
            this.username = username;
            this.email = email;
            this.role = role;
        }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
    }
}