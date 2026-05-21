package quanlibancoffee.client.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import quanlibancoffee.shared.model.Order;
import quanlibancoffee.server.utils.Database;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class ThongKeController implements Initializable {
    @FXML private Label lblTodayRevenue;
    @FXML private Label lblTotalOrders;
    @FXML private BarChart<String, Number> chartRevenue;
    @FXML private TableView<Order> tableOrders;
    @FXML private TableColumn<Order, Integer> colId;
    @FXML private TableColumn<Order, String> colDate;
    @FXML private TableColumn<Order, String> colTime;
    @FXML private TableColumn<Order, Double> colTotal;
    @FXML private TableColumn<Order, String> colPayment;
    @FXML private DatePicker datePicker;

    private ObservableList<Order> listOrders = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadCardsData();
        loadChartData();
        loadTableData(null);
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("orderTime"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colPayment.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));

        // Format tiền tệ trong bảng cho đẹp
        colTotal.setCellFactory(tc -> new TableCell<Order, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) setText(null);
                else setText(String.format("%,.0f đ", price));
            }
        });
    }

    // 1. Nạp dữ liệu cho 3 thẻ màu trên cùng
    private void loadCardsData() {
        String sql = "SELECT SUM(total_amount) as TongTien, COUNT(*) as SoDon FROM orders WHERE order_date = CAST(GETDATE() AS DATE)";

        try (Connection con = Database.connectDB();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                double total = rs.getDouble("TongTien");
                int count = rs.getInt("SoDon");

                lblTodayRevenue.setText(String.format("%,.0f đ", total));
                lblTotalOrders.setText(String.valueOf(count));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 2. Vẽ biểu đồ cột 7 ngày gần nhất
    private void loadChartData() {
        chartRevenue.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu");

        // Query lấy doanh thu 7 ngày gần nhất
        String sql = "SELECT order_date, SUM(total_amount) as DoanhThu FROM orders " +
                "WHERE order_date >= DATEADD(day, -7, GETDATE()) " +
                "GROUP BY order_date ORDER BY order_date ASC";

        try (Connection con = Database.connectDB();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                // Đưa dữ liệu vào cột (Ngày, Tiền)
                series.getData().add(new XYChart.Data<>(rs.getString("order_date"), rs.getDouble("DoanhThu")));
            }
        } catch (Exception e) { e.printStackTrace(); }

        chartRevenue.getData().add(series);
    }

    // 3. Nạp dữ liệu bảng
    private void loadTableData(String dateFilter) {
        listOrders.clear();
        String sql = "SELECT * FROM orders";
        if (dateFilter != null) {
            sql += " WHERE order_date = '" + dateFilter + "'";
        }
        sql += " ORDER BY id DESC"; // Đơn mới nhất lên đầu

        try (Connection con = Database.connectDB();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Order order = new Order(
                        rs.getInt("id"),
                        rs.getString("order_date"), // Lưu ý Model Order phải nhận String hoặc Date
                        rs.getString("order_time"),
                        rs.getDouble("total_amount"),
                        rs.getString("payment_method")
                );
                listOrders.add(order);
            }
            tableOrders.setItems(listOrders);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void handleFilter(ActionEvent event) {
        if (datePicker.getValue() != null) {
            loadTableData(datePicker.getValue().toString());
        } else {
            loadTableData(null);
        }
    }
}