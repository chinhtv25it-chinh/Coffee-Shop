package quanlibancoffee.server.socket;

import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import quanlibancoffee.shared.model.CoffeeTable;
import quanlibancoffee.server.dao.TableDAO;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            String clientRequest;
            // Vòng lặp liên tục lắng nghe yêu cầu từ client
            while ((clientRequest = reader.readLine()) != null) {
                System.out.println("[SERVER] Đã nhận lệnh từ client: " + clientRequest);

                String[] tokens = clientRequest.split(";");
                String command = tokens[0];

                switch (command) {
                    case "GET_ALL_TABLES":
                        // Gọi lớp DAO ở server để lấy dữ liệu từ SQL server
                        List<CoffeeTable> tables = TableDAO.getAllTables();

                        // Đóng gói danh sách thành chuỗi: TABLE_LIST_RES;id,name,status|id,name,status
                        StringBuilder res = new StringBuilder("TABLE_LIST_RES;");
                        for (int i = 0; i < tables.size(); i++) {
                            CoffeeTable t = tables.get(i);
                            res.append(t.getId()).append(",")
                                    .append(t.getTableName()).append(",")
                                    .append(t.getStatus());
                            if (i < tables.size() - 1) {
                                res.append("|");
                            }
                        }
                        writer.println(res.toString()); // Gửi trả về client
                        break;

                    case "UPDATE_TABLE_STATUS":
                        // Cú pháp lệnh: UPDATE_TABLE_STATUS;id;status
                        int id = Integer.parseInt(tokens[1]);
                        String status = tokens[2];

                        // Thực thi câu lệnh SQL UPDATE chuẩn
                        TableDAO.updateTableStatus(id, status);
                        writer.println("UPDATE_SUCCESS");
                        break;

                    case "LOGIN": {
                        // Cú pháp Client gửi lên: LOGIN;username;password_da_bam
                        if (tokens.length >= 3) {
                            String uname = tokens[1];
                            String hashedPass = tokens[2];

                            // Câu lệnh SQL lấy ra vai trò (role) của user
                            String sql = "SELECT role FROM users WHERE username = ? AND password = ?";

                            try (Connection con = quanlibancoffee.server.utils.Database.connectDB();
                                 PreparedStatement ps = con.prepareStatement(sql)) {

                                ps.setString(1, uname);
                                ps.setString(2, hashedPass);

                                try (ResultSet rs = ps.executeQuery()) {
                                    if (rs.next()) {
                                        String role = rs.getString("role"); // Lấy ra 'Admin' hoặc 'User'

                                        // Trả về thành công KÈM THEO quyền hạn
                                        writer.println("LOGIN_SUCCESS;" + role);
                                        System.out.println("[SERVER] Người dùng [" + uname + "] đăng nhập thành công với quyền: " + role);
                                    } else {
                                        // Sai tài khoản hoặc mật khẩu
                                        writer.println("LOGIN_FAIL;Tài khoản hoặc mật khẩu không chính xác!");
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                writer.println("LOGIN_FAIL;Lỗi kết nối cơ sở dữ liệu phía máy chủ!");
                            }
                        } else {
                            writer.println("LOGIN_FAIL;Dữ liệu đăng nhập không hợp lệ!");
                        }
                        break;
                    }

                    /* =========================================================================
                       🆕 THÊM MỚI: CASE XỬ LÝ ĐĂNG KÝ TÀI KHOẢN QUA SOCKET (Đúng chuẩn cấu trúc hệ thống)
                       ========================================================================= */
                    case "REGISTER":
                        // Nhận chuỗi từ Client: REGISTER;username;email;password
                        if (tokens.length >= 4) {
                            String username = tokens[1];
                            String email = tokens[2];
                            String rawPassword = tokens[3];

                            // 💥 TIÊU CHÍ BẮT BUỘC: Mã hóa băm SHA-256 bảo mật mật khẩu trước khi lưu
                            String hashedPassword = quanlibancoffee.server.utils.PasswordUtil.hashPassword(rawPassword);

                            String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";

                            try (Connection con = quanlibancoffee.server.utils.Database.connectDB();
                                 PreparedStatement ps = con.prepareStatement(sql)) {

                                ps.setString(1, username);
                                ps.setString(2, email);
                                ps.setString(3, hashedPassword); // Ghi chuỗi bảo mật vào SQL
                                ps.executeUpdate();

                                // Trả tín hiệu thành công về Client
                                writer.println("REGISTER_SUCCESS");
                                System.out.println("[SERVER] Đăng ký thành công tài khoản mới: " + username);

                            } catch (Exception e) {
                                System.err.println("❌ Server gặp lỗi ghi DB đăng ký: " + e.getMessage());
                                writer.println("REGISTER_FAIL;Tên tài khoản hoặc Email này đã tồn tại trên hệ thống!");
                            }
                        } else {
                            writer.println("REGISTER_FAIL;Dữ liệu gửi từ máy trạm không hợp lệ!");
                        }
                        break;

                    case "GET_ALL_PRODUCTS":
                        try {
                            List<quanlibancoffee.shared.model.Product> products = quanlibancoffee.server.dao.ProductDAO.getAllProducts();
                            StringBuilder prodRes = new StringBuilder("PRODUCT_LIST_RES;");

                            for (int i = 0; i < products.size(); i++) {
                                quanlibancoffee.shared.model.Product p = products.get(i);
                                prodRes.append(p.getId()).append(",")
                                        .append(p.getName()).append(",")
                                        .append(p.getPrice()).append(",")
                                        .append(p.getImage()).append(",")
                                        .append(p.getCategory());

                                if (i < products.size() - 1) {
                                    prodRes.append("|");
                                }
                            }
                            writer.println(prodRes.toString());
                            System.out.println("[SERVER] Đã gửi danh sách thực đơn về cho Client.");
                        } catch (Exception e) {
                            writer.println("ERROR;Lỗi lấy danh sách món ăn từ Server");
                            e.printStackTrace();
                        }
                        break;

                    case "DELETE_PRODUCT":
                        if (tokens.length >= 2) {
                            try {
                                int prodId = Integer.parseInt(tokens[1]);
                                boolean isDeleted = quanlibancoffee.server.dao.ProductDAO.updateStatus(prodId, 0);

                                if (isDeleted) {
                                    writer.println("DELETE_PRODUCT_SUCCESS");
                                } else {
                                    writer.println("DELETE_PRODUCT_FAIL;Không tìm thấy món ăn hoặc lỗi kết nối!");
                                }
                            } catch (NumberFormatException e) {
                                writer.println("DELETE_PRODUCT_FAIL;Mã món ăn không hợp lệ!");
                            }
                        } else {
                            writer.println("DELETE_PRODUCT_FAIL;Thiếu tham số xóa!");
                        }
                        break;

                    case "CHECKOUT":
                        try {
                            if (tokens.length >= 5) {
                                int tableId = Integer.parseInt(tokens[1]);
                                double totalAmount = Double.parseDouble(tokens[2]);
                                String rawDetails = tokens[3];
                                String paymentMethod = tokens[4];

                                String insertOrderSQL = "INSERT INTO orders (order_date, order_time, total_amount, payment_method) VALUES (CAST(GETDATE() AS DATE), CAST(GETDATE() AS TIME), ?, ?)";
                                String insertDetailSQL = "INSERT INTO order_details (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";

                                try (Connection con = quanlibancoffee.server.utils.Database.connectDB()) {
                                    con.setAutoCommit(false);

                                    int orderId = -1;
                                    try (PreparedStatement psOrder = con.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS)) {
                                        psOrder.setDouble(1, totalAmount);
                                        psOrder.setNString(2, paymentMethod);
                                        psOrder.executeUpdate();

                                        try (ResultSet rs = psOrder.getGeneratedKeys()) {
                                            if (rs.next()) orderId = rs.getInt(1);
                                        }
                                    }

                                    try (PreparedStatement psDetail = con.prepareStatement(insertDetailSQL)) {
                                        String[] detailRows = rawDetails.split("\\|");
                                        for (String row : detailRows) {
                                            String[] item = row.split(",");
                                            if (item.length >= 3) {
                                                psDetail.setInt(1, orderId);
                                                psDetail.setInt(2, Integer.parseInt(item[0]));
                                                psDetail.setInt(3, Integer.parseInt(item[1]));
                                                psDetail.setDouble(4, Double.parseDouble(item[2]));
                                                psDetail.addBatch();
                                            }
                                        }
                                        psDetail.executeBatch();
                                    }

                                    con.commit();
                                    writer.println("CHECKOUT_SUCCESS");
                                    System.out.println("[SERVER] Đã lưu hóa đơn thành công với hình thức: " + paymentMethod);
                                }
                            } else {
                                writer.println("CHECKOUT_FAIL;Thiếu dữ liệu!");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            writer.println("CHECKOUT_FAIL;Lỗi xử lý tại hệ thống máy chủ!");
                        }
                        break;

                    case "FETCH_USERS": {
                        // Lấy toàn bộ danh sách tài khoản (Ẩn mật khẩu đi để bảo mật)
                        String sql = "SELECT username, email, role FROM users";
                        try (Connection con = quanlibancoffee.server.utils.Database.connectDB();
                             PreparedStatement ps = con.prepareStatement(sql);
                             ResultSet rs = ps.executeQuery()) {

                            StringBuilder sb = new StringBuilder("FETCH_USERS_SUCCESS");
                            while (rs.next()) {
                                sb.append(";").append(rs.getString("username"))
                                        .append(",").append(rs.getString("email"))
                                        .append(",").append(rs.getString("role"));
                            }
                            writer.println(sb.toString());
                        } catch (Exception e) {
                            writer.println("FETCH_USERS_FAIL;Lỗi nạp danh sách tài khoản!");
                        }
                        break;
                    }
                    case "ADD_USER": {
                        // Cú pháp: ADD_USER;username;email;password;role
                        if (tokens.length >= 5) {
                            String uname = tokens[1];
                            String email = tokens[2];
                            String pass = tokens[3]; // Lưu ý: Mật khẩu này Client nên băm SHA-256 trước khi gửi qua
                            String role = tokens[4];

                            String sql = "INSERT INTO users (username, email, password, role) VALUES (?, ?, ?, ?)";
                            try (Connection con = quanlibancoffee.server.utils.Database.connectDB();
                                 PreparedStatement ps = con.prepareStatement(sql)) {
                                ps.setString(1, uname);
                                ps.setString(2, email);
                                ps.setString(3, pass);
                                ps.setString(4, role);
                                ps.executeUpdate();
                                writer.println("ADD_USER_SUCCESS");
                            } catch (Exception e) {
                                writer.println("ADD_USER_FAIL;Tên tài khoản hoặc Email đã tồn tại!");
                            }
                        }
                        break;
                    }
                    case "UPDATE_USER": {
                        // Cú pháp: UPDATE_USER;username;email;role
                        if (tokens.length >= 4) {
                            String uname = tokens[1];
                            String email = tokens[2];
                            String role = tokens[3];

                            String sql = "UPDATE users SET email = ?, role = ? WHERE username = ?";
                            try (Connection con = quanlibancoffee.server.utils.Database.connectDB();
                                 PreparedStatement ps = con.prepareStatement(sql)) {
                                ps.setString(1, email);
                                ps.setString(2, role);
                                ps.setString(3, uname);
                                ps.executeUpdate();
                                writer.println("UPDATE_USER_SUCCESS");
                            } catch (Exception e) {
                                writer.println("UPDATE_USER_FAIL;Lỗi cập nhật tài khoản!");
                            }
                        }
                        break;
                    }
                    case "DELETE_USER": {
                        // Cú pháp: DELETE_USER;username
                        if (tokens.length >= 2) {
                            String uname = tokens[1];
                            String sql = "DELETE FROM users WHERE username = ?";
                            try (Connection con = quanlibancoffee.server.utils.Database.connectDB();
                                 PreparedStatement ps = con.prepareStatement(sql)) {
                                ps.setString(1, uname);
                                ps.executeUpdate();
                                writer.println("DELETE_USER_SUCCESS");
                            } catch (Exception e) {
                                writer.println("DELETE_USER_FAIL;Lỗi xóa tài khoản!");
                            }
                        }
                        break;
                    }

                    default:
                        writer.println("ERROR;Lệnh không hợp lệ");
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("[SERVER] Một máy client đã ngắt kết nối đường truyền.");
        } finally {
            try {
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}