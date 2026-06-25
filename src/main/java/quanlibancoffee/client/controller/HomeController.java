package quanlibancoffee.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button; // Thêm import Button để xử lý ẩn/hiện
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    @FXML
    private StackPane contentArea;

    // Thêm các nút menu để dễ dàng ẩn/hiện theo quyền
    @FXML private Button btnSanPham;
    @FXML private Button btnThongKe;
    @FXML private Button btnTaiKhoan;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        showTrangChu();

        // 2. Gọi hàm phân quyền ngay khi khởi tạo giao diện chính để ẩn các tính năng không phù hợp với vai trò người dùng
        checkAuthorization();
    }

    /**
     * Hàm thực hiện phân quyền ẩn chức năng của Admin đối với Nhân viên thường
     */
    private void checkAuthorization() {
        // Nếu vai trò trong bộ nhớ tạm UserSession là 'User' (Nhân viên thường)
        if ("User".equalsIgnoreCase(UserSession.role)) {

            // Ẩn nút Quản lý sản phẩm
            if (btnSanPham != null) {
                btnSanPham.setVisible(false);
                btnSanPham.setManaged(false); // Xóa không gian chiếm chỗ để menu co lên gọn gàng
            }

            // Ẩn nút Thống kê
            if (btnThongKe != null) {
                btnThongKe.setVisible(false);
                btnThongKe.setManaged(false);
            }

            // Ẩn nút Quản lý tài khoản nhân viên
            if (btnTaiKhoan != null) {
                btnTaiKhoan.setVisible(false);
                btnTaiKhoan.setManaged(false);
            }

            System.out.println("🔒 [PHÂN QUYỀN] Đã ẩn các tính năng Admin đối với nhân viên thường.");
        } else {
            System.out.println("🔓 [PHÂN QUYỀN] Tài khoản Admin đăng nhập. Hiển thị toàn bộ Menu.");
        }
    }

    private void loadPage(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/quanlibancoffee/client/view/" + fxml));
            Parent root = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
        } catch (IOException e) {
            System.err.println("Lỗi load tệp FXML: " + fxml);
            e.printStackTrace();
        }
    }

    @FXML void showTrangChu() { loadPage("trangchu.fxml"); }
    @FXML void showBanHang() { loadPage("banhang.fxml"); }
    @FXML void showSanPham() { loadPage("sanpham.fxml"); }
    @FXML void showThongKe() { loadPage("thongke.fxml"); }
    @FXML void showTaiKhoan() { loadPage("taikhoan.fxml"); }

    @FXML
    void logout() {
        try {
            // 3. Xóa sạch thông tin tài khoản và quyền trong bộ nhớ tạm khi đăng xuất
            UserSession.cleanSession();
            System.out.println("♻ [ĐĂNG XUẤT] Đã xóa phiên làm việc an toàn.");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/quanlibancoffee/client/view/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) contentArea.getScene().getWindow();
            Scene scene = new Scene(root);

            try {
                scene.getStylesheets().add(getClass().getResource("/quanlibancoffee/client/view/style.css").toExternalForm());
            } catch (Exception cssEx) {
                System.out.println("⚠ Không tìm thấy style.css khi đăng xuất, hệ thống sử dụng CSS mặc định.");
            }

            stage.setScene(scene);

            // Hủy trạng thái phóng to màn hình chính, đưa cửa sổ đăng nhập về giữa màn hình
            stage.setMaximized(false);
            stage.setWidth(1040);
            stage.setHeight(640);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            System.err.println("❌ Lỗi: Không thể tải được giao diện đăng nhập khi đăng xuất!");
            e.printStackTrace();
        }
    }
}