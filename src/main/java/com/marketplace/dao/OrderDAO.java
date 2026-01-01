package com.marketplace.dao;

import com.marketplace.db.DBUtil;
import com.marketplace.models.Order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单数据访问对象：封装与 orders 表交互的 CRUD 操作。
 */
public class OrderDAO {
    /**
     * 保存或更新订单记录
     */
    public void save(Order o) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT OR REPLACE INTO orders (id, user_id, merchant_id, total_amount, discount, pay_by_platform, status, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, o.getOrderId());
            ps.setString(2, o.getUserId());
            ps.setString(3, o.getMerchantId());
            ps.setDouble(4, o.getTotalAmount());
            ps.setDouble(5, o.getDiscount());
            ps.setDouble(6, o.getPayByPlatform());
            ps.setString(7, o.getStatus().name());
            ps.setString(8, Long.toString(o.getCreateTime().getTime()));
            ps.executeUpdate();
        }
    }

    /**
     * 根据用户 ID 查询订单列表
     */
    public List<Order> findByUser(String userId) throws SQLException {
        List<Order> res = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, user_id, merchant_id, total_amount, discount, pay_by_platform, status FROM orders WHERE user_id = ?")) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    res.add(mapRow(rs));
                }
            }
        }
        return res;
    }

    /**
     * 将当前 ResultSet 行映射为 Order 对象，避免重复构造样板。
     */
    private Order mapRow(ResultSet rs) throws SQLException {
        return new Order(rs.getString("id"), rs.getString("user_id"), rs.getString("merchant_id"), rs.getDouble("total_amount"), rs.getDouble("discount"), rs.getDouble("pay_by_platform"));
    }
}
