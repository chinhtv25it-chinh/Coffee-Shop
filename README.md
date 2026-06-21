<div align="center">

# ☕ COFFEE SHOP
### 🚀 Hệ Thống Quản Lý & Bán Hàng Cà Phê

Thực hiện theo kiến trúc phân lớp chuẩn **MVC (Client)** và **3-Tier / DAO (Server)** giao tiếp qua **TCP/IP Socket**.

---

[![Java Version](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-0073B7?style=for-the-badge&logo=java&logoColor=white)](https://openjfx.io/)
[![SQL Server](https://img.shields.io/badge/SQL%20Server-2022-CC292B?style=for-the-badge&logo=microsoft-sql-server&logoColor=white)](https://www.microsoft.com/sql-server)
[![Maven](https://img.shields.io/badge/Maven-3.x-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Socket](https://img.shields.io/badge/Network-TCP%2FIP%20Socket-brightgreen?style=for-the-badge)]()

</div>

---

## 📌 1. Tổng Quan Kiến Trúc Dự Án

* 🏛️ **Client-Side:** Sử dụng JavaFX 21 để dựng giao diện tĩnh qua FXML. Code xử lý được chia tách làm 3 gói ngang hàng: `controller` (Bắt sự kiện UI), `service` (Xử lý logic và truyền tin Socket), và `view` (Giao diện).
* 🌐 **Server-Side:** Sử dụng kiến trúc đa luồng (`Multi-threading`) để chấp nhận nhiều kết nối đồng thời qua `ClientHandler`. Tầng `dao` đảm nhận nhiệm vụ truy vấn cơ sở dữ liệu SQL Server tách biệt hoàn toàn.

---

## 🛠️ 2. Công Nghệ & Thư Viện Sử Dụng

| Thành phần | Công nghệ / Thư viện | Vai trò trong hệ thống |
| :--- | :--- | :--- |
| **Core** | `Java SE 21` | Ngôn ngữ lập trình chính của toàn bộ hệ thống (LTS mới tối ưu hiệu năng). |
| **UI Framework** | `JavaFX 21` | Thiết kế giao diện người dùng mượt mà, trực quan và hiện đại. |
| **Database** | `Microsoft JDBC Driver for SQL Server` | Thư viện kết nối hệ quản trị cơ sở dữ liệu SQL Server. |
| **Network** | `Java Custom Socket` | Truyền nhận dữ liệu dạng chuỗi/đối tượng qua giao thức TCP/IP. |
| **Security** | `SHA-256 / Utils` | Mã hóa một chiều mật khẩu người dùng trước khi lưu xuống DB. |
| **Build Tool** | `Apache Maven` | Quản lý vòng đời dự án và tự động tải các dependencies. |

---

## ✨ 3. Các Chức Năng Đã Hoàn Thiện

### 🛒 Phân Hệ Nhân Viên (Bán Hàng)
* **Xác thực hệ thống:** Đăng nhập, bảo mật phiên làm việc cá nhân thông qua lớp `UserSession`.
* **Sơ đồ bàn trực quan:** Hiển thị danh sách bàn dạng lưới, thay đổi trạng thái màu theo thời gian thực (`Màu xanh`: Trống, `Màu đỏ`: Có khách).
* **Menu món ăn thông minh:** Hiển thị danh sách sản phẩm kèm hình ảnh minh họa, giá bán, hỗ trợ tìm kiếm món ăn theo từ khóa.
* **Hóa đơn động:** Thêm/bớt số lượng món trực tiếp trên bảng chọn, tự động tính tổng tiền ngay lập tức.
* **Thanh toán đa phương thức:** Hỗ trợ thanh toán qua `Tiền mặt` hoặc `Chuyển khoản (Banking)`, tích hợp cửa sổ Dialog xác nhận và tự động xuất hóa đơn cứng ra file định dạng `.txt` hệ thống.

### 👔 Phân Hệ Quản Lý (Admin)
* **Quản lý danh mục:** Thêm, sửa, xóa thông tin sản phẩm (món ăn) và sơ đồ danh sách bàn.
* **Quản lý tài khoản:** Cấp phát và phân quyền tài khoản cho nhân viên mới.
* **Thống kê doanh thu:** Xem báo cáo lịch sử giao dịch và biểu đồ doanh thu theo thời gian.

---

## 📦 4. Cấu Trúc Thư Mục Dự Án (Directory Tree)

```text
src/main/java/quanlibancoffee/
│
├── 📱 client/                      # PHÂN HỆ CLIENT (JavaFX App)
│   ├── controller/              # [C] Điều khiển UI, bắt sự kiện tương tác
│   ├── service/                 # [M] Xử lý nghiệp vụ logic, kết nối Socket
│   ├── view/                    # [V] Chứa giao diện tĩnh .fxml và file style.css
│   └── CoffeeApp.java           # Điểm khởi chạy (Main Class) ứng dụng Client
│
├── ⚙️ server/                      # PHÂN HỆ SERVER (Xử lý tập trung)
│   ├── dao/                     # Tầng làm việc trực tiếp với DB (Data Access Object)
│   ├── socket/                  # Lắng nghe kết nối mạng, xử lý đa luồng (Multi-Thread)
│   └── utils/                   # Bộ công cụ kết nối Database, mã hóa mật khẩu
│
└── 🔄 shared/                      # TÀI NGUYÊN DÙNG CHUNG
    └── model/                   # Định nghĩa các đối tượng thực thể (Product, Order,...)
```

---

## 💾 5. Hướng Dẫn Cài Đặt Môi Trường

* 🗄️ **Khởi tạo Cơ sở dữ liệu:** Mở phần mềm Microsoft SQL Server Management Studio (SSMS), chạy lệnh SQL `CREATE DATABASE quanlibancoffee;` rồi execute tệp script `.sql` đi kèm dự án để tự động tạo cấu trúc bảng hệ thống.
* 📝 **Cấu hình kết nối hệ thống:** Tìm đến tệp `Database.java` theo đường dẫn `src/main/java/quanlibancoffee/server/utils/Database.java`. Tiến hành thay đổi thông số tài khoản và mật khẩu SQL Server của máy bạn:
  ```java
  // Cấu hình URL chuẩn kết nối tới Microsoft SQL Server (Port mặc định: 1433)
  private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=quanlibancoffee;encrypt=true;trustServerCertificate=true;";
  private static final String USER = "sa";             // Tài khoản sa của SQL Server
  private static final String PASSWORD = "YOUR_PASSWORD"; // Mật khẩu sa của bạn


---

## 💻 6. Quy Trình Khởi Chạy Ứng Dụng
* ⚠️ **Lưu ý bắt buộc:** Vì hệ thống chạy theo mô hình Socket kết nối mạng, bạn phải kích hoạt Server trước, sau đó mới khởi động Client. Nếu chạy Client trước, ứng dụng sẽ lập tức crash do không tìm thấy cổng mạng kết nối.

* 📡 **Bước 1 - Chạy Server trung tâm**: Tìm đến lớp MainServer.java theo đường dẫn src/main/java/quanlibancoffee/server/socket/MainServer.java, click chuột phải và chọn Run 'MainServer.main()'. Server báo log đang lắng nghe kết nối là thành công.

* 📱 **Bước 2 - Chạy Client (Giao diện):** Tìm đến lớp CoffeeApp.java theo đường dẫn src/main/java/quanlibancoffee/client/CoffeeApp.java, click chuột phải và chọn Run 'CoffeeApp.main()'. Cửa sổ đăng nhập JavaFX sẽ tự động hiển thị.

---

## 🔑 7. Tài Khoản Kiểm Thử Hệ Thống
Hệ thống thực hiện phân quyền hạn thao tác dữ liệu và hiển thị thanh menu chức năng dựa trên vai trò của tài khoản ngay sau khi đăng nhập thành công:
* **Admin:** chinh, 123456. Được phép truy cập toàn bộ hệ thống: quản lý món, cập nhật sơ đồ bàn, cấp tài khoản nhân viên và xem thống kê doanh thu.
* **Nhanvien:** tranchinh, 1234567. Chỉ được thao tác nghiệp vụ tại quầy: xem sơ đồ bàn, gọi món, thay đổi trạng thái bàn và in hóa đơn bán hàng cho khách.
