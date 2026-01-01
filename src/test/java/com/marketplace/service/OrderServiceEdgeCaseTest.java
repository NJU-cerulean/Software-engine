package com.marketplace.service;

import com.marketplace.db.DBUtil;
import com.marketplace.dao.CouponDAO;
import com.marketplace.models.Coupon;
import com.marketplace.models.Order;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class OrderServiceEdgeCaseTest {

    @BeforeEach
    public void setup() throws SQLException {
        DBUtil.clearAllData();
        DBUtil.seedSampleData();
    }

    @AfterEach
    public void tearDown() throws SQLException {
        DBUtil.clearAllData();
    }

    @Test
    public void testCreateOrderWithCoupon_discount_exceeds_total_should_not_be_negative() throws SQLException {
        // 插入一个折扣远大于订单总额的优惠券
        Coupon big = new Coupon("m1", "BIG_DISCOUNT", 1000.0, "2099-01-01", 5);
        CouponDAO cdao = new CouponDAO();
        cdao.save(big);

        // 手动插入 user_coupons，使得我们知道 user_coupon id
        String userCouponId = "uc-large-1";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO user_coupons (id, coupon_id, user_phone, used) VALUES (?,?,?,0)")) {
            ps.setString(1, userCouponId);
            ps.setString(2, big.getCouponId());
            ps.setString(3, "10000000001");
            ps.executeUpdate();
        }

        OrderService svc = new OrderService();
        // 下单金额小于优惠券折扣
        Order o = svc.createOrderWithCoupon("u-100", "m1", 100.0, userCouponId);

        // 断言：订单金额不应为负（程序当前实现会导致负数，从而此断言会失败）
        assertTrue(o.getTotalAmount() >= 0, "期望订单金额非负，但实际为: " + o.getTotalAmount());
    }
    
    @Test
    public void testCreateOrderWithCoupon_null_couponId_uses_no_discount() throws SQLException {
        OrderService svc = new OrderService();
        Order o = svc.createOrderWithCoupon("u-100", "m1", 100.0, null);
        assertEquals(100.0, o.getTotalAmount());
        assertEquals(0.0, o.getDiscount());
    }

    @Test
    public void testCreateOrderWithCoupon_empty_couponId_uses_no_discount() throws SQLException {
        OrderService svc = new OrderService();
        Order o = svc.createOrderWithCoupon("u-100", "m1", 80.0, "");
        assertEquals(80.0, o.getTotalAmount());
        assertEquals(0.0, o.getDiscount());
    }

    @Test
    public void testCreateOrderWithCoupon_valid_coupon_applies_discount() throws SQLException {
        Coupon c = new Coupon("m1", "HALF", 50.0, "2099-12-31", 2);
        CouponDAO cdao = new CouponDAO();
        cdao.save(c);
        String ucid = "uc-valid-1";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO user_coupons (id, coupon_id, user_phone, used) VALUES (?,?,?,0)")) {
            ps.setString(1, ucid);
            ps.setString(2, c.getCouponId());
            ps.setString(3, "10000000001");
            ps.executeUpdate();
        }
        OrderService svc = new OrderService();
        Order o = svc.createOrderWithCoupon("u-100", "m1", 100.0, ucid);
        assertEquals(50.0, o.getTotalAmount());
        assertEquals(50.0, o.getDiscount());
    }

    @Test
    public void testCreateOrder_negative_discount_increases_total() throws SQLException {
        OrderService svc = new OrderService();
        Order o = svc.createOrder("u-200", "m1", 100.0, -10.0, 0.0);
        assertEquals(-10.0, o.getDiscount());
        assertEquals(110.0, o.calculateTotal());
    }

    @Test
    public void testCreateOrder_zero_total() throws SQLException {
        OrderService svc = new OrderService();
        Order o = svc.createOrder("u-300", "m1", 0.0, 0.0, 0.0);
        assertEquals(0.0, o.getTotalAmount());
        assertEquals(0.0, o.calculateTotal());
    }

    @Test
    public void testCreateOrder_payByPlatform_greater_than_total() throws SQLException {
        OrderService svc = new OrderService();
        Order o = svc.createOrder("u-400", "m1", 100.0, 10.0, 200.0);
        assertEquals(100.0, o.getTotalAmount());
        assertEquals(100.0 - 10.0 - 200.0, o.calculateTotal());
    }

    @Test
    public void testCreateOrderWithCoupon_nonexistent_userCoupon_treated_as_no_discount() throws SQLException {
        OrderService svc = new OrderService();
        Order o = svc.createOrderWithCoupon("u-500", "m1", 120.0, "not-exist-uc");
        assertEquals(120.0, o.getTotalAmount());
        assertEquals(0.0, o.getDiscount());
    }

    @Test
    public void testCreateOrder_large_values_handle() throws SQLException {
        OrderService svc = new OrderService();
        double large = 1e9;
        Order o = svc.createOrder("u-600", "m1", large, 1e8, 0.0);
        // Order.totalAmount stores the passed total; calculateTotal reflects discount
        assertEquals(large, o.getTotalAmount());
        assertEquals(large - 1e8, o.calculateTotal());
    }

    @Test
    public void testCreateOrderWithCoupon_marks_userCoupon_used() throws SQLException {
        Coupon c = new Coupon("m1", "SINGLE", 20.0, "2099-12-31", 2);
        CouponDAO cdao = new CouponDAO();
        cdao.save(c);
        String ucid = "uc-mark-used-1";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO user_coupons (id, coupon_id, user_phone, used) VALUES (?,?,?,0)")) {
            ps.setString(1, ucid);
            ps.setString(2, c.getCouponId());
            ps.setString(3, "10000000001");
            ps.executeUpdate();
        }
        OrderService svc = new OrderService();
        Order o = svc.createOrderWithCoupon("u-700", "m1", 50.0, ucid);
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement q = conn.prepareStatement("SELECT used FROM user_coupons WHERE id = ?")) {
            q.setString(1, ucid);
            try (java.sql.ResultSet rs = q.executeQuery()) {
                if (rs.next()) {
                    assertEquals(1, rs.getInt("used"));
                } else {
                    fail("user_coupon row missing");
                }
            }
        }
    }
}
