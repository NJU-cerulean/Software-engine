package com.marketplace.service;

import com.marketplace.dao.MerchantDAO;
import com.marketplace.dao.UserDAO;
import com.marketplace.db.DBUtil;
import com.marketplace.models.Merchant;
import com.marketplace.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 认证服务：处理用户与商家的注册与登录逻辑。
 */
public class AuthService {
    private final UserDAO userDAO = new UserDAO();
    private final MerchantDAO merchantDAO = new MerchantDAO();
    private final AdminService adminService = new AdminService();

    /**
     * 注册普通用户，返回是否成功
     */
    public boolean registerUser(User u, String password) throws SQLException {
        // 若手机号在黑名单中，则禁止注册
        if (adminService.isBanned(u.getPhone())) return false;
        // 检查手机号是否已被注册，若已注册则提示并返回 false
        try (Connection c = DBUtil.getConnection();
             PreparedStatement check = c.prepareStatement("SELECT phone FROM users WHERE phone = ?")) {
            check.setString(1, u.getPhone());
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) return false; // 已存在
            }
        }
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO users (id, username, phone, password, vip, login_count, total_spent) VALUES (?, ?, ?, ?, ?, ?, ?)") ) {
            ps.setString(1, u.getUserId());
            ps.setString(2, u.getUsername());
            ps.setString(3, u.getPhone());
            ps.setString(4, password);
            ps.setString(5, "NORMAL");
            ps.setInt(6, 0);
            ps.setDouble(7, 0.0);
            int affected = ps.executeUpdate();
            return affected > 0;
        }
    }

    /**
     * 用户登录校验
     */
    public boolean loginUser(String phone, String password) throws SQLException {
        // 禁止被封禁的用户登录
        if (adminService.isBanned(phone)) return false;
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT password FROM users WHERE phone = ?")) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String stored = rs.getString(1);
                    if (stored == null) return false;
                    // 简单明文比对（按用户要求，禁用哈希）
                    if (stored.equals(password)) return true;
                }
            }
        }
        return false;
    }

    /**
     * 注册商家并持久化密码
     */
    public boolean registerMerchant(Merchant m, String password) throws SQLException {
        // 若手机号在黑名单中，则禁止注册
        if (adminService.isBanned(m.getPhone())) return false;
        // persist merchant with password
        MerchantDAO dao = new MerchantDAO();
        // NOTE: 使用明文存储密码（不安全，仅按用户要求保留简单检测）
        dao.save(m, password);
        return true;
    }

    /**
     * 商家登录校验
     */
    public boolean loginMerchant(String phone, String password) throws SQLException {
        // 禁止被封禁的商家登录
        if (adminService.isBanned(phone)) return false;
        MerchantDAO dao = new MerchantDAO();
        String stored = dao.getPasswordByPhone(phone);
        if (stored == null) return false;
        // 明文比较
        return stored.equals(password);
    }

    /**
     * 管理员登录校验（管理员账号由数据库预置，禁止在程序中注册）
     */
    public boolean loginAdmin(String username, String password) 
    {
        if(username.equals("admin") && password.equals("123456"))
        {
            return true;
        }
        return false;
    }
}
