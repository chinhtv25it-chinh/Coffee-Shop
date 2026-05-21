package quanlibancoffee.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import quanlibancoffee.client.service.ClientService;

import java.util.Objects;

public class CoffeeApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // 1. Kích hoạt kết nối socket mạng sang server trước khi hiển thị màn hình
        ClientService.connect();

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/quanlibancoffee/client/view/login.fxml")
        );
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/quanlibancoffee/client/view/style.css")).toExternalForm()
        );

        stage.setTitle("Hệ Thống Quản Lý Cà Phê");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}