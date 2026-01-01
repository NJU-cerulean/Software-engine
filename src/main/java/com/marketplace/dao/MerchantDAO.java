package com.marketplace.dao;

import com.marketplace.db.DBUtil;
import com.marketplace.models.Merchant;
import com.marketplace.models.Enums;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 商家数据访问对象：处理 merchants 表的持久化操作。
 */
public class MerchantDAO {
    /**
     * 保存商家及其密码（若已存在则忽略）
     */
    public void save(Merchant m, String password) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT OR IGNORE INTO merchants (id, shop_name, phone, password, contact_info, employee_count, identity) VALUES (?, ?, ?, ?, ?, ?, ?)") ) {
            ps.setString(1, m.getMerchantId());
            ps.setString(2, m.getShopName());
            ps.setString(3, m.getPhone());
            ps.setString(4, password);
            ps.setString(5, m.getContactInfo());
            ps.setInt(6, m.getEmployeeCount());
            ps.setString(7, m.getIdentity().name());
            ps.executeUpdate();
        }
    }

    /**
     * 根据手机号查找商家信息
     */
    public Merchant findByPhone(String phone) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, shop_name, phone, password, contact_info, identity FROM merchants WHERE phone = ?")) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Merchant m = new Merchant(rs.getString("shop_name"), rs.getString("phone"), rs.getString("contact_info"), Enums.IDENTITY.BOSS);
                    // 恢复数据库中的 id 与密码
                    m.setMerchantId(rs.getString("id"));
                    m.setPassword(rs.getString("password"));
                    return m;
                }
            }
        }
        return null;
    }

    /**
     * 获取商家密码散列（可能为明文或哈希），用于登录验证/迁移
     */
    public String getPasswordByPhone(String phone) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT password FROM merchants WHERE phone = ?")) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("password");
            }
        }
        return null;
    }

    /**
     * 根据手机号更新商家密码（可用于迁移明文到哈希）
     */
    public void updatePasswordByPhone(String phone, String hashed) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE merchants SET password = ? WHERE phone = ?")) {
            ps.setString(1, hashed);
            ps.setString(2, phone);
            ps.executeUpdate();
        }
    }
}
