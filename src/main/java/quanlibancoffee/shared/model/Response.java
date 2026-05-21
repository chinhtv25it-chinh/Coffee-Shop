package quanlibancoffee.shared.model;

import java.io.Serializable;

public class Response implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success; // true nếu thành công, false nếu thất bại
    private String message;  // Tin nhắn phản hồi (Ví dụ: "Đăng nhập thành công")
    private Object data;     // Dữ liệu trả về (Ví dụ: Danh sách món ăn, thông tin hóa đơn)

    public Response(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Object getData() { return data; }
}