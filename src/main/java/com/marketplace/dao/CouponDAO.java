package com.marketplace.dao;

import com.marketplace.db.DBUtil;
import com.marketplace.models.Coupon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 优惠券 DAO：负责 coupons 与 user_coupons 的简单持久化操作（骨架实现）
 */
public class CouponDAO {

    /**
     * 保存优惠券定义到数据库（若已存在则忽略）
     */
    public void save(Coupon c) throws SQLException {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT OR IGNORE INTO coupons (id, merchant_id, code, discount, valid_until, total_qty, claimed_qty) VALUES (?, ?, ?, ?, ?, ?, ?)") ) {
            ps.setString(1, c.getCouponId());
            ps.setString(2, c.getMerchantId());
            ps.setString(3, c.getCode());
            ps.setDouble(4, c.getDiscount());
            ps.setString(5, c.getValidUntil());
            ps.setInt(6, c.getTotalQty());
            ps.setInt(7, c.getClaimedQty());
            ps.executeUpdate();
        }
    }

    /**
     * 尝试领取优惠券（在事务中操作，防止并发超额领取）
     * @return true 如果领取成功
     */
    public boolean claimCoupon(String couponId, String userPhone) throws SQLException {
        Connection conn = DBUtil.getConnection();
        try {
            // 使用事务保护：避免并发超领
            conn.setAutoCommit(false);

            // 检查剩余数量
            try (PreparedStatement q = conn.prepareStatement("SELECT total_qty, claimed_qty FROM coupons WHERE id = ?")){
                q.setString(1, couponId);
                try (ResultSet rs = q.executeQuery()){
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                    int total = rs.getInt("total_qty");
                    int claimed = rs.getInt("claimed_qty");
                    if (claimed >= total) {
                        conn.rollback();
                        return false;
                    }
                }
            }

            // 插入 user_coupons
            try (PreparedStatement ins = conn.prepareStatement("INSERT INTO user_coupons (id, coupon_id, user_phone, used) VALUES (?, ?, ?, 0)")){
                ins.setString(1, UUID.randomUUID().toString());
                ins.setString(2, couponId);
                ins.setString(3, userPhone);
                ins.executeUpdate();
            }

            // 更新 claimed_qty
            try (PreparedStatement up = conn.prepareStatement("UPDATE coupons SET claimed_qty = claimed_qty + 1 WHERE id = ?")){
                up.setString(1, couponId);
                up.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException ex) {
            try { conn.rollback(); } catch (SQLException ignore) {}
            throw ex;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignore) {}
            try { conn.close(); } catch (SQLException ignore) {}
        }
    }

    /**
     * 列出用户已领取但未使用的优惠券记录，返回格式: user_coupon_id | coupon_code | discount | used(0/1)
     */
    public List<String> listUserCoupons(String userPhone) throws SQLException {
        List<String> res = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT uc.id AS ucid, c.code, c.discount, uc.used FROM user_coupons uc JOIN coupons c ON uc.coupon_id = c.id WHERE uc.user_phone = ?")) {
            ps.setString(1, userPhone);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    res.add(rs.getString("ucid") + " | " + rs.getString("code") + " | " + rs.getDouble("discount") + " | " + rs.getInt("used"));
                }
            }
        }
        return res;
    }

    /**
     * 标记用户领取的优惠券为已使用
     */
    public void markUserCouponUsed(String userCouponId) throws SQLException {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE user_coupons SET used = 1 WHERE id = ?")) {
            ps.setString(1, userCouponId);
            ps.executeUpdate();
        }
    }

    /**
     * 根据 user_coupons.id 获取对应的 coupon 折扣
     */
    public Double getDiscountByUserCouponId(String userCouponId) throws SQLException {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT c.discount FROM user_coupons uc JOIN coupons c ON uc.coupon_id = c.id WHERE uc.id = ?")) {
            ps.setString(1, userCouponId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return null;
    }

    /**
     * 列出给定商家的可用优惠券（骨架）
     * 返回格式：coupon_id | code | discount | remain_qty
     */
    public List<String> listCouponsByMerchant(String merchantId) throws SQLException {
        List<String> res = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id, code, discount, (total_qty - claimed_qty) AS remain FROM coupons WHERE merchant_id = ?")) {
            ps.setString(1, merchantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    res.add(rs.getString("id") + " | " + rs.getString("code") + " | " + rs.getDouble("discount") + " | " + rs.getInt("remain"));
                }
            }
        }
        return res;
    }
}
