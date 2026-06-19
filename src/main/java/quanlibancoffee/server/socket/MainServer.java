package quanlibancoffee.server.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MainServer {
    private static final int PORT = 8888;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("☕ [SERVER] Coffee server đã khởi động thành công tại cổng: " + PORT);
            System.out.println("[SERVER] Đang chờ đợi các máy client kết nối...");

            while (true) {
                // Chấp nhận kết nối từ client
                Socket clientSocket = serverSocket.accept();
                System.out.println("\n🌐 [SERVER] Có thiết bị kết nối mới từ IP: " + clientSocket.getRemoteSocketAddress());

                // Kích hoạt ĐA LUỒNG (Thread) xử lý riêng cho client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("❌ [SERVER] Lỗi khởi động server: " + e.getMessage());
        }
    }
}