package quanlibancoffee.client.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import quanlibancoffee.shared.model.Product;
import quanlibancoffee.server.dao.ProductDAO;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ResourceBundle;

public class SanPhamController implements Initializable {

    @FXML
    private TableView<Product> tableProducts;
    @FXML
    private TableColumn<Product, Integer> colId;
    @FXML
    private TableColumn<Product, String> colImage;
    @FXML
    private TableColumn<Product, String> colName;
    @FXML
    private TableColumn<Product, String> colCategory;
    @FXML
    private TableColumn<Product, Double> colPrice;
    @FXML
    private TableColumn<Product, Integer> colStatus;
    @FXML
    private TableColumn<Product, Void> colAction;

    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cbCategoryFilter;

    private ObservableList<Product> productList;
    private ProductDAO productDAO = new ProductDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadData();
        setupFilters();
    }

    private void setupTableColumns() {
        // Căn các cột
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setStyle("-fx-alignment: CENTER;");

        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colName.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colCategory.setStyle("-fx-alignment: CENTER;");

        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colPrice.setStyle("-fx-alignment: CENTER; -fx-text-fill: #e67e22; -fx-font-weight: bold;");

        // Xử lí hình ảnh
        colImage.setCellValueFactory(new PropertyValueFactory<>("image"));
        colImage.setCellFactory(column -> new TableCell<Product, String>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                if (empty || imagePath == null) {
                    setGraphic(null);
                } else {
                    try {
                        String fullPath = "/quanlibancoffee/client/image/" + imagePath;
                        Image img = new Image(getClass().getResourceAsStream(fullPath));
                        imageView.setImage(img);
                        imageView.setFitWidth(50);
                        imageView.setFitHeight(50);
                        // Bo góc ảnh
                        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(50, 50);
                        clip.setArcWidth(10);
                        clip.setArcHeight(10);
                        imageView.setClip(clip);

                        setGraphic(imageView);
                        setAlignment(javafx.geometry.Pos.CENTER);
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
            }
        });

        // Xử lý cột TRẠNG THÁI
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(column -> new TableCell<Product, Integer>() {
            @Override
            protected void updateItem(Integer status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label();
                    badge.setPrefWidth(85);
                    badge.setAlignment(javafx.geometry.Pos.CENTER);
                    badge.setPadding(new javafx.geometry.Insets(3, 5, 3, 5));

                    if (status == 1) {
                        badge.setText("Đang bán");
                        badge.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-background-radius: 10; -fx-font-weight: bold;");
                    } else {
                        badge.setText("Ngừng bán");
                        badge.setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-background-radius: 10; -fx-font-weight: bold;");
                    }
                    setGraphic(badge);
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });

        // 3. Xử lý cột CHỨC NĂNG
        colAction.setCellFactory(param -> new TableCell<Product, Void>() {
            private final Button btnEdit = new Button("Sửa");
            private final Button btnDelete = new Button("Xóa");

            {
                btnEdit.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
                btnDelete.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");

                btnEdit.setOnAction(event -> handleEdit(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(event -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox pane = new HBox(8, btnEdit, btnDelete);
                    pane.setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(pane);
                }
            }
        });
    }

    private void loadData() {
        productList = FXCollections.observableArrayList(productDAO.getAllProducts());
        tableProducts.setItems(productList);

        // Load danh sách loại vào ComboBox
        ObservableList<String> categories = FXCollections.observableArrayList("Tất cả");
        productList.stream().map(Product::getCategory).distinct().forEach(categories::add);
        cbCategoryFilter.setItems(categories);
    }

    // Logic tìm kiếm và lọc
    private void setupFilters() {
        FilteredList<Product> filteredData = new FilteredList<>(productList, b -> true);
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(product -> {
                if (newValue == null || newValue.isEmpty()) return true;
                return product.getName().toLowerCase().contains(newValue.toLowerCase());
            });
        });

        // Kết hợp với TableView
        tableProducts.setItems(filteredData);
    }

    @FXML
    private void handleAdd() {
        // Tạo Dialog nhập liệu đơn giản
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Thêm món mới");
        dialog.setHeaderText("Nhập theo định dạng: Tên món - Giá - Loại");
        dialog.setContentText("Ví dụ: Cà phê Muối - 30000 - Cà phê");

        dialog.showAndWait().ifPresent(input -> {
            try {
                String[] parts = input.split("-");
                String name = parts[0].trim();
                double price = Double.parseDouble(parts[1].trim());
                String category = parts[2].trim();

                try (Connection con = quanlibancoffee.server.utils.Database.connectDB()) {
                    String sql = "INSERT INTO products (name, price, category, status) VALUES (?, ?, ?, 1)";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, name);
                    ps.setDouble(2, price);
                    ps.setString(3, category);
                    ps.executeUpdate();

                    loadData();
                }
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Sai định dạng! Vui lòng nhập đúng: Tên - Giá - Loại").show();
            }
        });
    }

    private void handleEdit(Product p) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(p.getPrice()));
        dialog.setTitle("Sửa giá món");
        dialog.setHeaderText("Đang sửa giá cho món: " + p.getName());
        dialog.setContentText("Nhập giá mới:");

        dialog.showAndWait().ifPresent(newPrice -> {
            try {
                double price = Double.parseDouble(newPrice);
                try (Connection con = quanlibancoffee.server.utils.Database.connectDB()) {
                    String sql = "UPDATE products SET price = ? WHERE id = ?";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setDouble(1, price);
                    ps.setInt(2, p.getId());
                    ps.executeUpdate();

                    loadData();
                }
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Giá tiền phải là số!").show();
            }
        });
    }

    private void handleDelete(Product p) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText("Ngừng bán món: " + p.getName());
        alert.setContentText("Bạn có chắc chắn không?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection con = quanlibancoffee.server.utils.Database.connectDB()) {
                    String sql = "UPDATE products SET status = 0 WHERE id = ?";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setInt(1, p.getId());
                    ps.executeUpdate();

                    loadData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}