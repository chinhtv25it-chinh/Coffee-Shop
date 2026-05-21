package quanlibancoffee.client.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import quanlibancoffee.server.utils.Database;
import quanlibancoffee.shared.model.OrderSummary;

public class TrangChuController implements Initializable {
    @FXML private Label lblTodayRevenue, lblTodayOrders, lblTopProduct;
    @FXML private TableView<OrderSummary> tableRecentOrders;
    @FXML private TableColumn<OrderSummary, String> colTime, colMethod;
    @FXML private TableColumn<OrderSummary, Double> colTotal;

    private final DecimalFormat df = new DecimalFormat("#,### đ");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Ánh xạ dữ liệu vào các cột của bảng
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colMethod.setCellValueFactory(new PropertyValueFactory<>("method"));

        // Định dạng tiền tệ cho cột Tổng tiền trực tiếp trên TableView giúp hiển thị đẹp mắt hơn
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colTotal.setCellFactory(column -> new TableCell<OrderSummary, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(df.format(item));
                }
            }
        });

        refreshDashboard();
    }

    @FXML
    public void refreshDashboard() {
        try (Connection con = Database.connectDB()) {
            updateCards(con);
            updateRecentTable(con);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateCards(Connection con) throws SQLException {
        // Doanh thu & Số đơn hàng của ngày hôm nay
        String sql = "SELECT SUM(total_amount), COUNT(*) FROM orders WHERE order_date = CONVERT(DATE, GETDATE())";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                double revenue = rs.getDouble(1);
                int totalOrders = rs.getInt(2);

                lblTodayRevenue.setText(df.format(revenue));
                lblTodayOrders.setText(String.valueOf(totalOrders));
            }
        }

        // Món chạy nhất trong ngày
        String sqlTop = "SELECT TOP 1 p.name FROM order_details od " +
                "JOIN products p ON od.product_id = p.id " +
                "JOIN orders o ON od.order_id = o.id " +
                "WHERE o.order_date = CONVERT(DATE, GETDATE()) " +
                "GROUP BY p.name ORDER BY SUM(od.quantity) DESC";

        try (Statement stTop = con.createStatement(); ResultSet rsTop = stTop.executeQuery(sqlTop)) {
            if (rsTop.next()) {
                lblTopProduct.setText(rsTop.getString(1).toUpperCase());
            } else {
                lblTopProduct.setText("CHƯA CÓ ĐƠN HÀNG");
            }
        }
    }

    private void updateRecentTable(Connection con) throws SQLException {
        ObservableList<OrderSummary> list = FXCollections.observableArrayList();
        // Lấy định dạng thời gian HH:mm cho đẹp mắt nếu muốn từ chuỗi thời gian
        String sql = "SELECT TOP 10 CONVERT(VARCHAR(5), order_time, 108), total_amount, payment_method FROM orders " +
                "WHERE order_date = CONVERT(DATE, GETDATE()) ORDER BY id DESC";

        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new OrderSummary(rs.getString(1), rs.getDouble(2), rs.getString(3)));
            }
        }
        tableRecentOrders.setItems(list);
    }
}