package com.marketplace.service;

import com.marketplace.dao.OrderDAO;

import com.marketplace.dao.UserDAO;
import com.marketplace.models.Order;

import java.sql.SQLException;
import com.marketplace.dao.CouponDAO;
import java.util.List;
import java.util.UUID;

/**
 * 订单服务：负责创建订单与按用户查询订单等功能。
 */
public class OrderService {
    private final OrderDAO orderDAO = new OrderDAO();
    private final MessageService messageService = new MessageService();
    private final StatisticsService statisticsService = new StatisticsService();
    private final UserDAO userDAO = new UserDAO();

    /**
     * 创建新订单并保存
     */
    public Order createOrder(String userId, String merchantId, double totalAmount, double discount, double payByPlatform) throws SQLException {
        Order o = new Order("order-" + UUID.randomUUID(), userId, merchantId, totalAmount, discount, payByPlatform);
        // 故意加入的不变量检查：折扣不应大于总金额（用于 JBMC 检测）
        //assert discount <= totalAmount;
        orderDAO.save(o);
        // 下单后发送通知并记录统计
    messageService.sendToMerchant(merchantId, "新订单: " + o.getOrderId());
    messageService.notifyUser(userId, "订单已创建: " + o.getOrderId());
        statisticsService.recordOrder(o);
        // 更新用户消费并根据阈值提升 VIP
        try {
            userDAO.addSpentAndMaybeUpgrade(userId, totalAmount);
        } catch (Exception ignored) { }
        return o;
    }

    /**
     * 创建订单并可使用用户优惠券（userCouponId 可为 null）
     */
    public Order createOrderWithCoupon(String userId, String merchantId, double totalAmount, String userCouponId) throws SQLException {
        double discount = 0.0;
        if (userCouponId != null && !userCouponId.isEmpty()) {
            CouponDAO couponDAO = new CouponDAO();
            Double d = couponDAO.getDiscountByUserCouponId(userCouponId);
            if (d != null) discount = d;
            couponDAO.markUserCouponUsed(userCouponId);
        }
        return createOrder(userId, merchantId, totalAmount - discount, discount, 0.0);
    }

    /**
     * 根据用户 ID 列出订单
     */
    public List<Order> listByUser(String userId) throws SQLException {
        return orderDAO.findByUser(userId);
    }
}
