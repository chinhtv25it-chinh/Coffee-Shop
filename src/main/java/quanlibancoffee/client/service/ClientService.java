package quanlibancoffee.client.service;

import java.io.*;
import java.net.Socket;

public class ClientService {
    private static final String HOST = "localhost"; // Nếu chạy cùng máy, hoặc đổi thành IP máy server
    private static final int PORT = 8888;

    private static Socket socket;
    private static BufferedReader reader;
    private static PrintWriter writer;

    // Hàm thực hiện bắt tay kết nối lúc mở phần mềm
    public static void connect() {
        try {
            if (socket == null || socket.isClosed()) {
                socket = new Socket(HOST, PORT);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
                System.out.println("🔌 [CLIENT] Kết nối đến mạng server thành công!");
            }
        } catch (IOException e) {
            System.err.println("❌ [CLIENT] Không thể kết nối tới server. Hãy kiểm tra xem server đã bật chưa!");
        }
    }

    // Hàm đồng bộ gửi yêu cầu và nhận ngay kết quả phản hồi từ server
    public static synchronized String sendRequest(String request) {
        try {
            if (writer == null || socket == null || socket.isClosed()) {
                connect();
            }
            writer.println(request); // Đẩy chuỗi dữ liệu đi
            return reader.readLine(); // Chờ server xử lý xong và nhận kết quả dòng text trả về
        } catch (IOException e) {
            System.err.println("❌ [CLIENT] Lỗi truyền nhận dữ liệu qua socket: " + e.getMessage());
            return "ERROR;Lỗi đường truyền";
        }
    }
}