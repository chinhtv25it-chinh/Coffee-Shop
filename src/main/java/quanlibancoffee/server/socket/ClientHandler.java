package quanlibancoffee.server.socket;

import java.io.*;
import java.net.Socket;
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

                    // Bạn có thể viết thêm các case chức năng khác tại đây (ví dụ: LOGIN, GET_PRODUCTS...)

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