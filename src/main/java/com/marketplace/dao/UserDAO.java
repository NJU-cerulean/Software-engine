package com.marketplace.dao;

import com.marketplace.db.DBUtil;
import com.marketplace.models.Enums;
import com.marketplace.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * 用户数据访问对象：负责 users 表的读写操作。
 */
public class UserDAO {
    /**
     * 保存用户记录（若已存在则忽略）
     */
    public void save(User u) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT OR IGNORE INTO users (id, username, phone, password, vip, login_count, last_login) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, u.getUserId());
            ps.setString(2, u.getUsername());
            ps.setString(3, u.getPhone());
            ps.setString(4, ""); // password persisted by auth service
            ps.setString(5, Enums.VIPLevel.NORMAL.name());
            ps.setInt(6, u.getLoginCount());
            ps.setString(7, null);
            ps.executeUpdate();
        }
    }

    /**
     * 根据手机号查找用户
     */
    public User findByPhone(String phone) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, username, phone, password FROM users WHERE phone = ?")) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User(rs.getString("username"), rs.getString("phone"), rs.getString("password"));
                    return u;
                }
            }
        }
        return null;
    }

    /**
     * 增加用户消费总额，并在达到阈值时更新 VIP 等级（阈值示例：>=30000 -> GOLD）
     */
    public void addSpentAndMaybeUpgrade(String phone, double amount) throws SQLException {
        try (Connection c = DBUtil.getConnection()) {
            // 读取当前 total_spent 与 vip
            double total = 0.0;
            try (PreparedStatement ps = c.prepareStatement("SELECT total_spent, vip FROM users WHERE phone = ?")) {
                ps.setString(1, phone);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        total = rs.getDouble("total_spent");
                    }
                }
            }
            total += amount;
            // 简单策略：>=30000 -> GOLD；>=10000 -> SILVER；>=3000 -> BRONZE
            Enums.VIPLevel newVip = Enums.VIPLevel.NORMAL;
            if (total >= 30000) newVip = Enums.VIPLevel.GOLD;
            else if (total >= 10000) newVip = Enums.VIPLevel.SILVER;
            else if (total >= 3000) newVip = Enums.VIPLevel.BRONZE;

            try (PreparedStatement ups = c.prepareStatement("UPDATE users SET total_spent = ?, vip = ? WHERE phone = ?")) {
                ups.setDouble(1, total);
                ups.setString(2, newVip.name());
                ups.setString(3, phone);
                ups.executeUpdate();
            }
        }
    }

    /**
     * 获取用户的 VIP 等级（字符串）和总消费（double），若不存在返回 null
     */
    public Map<String, Object> getVipAndTotalByPhone(String phone) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT vip, total_spent FROM users WHERE phone = ?")) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return java.util.Map.of("vip", rs.getString("vip"), "total_spent", rs.getDouble("total_spent"));
                }
            }
        }
        return null;
    }

    /**
     * 更新用户密码（用于将明文迁移为哈希）
     */
    public void updatePasswordByPhone(String phone, String hashed) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE users SET password = ? WHERE phone = ?")) {
            ps.setString(1, hashed);
            ps.setString(2, phone);
            ps.executeUpdate();
        }
    }

    /**
     * 根据用户 id 查找对应的 phone 字段，若找不到返回 null
     */
    public String findPhoneByUserId(String userId) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT phone FROM users WHERE id = ?")) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        }
        return null;
    }

    /**
     * 返回任意存在的用户手机号（用于回退策略），找不到返回 null
     */
    public String findAnyPhone() throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT phone FROM users LIMIT 1")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        }
        return null;
    }
}
