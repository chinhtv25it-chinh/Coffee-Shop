-- 1. XÓA DATABASE CŨ VÀ TẠO MỚI TỪ ĐẦU ĐỂ ĐỒNG BỘ
USE master;
GO
IF EXISTS (SELECT * FROM sys.databases WHERE name = 'coffee_db')
BEGIN
    ALTER DATABASE coffee_db SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE coffee_db;
END
GO

CREATE DATABASE coffee_db;
GO

USE coffee_db;
GO


-- 1. BẢNG TÀI KHOẢN

CREATE TABLE users (
    id INT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    role VARCHAR(20) DEFAULT 'User' NOT NULL -- Quyền: 'Admin' hoặc 'User' (Mặc định là User)
);
GO

-- Có sẵn 2 tài khoản mẫu
-- Chuỗi "8d969eef..." là mật khẩu đã được băm mã hóa SHA-256 đồng bộ với code Java
INSERT INTO users (username, password, email, role) VALUES
('chinh', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'chinh@gmail.com', 'Admin'),
('nhanvien', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'staff@gmail.com', 'User');
GO



-- 2. BẢNG SẢN PHẨM (THỰC ĐƠN MÓN ĂN)

CREATE TABLE products (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    image VARCHAR(255),
    category NVARCHAR(50),
    status BIT DEFAULT 1
);
GO

INSERT INTO products (name, price, image, category) VALUES
(N'Cà phê đen đá', 20000, 'coffee1.jpg', N'Cà phê'),
(N'Cà phê sữa', 25000, 'coffee2.jpg', N'Cà phê'),
(N'Capuchino', 30000, 'capuchino.jpg', N'Capuchino'),
(N'Bạc xỉu', 30000, 'bacxiu.jpg', N'Bạc xỉu'),
(N'Mocha', 35000, 'mocha.jpg', N'Mocha'),
(N'Caramel macchiato', 45000, 'caramel.jpg', N'Caramel macchiato'),
(N'Trà đào', 30000, 'tea1.jpg', N'Trà'),
(N'Trà chanh', 20000, 'tea2.jpg', N'Trà'),
(N'Trà gừng', 20000, 'tea3.jpg', N'Trà'),
(N'Trà đào cam sả', 30000, 'tea4.jpg', N'Trà'),
(N'Trà sữa truyền thống', 28000, 'trasua1.jpg', N'Trà sữa'),
(N'Trà sữa matcha', 30000, 'trasua2.jpg', N'Trà sữa'),
(N'Cà phê Muối', 30000, 'coffee3.jpg', N'Cà phê');
GO



-- 3. BẢNG SƠ ĐỒ BÀN COFFEE

CREATE TABLE coffee_tables (
    id INT IDENTITY(1,1) PRIMARY KEY,
    table_name NVARCHAR(50) NOT NULL,
    status NVARCHAR(20) DEFAULT N'Trống'
);
GO

INSERT INTO coffee_tables (table_name, status) VALUES
(N'Bàn 1', N'Trống'), (N'Bàn 2', N'Trống'), (N'Bàn 3', N'Trống'), (N'Bàn 4', N'Trống'),
(N'Bàn 5', N'Trống'), (N'Bàn 6', N'Trống'), (N'Bàn 7', N'Trống'), (N'Bàn 8', N'Trống'),
(N'Bàn 9', N'Trống'), (N'Bàn 10', N'Trống'), (N'Bàn 11', N'Trống'), (N'Bàn 12', N'Trống');
GO



-- 4. BẢNG HÓA ĐƠN (ORDERS)

CREATE TABLE orders (
    id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NULL,
    table_id INT NULL, -- Quản lý hóa đơn gắn với bàn nào
    order_date DATE DEFAULT GETDATE(),
    order_time TIME DEFAULT CONVERT(TIME, GETDATE()),
    total_amount DECIMAL(10,2) NOT NULL,
    payment_method NVARCHAR(50) DEFAULT N'Tiền mặt',
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (table_id) REFERENCES coffee_tables(id)
);
GO


-- 5. BẢNG CHI TIẾT HÓA ĐƠN (ORDER DETAILS)

CREATE TABLE order_details (
    id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
);
GO

-- Tạo một hóa đơn test mẫu ban đầu cho hệ thống ổn định
INSERT INTO orders (total_amount, payment_method, table_id) VALUES
(150000, N'Tiền mặt', 1);
GO



SELECT * FROM users;
SELECT * FROM products;
SELECT * FROM coffee_tables;
SELECT * FROM orders;