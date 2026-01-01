package com.marketplace.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 数据库工具类：管理 SQLite 连接与初始化数据库表。
 */
public class DBUtil {
    private static final String DB_URL = "jdbc:sqlite:marketplace.db";

    static {
        try {
            initDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取数据库连接
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private static void initDatabase() throws SQLException {
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            // users
            st.executeUpdate("CREATE TABLE IF NOT EXISTS users (id TEXT PRIMARY KEY, username TEXT, phone TEXT UNIQUE, password TEXT, vip TEXT, login_count INTEGER, last_login TEXT)");
            // merchants (增加 password 字段用于认证)
            st.executeUpdate("CREATE TABLE IF NOT EXISTS merchants (id TEXT PRIMARY KEY, shop_name TEXT, phone TEXT UNIQUE, password TEXT, contact_info TEXT, employee_count INTEGER, identity TEXT)");
            // admins
            st.executeUpdate("CREATE TABLE IF NOT EXISTS admins (id TEXT PRIMARY KEY, username TEXT, password TEXT)");
            // products
            st.executeUpdate("CREATE TABLE IF NOT EXISTS products (id TEXT PRIMARY KEY, title TEXT, description TEXT, price REAL, stock INTEGER, status TEXT, merchant_id TEXT, merchant_phone TEXT)");
            // orders
            st.executeUpdate("CREATE TABLE IF NOT EXISTS orders (id TEXT PRIMARY KEY, user_id TEXT, merchant_id TEXT, total_amount REAL, discount REAL, pay_by_platform REAL, status TEXT, create_time TEXT)");
            // messages 表扩展：增加 is_read 字段用于未读标识（默认 0）
            st.executeUpdate("CREATE TABLE IF NOT EXISTS messages (id TEXT PRIMARY KEY, sender_id TEXT, receiver_id TEXT, content TEXT, timestamp TEXT, is_read INTEGER DEFAULT 0)");
            // 如果旧版本的数据库里 messages 表缺少 is_read 列，尝试添加（SQLite 在列已存在时会抛异常，忽略之）
            try {
                st.executeUpdate("ALTER TABLE messages ADD COLUMN is_read INTEGER DEFAULT 0");
            } catch (SQLException ignore) { /* column may already exist */ }
            // complaints
            st.executeUpdate("CREATE TABLE IF NOT EXISTS complaints (id TEXT PRIMARY KEY, user_id TEXT, target_id TEXT, type TEXT, status TEXT)");
            // promotions
            st.executeUpdate("CREATE TABLE IF NOT EXISTS promotions (id TEXT PRIMARY KEY, merchant_id TEXT, type TEXT, discount REAL, valid_until TEXT)");
            // coupons 表：商家创建的优惠券
            st.executeUpdate("CREATE TABLE IF NOT EXISTS coupons (id TEXT PRIMARY KEY, merchant_id TEXT, code TEXT, discount REAL, valid_until TEXT, total_qty INTEGER, claimed_qty INTEGER DEFAULT 0)");
            // 用户-优惠券关联
            st.executeUpdate("CREATE TABLE IF NOT EXISTS user_coupons (id TEXT PRIMARY KEY, coupon_id TEXT, user_phone TEXT, used INTEGER DEFAULT 0)");
            // banned phones
            st.executeUpdate("CREATE TABLE IF NOT EXISTS banned (phone TEXT PRIMARY KEY, reason TEXT)");
            // banned products (管理员封禁的商品记录)
            st.executeUpdate("CREATE TABLE IF NOT EXISTS banned_products (product_id TEXT PRIMARY KEY, reason TEXT)");

            // 确保 users 表包含 total_spent 字段（在已有数据库上尝试添加列，若已存在则忽略异常）
            try {
                st.executeUpdate("ALTER TABLE users ADD COLUMN total_spent REAL DEFAULT 0");
            } catch (SQLException ignore) { /* column may already exist */ }

            // seed admin (id=admin, password=123456)，管理员无法通过程序注册
            st.executeUpdate("INSERT OR IGNORE INTO admins (id, username, password) VALUES ('admin','admin','123456')");
        }
    }

    /**
     * 清空主要数据表（仅用于管理员清库功能，慎用）
     */
    public static void clearAllData() throws SQLException {
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            // 注意顺序以减少依赖冲突（本示例无外键约束）。使用 DELETE 保留表结构但清空所有数据。
            st.executeUpdate("DELETE FROM messages");
            st.executeUpdate("DELETE FROM orders");
            st.executeUpdate("DELETE FROM products");
            st.executeUpdate("DELETE FROM promotions");
            // coupons 和 user_coupons 同步清理
            st.executeUpdate("DELETE FROM user_coupons");
            st.executeUpdate("DELETE FROM coupons");
            st.executeUpdate("DELETE FROM banned");
            st.executeUpdate("DELETE FROM banned_products");
            st.executeUpdate("DELETE FROM complaints");
            st.executeUpdate("DELETE FROM users");
            st.executeUpdate("DELETE FROM merchants");
            // 同时清空管理员账号（如需保留请勿执行此行）
            st.executeUpdate("DELETE FROM admins");
        }
    }

    /**
     * 向数据库插入一些基础的商家与商品样例，管理员可选择是否加载。
     */
    public static void seedSampleData() throws SQLException {
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            // 先插入两个商家
            st.executeUpdate("INSERT OR IGNORE INTO merchants (id, shop_name, phone, password, contact_info, employee_count, identity) VALUES ('m1','示例商家A','10000000001','pwd','联系A',1,'BOSS')");
            st.executeUpdate("INSERT OR IGNORE INTO merchants (id, shop_name, phone, password, contact_info, employee_count, identity) VALUES ('m2','示例商家B','10000000002','pwd','联系B',1,'BOSS')");
            // 额外样例商家（5 个）
            st.executeUpdate("INSERT OR IGNORE INTO merchants (id, shop_name, phone, password, contact_info, employee_count, identity) VALUES ('m3','示例商家C','10000000003','pwd','联系C',2,'BOSS')");
            st.executeUpdate("INSERT OR IGNORE INTO merchants (id, shop_name, phone, password, contact_info, employee_count, identity) VALUES ('m4','示例商家D','10000000004','pwd','联系D',3,'BOSS')");
            st.executeUpdate("INSERT OR IGNORE INTO merchants (id, shop_name, phone, password, contact_info, employee_count, identity) VALUES ('m5','示例商家E','10000000005','pwd','联系E',2,'BOSS')");
            st.executeUpdate("INSERT OR IGNORE INTO merchants (id, shop_name, phone, password, contact_info, employee_count, identity) VALUES ('m6','示例商家F','10000000006','pwd','联系F',4,'BOSS')");
            st.executeUpdate("INSERT OR IGNORE INTO merchants (id, shop_name, phone, password, contact_info, employee_count, identity) VALUES ('m7','示例商家G','10000000007','pwd','联系G',1,'BOSS')");
            // 插入若干商品
            st.executeUpdate("INSERT OR IGNORE INTO products (id, title, description, price, stock, status, merchant_id, merchant_phone) VALUES ('p1','示例手机','性价比高的示例手机',1999.0,50,'PUBLISHED','m1','10000000001')");
            st.executeUpdate("INSERT OR IGNORE INTO products (id, title, description, price, stock, status, merchant_id, merchant_phone) VALUES ('p2','示例耳机','舒适无线耳机',299.0,120,'PUBLISHED','m1','10000000001')");
            st.executeUpdate("INSERT OR IGNORE INTO products (id, title, description, price, stock, status, merchant_id, merchant_phone) VALUES ('p3','示例笔记本','办公学习用示例笔记本',6999.0,20,'PUBLISHED','m2','10000000002')");
            // 额外样例商品（10 个）
            st.executeUpdate("INSERT OR IGNORE INTO products (id, title, description, price, stock, status, merchant_id, merchant_phone) VALUES ('p4','示例键盘','机械键盘示例',399.0,80,'PUBLISHED','m3','10000000003')");
            st.executeUpdate("INSERT OR IGNORE INTO products (id, title, description, price, stock, status, merchant_id, merchant_phone) VALUES ('p5','示例鼠标','人体工学鼠标',199.0,150,'PUBLISHED','m3','10000000003')");
            st.executeUpdate("INSERT OR IGNORE INTO products (id, title, description, price, stock, status, merchant_id, merchant_phone) VALUES ('p6','示例显示器','24寸高清显示器',899.0,30,'PUBLISHED','m4','10000000004')");
            st.executeUpdate("INSERT OR IGNORE INTO products (id, title, description, price, stock, status, merchant_id, merchant_phone) VALUES ('p7','示例移动电源','大容量移动电源',149.0,200,'PUBLISHED','m4','10000000004')");
            st.executeUpdate("INSERT OR IGNORE INTO products (id, title, description, price, stock, status, merchant_id, merchant_phone) VALUES ('p8','示例相机','入门级微单相机',2499.0,10,'PUBLISHED','m5','10000000005')");
            st.executeUpdate("INSERT OR IGNORE INTO products (id, title, description, price, stock, status, merchant_id, merchant_phone) VALUES ('p9','示例手表','智能手表示例',599.0,60,'PUBLISHED','m5','10000000005')");
            st.executeUpdate("INSERT OR IGNORE INTO products (id, title, description, price, stock, status, merchant_id, merchant_phone) VALUES ('p10','示例背包','轻便旅行背包',249.0,70,'PUBLISHED','m6','10000000006')");
            st.executeUpdate("INSERT OR IGNORE INTO products (id, title, description, price, stock, status, merchant_id, merchant_phone) VALUES ('p11','示例运动鞋','跑步运动鞋',499.0,40,'PUBLISHED','m6','10000000006')");
            st.executeUpdate("INSERT OR IGNORE INTO products (id, title, description, price, stock, status, merchant_id, merchant_phone) VALUES ('p12','示例书籍','Java 编程入门书',89.0,200,'PUBLISHED','m7','10000000007')");
            st.executeUpdate("INSERT OR IGNORE INTO products (id, title, description, price, stock, status, merchant_id, merchant_phone) VALUES ('p13','示例耳塞','降噪耳塞',59.0,300,'PUBLISHED','m7','10000000007')");
        }
    }
}
