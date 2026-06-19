package quanlibancoffee.client.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import quanlibancoffee.shared.model.CoffeeTable;
import quanlibancoffee.shared.model.Product;
import quanlibancoffee.client.service.ClientService;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class BanHangController implements Initializable {
    @FXML private FlowPane flowCoffee;
    @FXML private FlowPane flowTea;
    @FXML private TableView<Product> tableOrder;
    @FXML private TableColumn<Product, String> colOrderName;
    @FXML private TableColumn<Product, Double> colOrderPrice;

    // Hành động tăng giảm xóa trong giỏ hàng
    private TableColumn<Product, Void> colAction;

    @FXML private Label lblTotal;
    @FXML private TextField txtChoice;
    @FXML private FlowPane flowTables;
    @FXML private Label lblSelectedTable;

    // Các điều hướng phương thức thanh toán
    @FXML
    private RadioButton radTienMat;
    @FXML
    private RadioButton radChuyenKhoan;
    @FXML
    private ToggleGroup paymentGroup;

    private CoffeeTable selectedTable = null;
    private List<Product> allProducts = new ArrayList<>();
    private ObservableList<Product> orderItems = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colOrderName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colOrderPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        // Khởi tạo và cấu hình cột Action (+ / - / Xóa)
        initActionColumn();
        tableOrder.setItems(orderItems);

        paymentGroup = new ToggleGroup();
        if (radTienMat != null && radChuyenKhoan != null) {
            radTienMat.setToggleGroup(paymentGroup);
            radChuyenKhoan.setToggleGroup(paymentGroup);
            radTienMat.setSelected(true); // Mặc định chọn tiền mặt lúc mở app
        } else {
            System.out.println("⚠️ Cảnh báo: Chưa liên kết radTienMat hoặc radChuyenKhoan với FXML!");
        }

        // Nạp dữ liệu hoàn toàn qua mạng Socket
        loadTablesFromServer();
        loadMenuFromServer();

        txtChoice.textProperty().addListener((observable, oldValue, newValue) -> {
            filterMenu(newValue);
        });
    }

    // Tạo cột tính năng Thêm/Bớt/Xóa
    private void initActionColumn() {
        colAction = new TableColumn<>("Hành động");
        colAction.setPrefWidth(110);

        Callback<TableColumn<Product, Void>, TableCell<Product, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Product, Void> call(final TableColumn<Product, Void> param) {
                return new TableCell<>() {
                    private final Button btnPlus = new Button("+");
                    private final Button btnMinus = new Button("-");
                    private final Button btnDelete = new Button("Xóa");
                    private final HBox pane = new HBox(4, btnPlus, btnMinus, btnDelete);

                    {
                        btnPlus.getStyleClass().add("btn-plus");
                        btnMinus.getStyleClass().add("btn-minus");
                        btnDelete.getStyleClass().add("btn-delete");

                        pane.setAlignment(Pos.CENTER);

                        btnPlus.setOnAction(e -> {
                            Product p = getTableView().getItems().get(getIndex());
                            orderItems.add(p);
                            calculateTotal();
                        });

                        btnMinus.setOnAction(e -> {
                            Product p = getTableView().getItems().get(getIndex());
                            orderItems.remove(p); // Xóa bớt 1 item khỏi danh sách
                            calculateTotal();
                        });

                        btnDelete.setOnAction(e -> {
                            Product p = getTableView().getItems().get(getIndex());
                            orderItems.removeIf(item -> item.getId() == p.getId()); // Xóa sạch món này
                            calculateTotal();
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) setGraphic(null);
                        else setGraphic(pane);
                    }
                };
            }
        };
        colAction.setCellFactory(cellFactory);
        tableOrder.getColumns().add(colAction);
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
        if (selectedTable == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một bàn trên sơ đồ trước khi bấm thanh toán!");
            return;
        }
        if (orderItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Đơn hàng đang trống! Vui lòng chọn món trước khi thanh toán.");
            return;
        }


        String paymentMethod = "Tiền mặt";
        if (paymentGroup.getSelectedToggle() != null) {
            RadioButton selectedRadio = (RadioButton) paymentGroup.getSelectedToggle();
            // Nếu nút đang chọn có chứa chữ "Chuyển khoản" hoặc "Banking"
            if (selectedRadio.getText().contains("Chuyển khoản") || selectedRadio.getText().contains("Banking")) {
                paymentMethod = "Chuyển khoản";
            }
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận thanh toán");
        confirm.setHeaderText("Thanh toán cho " + selectedTable.getTableName());
        confirm.setContentText("Tổng tiền: " + lblTotal.getText() + "\nHình thức: " + paymentMethod + "\nBạn có chắc chắn muốn thanh toán và in hóa đơn?");

        final String finalPaymentMethod = paymentMethod;
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if ("Chuyển khoản".equals(finalPaymentMethod)) {
                    double totalAmount = parsePrice(lblTotal.getText());
                    hienThiPopupQR(totalAmount, selectedTable.getTableName());
                } else {
                    sendOrderToServer(finalPaymentMethod);
                }
            }
        });
    }

    private void sendOrderToServer(String paymentMethod) {
        try {
            double totalAmount = parsePrice(lblTotal.getText());

            java.util.Map<Integer, Long> productCounts = orderItems.stream()
                    .collect(java.util.stream.Collectors.groupingBy(Product::getId, java.util.stream.Collectors.counting()));

            StringBuilder detailsBuilder = new StringBuilder();
            List<Product> distinctProducts = orderItems.stream().distinct().collect(Collectors.toList());
            for (int i = 0; i < distinctProducts.size(); i++) {
                Product p = distinctProducts.get(i);
                detailsBuilder.append(p.getId()).append(",").append(productCounts.get(p.getId())).append(",").append(p.getPrice());
                if (i < distinctProducts.size() - 1) {
                    detailsBuilder.append("|");
                }
            }

            // Gửi thêm thông tin hình thức thanh toán sang server: CHECKOUT;tableId;totalAmount;details;paymentMethod
            String requestStr = "CHECKOUT;" + selectedTable.getId() + ";" + totalAmount + ";" + detailsBuilder.toString() + ";" + paymentMethod;
            String response = ClientService.sendRequest(requestStr);

            if ("CHECKOUT_SUCCESS".equals(response)) {
                // Tiến hành xuất/in hóa đơn ra file text lập tức
                exportInvoiceText(paymentMethod);

                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thanh toán thành công! Hóa đơn đã được xuất.");
                orderItems.clear();
                calculateTotal();
                loadTablesFromServer();
            } else {
                showAlert(Alert.AlertType.ERROR, "Thất bại", "Lỗi từ Server: " + response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể kết nối đến máy chủ!");
        }
    }

    // Hàm tạo và xuất hóa đơn ra file text (.txt) đặt tại máy Client
    private void exportInvoiceText(String paymentMethod) {
        PrintWriter out = null;
        try {
            // Tạo tên file an toàn, không chứa ký tự đặc biệt nguy hiểm
            String safeTableName = (selectedTable != null) ? selectedTable.getTableName().replaceAll("[^a-zA-Z0-9_\\s]", "").trim() : "Ban";
            safeTableName = safeTableName.replace(" ", "_");

            String fileName = "Hóa Đơn_" + safeTableName + "_" + System.currentTimeMillis() + ".txt";
            File file = new File(fileName);
            out = new PrintWriter(file);

            out.println("=========================================");
            out.println("               COFFEE SHOP               ");
            out.println("        Hệ thống quản lý bán hàng        ");
            out.println("=========================================");
            out.println("Thời gian: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            out.println("Vị trí:    " + ((selectedTable != null) ? selectedTable.getTableName() : "Chưa chọn bàn"));
            out.println("Hình thức: " + paymentMethod);
            out.println("-----------------------------------------");
            out.println(String.format("%-18s %-5s %-14s", "Tên món", "SL", "Thành tiền"));
            out.println("-----------------------------------------");

            // Đếm số lượng món bằng vòng lặp cơ bản (tránh xung đột phân tách Stream API)
            java.util.Map<Integer, Integer> productCounts = new java.util.HashMap<>();
            java.util.Map<Integer, Product> productMap = new java.util.HashMap<>();

            for (Product p : orderItems) {
                productCounts.put(p.getId(), productCounts.getOrDefault(p.getId(), 0) + 1);
                productMap.put(p.getId(), p);
            }

            // Lấy chuỗi định dạng tiền tổng cộng sạch
            String totalAmountStr = lblTotal.getText();

            // Duyệt qua danh sách món đã gộp để ghi vào file
            for (Integer prodId : productCounts.keySet()) {
                Product p = productMap.get(prodId);
                int qty = productCounts.get(prodId);
                double subTotal = p.getPrice() * qty;

                // Định dạng chuỗi tiền tệ cho từng món
                String subTotalStr = String.format("%,.0fđ", subTotal);

                out.println(String.format("%-18s %-5d %-14s", p.getName(), qty, subTotalStr));
            }

            out.println("-----------------------------------------");
            out.println("TỔNG CỘNG:                " + totalAmountStr);
            out.println("=========================================");
            out.println("     CẢM ƠN QÚY KHÁCH - HẸN GẶP LẠI      ");
            out.println("=========================================");

            out.flush(); // Ép dữ liệu ghi hoàn toàn xuống ổ đĩa
            System.out.println("✅ Đã xuất hóa đơn thành công ra file: " + file.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("❌ Lỗi in hóa đơn chi tiết: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }


    private void loadTablesFromServer() {
        flowTables.getChildren().clear();
        try {
            String response = ClientService.sendRequest("GET_ALL_TABLES");
            if (response != null && response.startsWith("TABLE_LIST_RES")) {
                String[] parts = response.split(";");
                if (parts.length > 1) {
                    String[] rawTables = parts[1].split("\\|");
                    for (String raw : rawTables) {
                        String[] details = raw.split(",");
                        if (details.length >= 3) {
                            CoffeeTable t = new CoffeeTable(Integer.parseInt(details[0]), details[1], details[2]);
                            flowTables.getChildren().add(createTableCard(t));
                        }
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private VBox createTableCard(CoffeeTable t) {
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
        name.getStyleClass().add("table-title");

        Label status = new Label(t.getStatus());
        status.getStyleClass().add("table-status");

        tableCard.getChildren().addAll(name, status);

        tableCard.setOnMouseClicked(e -> {
            selectedTable = t;
            lblSelectedTable.setText("📍 Đang chọn: " + t.getTableName());

            if (t.getStatus().equals("Có khách")) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Chuyển trạng thái bàn này thành 'Trống'?", ButtonType.YES, ButtonType.NO);
                alert.showAndWait().ifPresent(res -> {
                    if (res == ButtonType.YES) {
                        String rep = ClientService.sendRequest("UPDATE_TABLE_STATUS;" + t.getId() + ";Trống");
                        if ("UPDATE_SUCCESS".equals(rep)) {
                            loadTablesFromServer();
                            orderItems.clear();
                            calculateTotal();
                            selectedTable = null;
                            lblSelectedTable.setText("📍 Đang chọn: Chưa chọn bàn");
                        }
                    }
                });
            }
        });
        return tableCard;
    }

    private void loadMenuFromServer() {
        allProducts.clear();
        try {
            String response = ClientService.sendRequest("GET_ALL_PRODUCTS");
            if (response != null && response.startsWith("PRODUCT_LIST_RES")) {
                String[] parts = response.split(";");
                if (parts.length > 1) {
                    String[] rawProducts = parts[1].split("\\|");
                    for (String raw : rawProducts) {
                        String[] details = raw.split(",");
                        if (details.length >= 5) {
                            allProducts.add(new Product(Integer.parseInt(details[0]), details[1], Double.parseDouble(details[2]), details[3], details[4], 1));
                        }
                    }
                }
                renderMenu(allProducts);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void renderMenu(List<Product> list) {
        flowCoffee.getChildren().clear();
        flowTea.getChildren().clear();
        for (Product p : list) {
            VBox productCard = createProductCard(p);
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
        card.setPrefWidth(140); card.setMinHeight(180);

        // ĐÃ SỬA: Đưa toàn bộ hiệu ứng đổ bóng, bo góc, đổi màu hover của Card Sản phẩm vào file CSS
        card.getStyleClass().add("product-card");

        ImageView imageView = new ImageView();
        try {
            Image img = new Image(getClass().getResourceAsStream("/quanlibancoffee/client/image/" + p.getImage()));
            imageView.setImage(img);
        } catch (Exception e) {}
        imageView.setFitWidth(85); imageView.setFitHeight(85); imageView.setPreserveRatio(true);

        Label lblName = new Label(p.getName().toUpperCase());
        lblName.getStyleClass().add("product-name"); // ĐÃ SỬA: Đưa font chữ, màu sắc chữ vào file CSS
        lblName.setWrapText(true); lblName.setAlignment(Pos.CENTER);

        Label lblPrice = new Label(String.format("%,.0f đ", p.getPrice()));
        lblPrice.getStyleClass().add("product-price"); // ĐÃ SỬA: Đưa màu sắc và chữ đậm vào file CSS

        card.getChildren().addAll(imageView, lblName, lblPrice);

        card.setOnMouseClicked(e -> {
            if (selectedTable == null) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn bàn trước khi chọn món!");
                return;
            }
            orderItems.add(p);
            calculateTotal();
            if (selectedTable.getStatus().equals("Trống")) {
                String rep = ClientService.sendRequest("UPDATE_TABLE_STATUS;" + selectedTable.getId() + ";Có khách");
                if ("UPDATE_SUCCESS".equals(rep)) {
                    loadTablesFromServer();
                    selectedTable.setStatus("Có khách");
                }
            }
        });
        return card;
    }

    private void filterMenu(String keyword) {
        if (keyword == null || keyword.isEmpty()) { renderMenu(allProducts); return; }
        String lowerKey = keyword.toLowerCase();
        renderMenu(allProducts.stream().filter(p -> p.getName().toLowerCase().contains(lowerKey)).collect(Collectors.toList()));
    }

    private double parsePrice(String priceStr) {
        try { return Double.parseDouble(priceStr.replaceAll("[^0-9]", "")); } catch (Exception e) { return 0.0; }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait();
    }

    private void hienThiPopupQR(double executionAmount, String tableName) {
        String bankId = "MB";
        String accountNo = "0886795905";
        String accountName = "TRAN VAN CHINH";

        String safeTableName = (tableName != null) ? tableName.replaceAll("[^a-zA-Z0-9_\\s]", "").trim() : "Ban";
        String addInfo = "Thanh%20Toan%20" + safeTableName.replace(" ", "%20");

        String qrUrl = String.format("https://img.vietqr.io/image/%s-%s-qr_only.png?amount=%.0f&addInfo=%s&accountName=%s",
                bankId, accountNo, executionAmount, addInfo, accountName.replace(" ", "%20"));

        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("QUÉT MÃ QR THANH TOÁN BANKING");
        popupStage.setWidth(380);
        popupStage.setHeight(480);
        popupStage.setResizable(false);

        Label lblTitle = new Label("MỜI KHÁCH HÀNG QUÉT MÃ QR");
        lblTitle.getStyleClass().add("qr-title"); // ĐÃ SỬA: Chuyển sang dùng CSS Class

        Label lblAmount = new Label(String.format("Số tiền: %,.0f VNĐ", executionAmount));
        lblAmount.getStyleClass().add("qr-amount"); // ĐÃ SỬA: Chuyển sang dùng CSS Class

        ImageView qrImageView = new ImageView();
        qrImageView.setFitWidth(280);
        qrImageView.setFitHeight(280);
        qrImageView.setPreserveRatio(true);

        try {
            Image qrImage = new Image(qrUrl, true);
            qrImageView.setImage(qrImage);
        } catch (Exception e) {
            System.err.println("❌ Lỗi tải mã QR: " + e.getMessage());
        }

        Button btnSuccess = new Button("Hoàn thành thanh toán");
        btnSuccess.getStyleClass().add("btn-success"); // ĐÃ SỬA: Chuyển sang dùng CSS Class

        btnSuccess.setOnAction(e -> {
            popupStage.close();
            sendOrderToServer("Chuyển khoản");
        });

        VBox layout = new VBox(12);
        layout.setPadding(new Insets(15));
        layout.setAlignment(Pos.CENTER);
        layout.getStyleClass().add("qr-popup-layout"); // ĐÃ SỬA: Chuyển màu nền kem vào CSS Class
        layout.getChildren().addAll(lblTitle, lblAmount, qrImageView, btnSuccess);

        Scene scene = new Scene(layout);
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }
}