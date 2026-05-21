package quanlibancoffee.client.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import quanlibancoffee.shared.model.CoffeeTable;
import quanlibancoffee.shared.model.Product;
import quanlibancoffee.server.dao.ProductDAO;
import quanlibancoffee.server.dao.TableDAO;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class BanHangController implements Initializable {
    @FXML private FlowPane flowCoffee;
    @FXML private FlowPane flowTea;
    @FXML private TableView<Product> tableOrder;
    @FXML private TableColumn<Product, String> colOrderName;
    @FXML private TableColumn<Product, Double> colOrderPrice;
    @FXML private Label lblTotal;
    @FXML private TextField txtChoice;
    @FXML private FlowPane flowTables;
    @FXML private Label lblSelectedTable;
    private CoffeeTable selectedTable = null;
    private List<Product> allProducts;

    private ObservableList<Product> orderItems = FXCollections.observableArrayList();
    private ProductDAO productDAO = new ProductDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadTables();
        loadData();
        colOrderName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colOrderPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        tableOrder.setItems(orderItems);

        allProducts = productDAO.getAllProducts();
        renderMenu(allProducts);

        txtChoice.textProperty().addListener((observable, oldValue, newValue) -> {
            filterMenu(newValue);
        });
    }

    private void loadData() {
    }

    private void calculateTotal() {
        double total = 0;
        for (Product p : orderItems) {
            total += p.getPrice();
        }
        DecimalFormat df = new DecimalFormat("#,###đ");
        lblTotal.setText(df.format(total));
    }

    @FXML void handleClear() { orderItems.clear(); calculateTotal(); }

    @FXML
    private void handleCheckOut() {
        // 1. Kiểm tra xem người dùng đã chọn bàn chưa
        if (selectedTable == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Cảnh báo");
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng chọn một bàn trên sơ đồ trước khi bấm thanh toán!");
            alert.showAndWait();
            return;
        }

        // 2. Kiểm tra xem đơn hàng có món nào chưa
        if (orderItems.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Cảnh báo");
            alert.setHeaderText(null);
            alert.setContentText("Đơn hàng đang trống! Vui lòng chọn món trước khi thanh toán.");
            alert.showAndWait();
            return;
        }

        // 3. Hiển thị hộp thoại xác nhận thanh toán
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText("Thanh toán cho " + selectedTable.getTableName());
        confirm.setContentText("Tổng tiền: " + lblTotal.getText() + "\nBạn có chắc chắn muốn thanh toán?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                saveOrderToDatabase();
            }
        });

    }

    private void saveOrderToDatabase() {
        String insertOrderSQL = "INSERT INTO orders (order_date, order_time, total_amount, payment_method) VALUES (CAST(GETDATE() AS DATE), CAST(GETDATE() AS TIME), ?, ?)";
        String insertDetailSQL = "INSERT INTO order_details (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
        String updateTableSQL = "UPDATE coffee_tables SET status = N'Trống' WHERE id = ?";

        try (Connection con = quanlibancoffee.server.utils.Database.connectDB()) {
            con.setAutoCommit(false); // Bắt đầu Transaction để đảm bảo an toàn dữ liệu

            // 1. Lưu vào bảng orders
            int orderId = -1;
            double totalAmount = parsePrice(lblTotal.getText());

            try (PreparedStatement psOrder = con.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS)) {
                psOrder.setDouble(1, totalAmount);
                psOrder.setNString(2, "Tiền mặt");
                psOrder.executeUpdate();

                ResultSet rs = psOrder.getGeneratedKeys();
                if (rs.next()) orderId = rs.getInt(1);
            }

            // 2. Lưu chi tiết vào bảng order_details
            java.util.Map<Integer, Long> productCounts = orderItems.stream()
                    .collect(java.util.stream.Collectors.groupingBy(p -> p.getId(), java.util.stream.Collectors.counting()));

            try (PreparedStatement psDetail = con.prepareStatement(insertDetailSQL)) {
                for (Product p : orderItems.stream().distinct().collect(java.util.stream.Collectors.toList())) {
                    psDetail.setInt(1, orderId);
                    psDetail.setInt(2, p.getId());
                    psDetail.setLong(3, productCounts.get(p.getId()));
                    psDetail.setDouble(4, p.getPrice());
                    psDetail.addBatch();
                }
                psDetail.executeBatch();
            }

            // 3. Cập nhật trạng thái bàn về "Trống"
            try (PreparedStatement psTable = con.prepareStatement(updateTableSQL)) {
                psTable.setInt(1, selectedTable.getId());
                psTable.executeUpdate();
            }

            con.commit(); // Hoàn tất lưu dữ liệu dữ liệu an toàn

            // 4. Thông báo và làm mới giao diện thành công
            new Alert(Alert.AlertType.INFORMATION, "Thanh toán thành công!").show();
            orderItems.clear();
            calculateTotal();
            loadTables(); // Load lại sơ đồ bàn để cập nhật giao diện hiển thị bàn Trống
            selectedTable = null;
            lblSelectedTable.setText("📍 Đang chọn: Chưa chọn bàn");

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Lỗi khi lưu hóa đơn!").show();
        }
    }

    // Hàm phụ để chuyển đổi chuỗi "20.000đ" thành số 20000.0
    private double parsePrice(String priceStr) {
        try {
            return Double.parseDouble(priceStr.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0.0;
        }
    }

    private void renderMenu(List<Product> list) {
        flowCoffee.getChildren().clear();
        flowTea.getChildren().clear();

        for (Product p : list) {
            VBox productCard = createProductCard(p);

            // Phân loại dựa trên danh mục
            String cat = p.getCategory().toLowerCase();

            if (cat.contains("cà phê") || cat.contains("capuchino") || cat.contains("mocha") || cat.contains("bạc xỉu") || cat.contains("caramel")) {
                flowCoffee.getChildren().add(productCard);
            } else {
                flowTea.getChildren().add(productCard);
            }
        }
    }

    private VBox createProductCard(Product p) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(150);

        // 1. Hình ảnh
        ImageView imageView = new ImageView();
        try {
            Image img = new Image(getClass().getResourceAsStream("/quanlibancoffee/client/image/" + p.getImage()));
            imageView.setImage(img);
        } catch (Exception e) {
            // Khối catch xử lý ảnh trống
        }
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true);

        // 2. Tên đồ uống
        Label lblName = new Label(p.getName().toUpperCase());
        lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #4E342E;");
        lblName.setWrapText(true);
        lblName.setAlignment(Pos.CENTER);
        lblName.setMinHeight(35);

        // 3. Giá tiền
        Label lblPrice = new Label(String.format("%,.0f đ", p.getPrice()));
        lblPrice.setStyle("-fx-text-fill: #8B4513; -fx-font-weight: bold;");

        card.getChildren().addAll(imageView, lblName, lblPrice);

        // 4. Ô số lượng
        Spinner<Integer> qtySpinner = new Spinner<>(1, 100, 1);
        qtySpinner.setPrefWidth(70);
        qtySpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
        qtySpinner.setEditable(true);

        // Kiểu dáng card
        card.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 15; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); " +
                "-fx-cursor: hand;");

        // Hiệu ứng di chuột
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #fcfcfc; -fx-background-radius: 15; -fx-border-color: #6F4E37; -fx-border-radius: 15; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);"));

        // Sự kiện chọn món
        card.setOnMouseClicked(e -> {
            int quantity = qtySpinner.getValue();
            for (int i = 0; i < quantity; i++) {
                orderItems.add(p);
            }
            calculateTotal();
            qtySpinner.getValueFactory().setValue(1);
        });

        return card;
    }

    private void filterMenu(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            renderMenu(allProducts);
            return;
        }

        String lowerKey = keyword.toLowerCase();
        List<Product> filteredList = allProducts.stream()
                .filter(p -> p.getName().toLowerCase().contains(lowerKey))
                .collect(Collectors.toList());

        renderMenu(filteredList);
    }

    private void loadTables() {
        flowTables.getChildren().clear();
        List<CoffeeTable> tables = TableDAO.getAllTables();

        for (CoffeeTable t : tables) {
            VBox tableCard = new VBox(5);
            tableCard.setAlignment(Pos.CENTER);
            tableCard.setPrefSize(100, 100);
            tableCard.getStyleClass().add("table-item");

            if (t.getStatus().equals("Trống")) {
                tableCard.getStyleClass().add("table-empty");
            } else {
                tableCard.getStyleClass().add("table-occupied");
            }

            Label name = new Label(t.getTableName());
            name.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");

            Label status = new Label(t.getStatus());
            status.setStyle("-fx-font-size: 11px;");

            tableCard.getChildren().addAll(name, status);

            tableCard.setOnMouseClicked(e -> {
                selectedTable = t;
                lblSelectedTable.setText("📍 Đang chọn: " + t.getTableName());

                if (t.getStatus().equals("Có khách")) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Xác nhận trả bàn");
                    alert.setHeaderText("Khách tại " + t.getTableName() + " đã rời đi?");
                    alert.setContentText("Bạn có muốn dọn dẹp và chuyển trạng thái bàn này thành 'Trống' không?");

                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            TableDAO.updateTableStatus(t.getId(), "Trống");
                            loadTables();

                            if (orderItems != null) {
                                orderItems.clear();
                            }
                            calculateTotal();
                        }
                    });
                }
                flowTables.getChildren().forEach(n -> n.setStyle("-fx-border-width: 2;"));
                tableCard.setStyle("-fx-border-width: 4; -fx-border-color: #4E342E;");
            });

            flowTables.getChildren().add(tableCard);
        }
    }
}