package quanlibancoffee.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    @FXML
    private StackPane contentArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        showTrangChu();
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

    @FXML
    void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/quanlibancoffee/client/view/image/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) contentArea.getScene().getWindow();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/quanlibancoffee/client/view/image/style.css").toExternalForm());


            stage.setScene(scene);
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}