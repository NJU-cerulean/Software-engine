package com.marketplace;

import com.marketplace.service.OrderService;
import com.marketplace.service.CartService;
import com.marketplace.models.Order;

/**
 * 针对 OrderService / CartService 的简单 JBMC 驱动：
 * - 调用 OrderService.createOrder / createOrderWithCoupon
 * - 调用 CartService 的接口方法（目前会抛 UnsupportedOperationException）
 */
public class OrderCartDriver {
    public static void main(String[] args) throws Exception {
        OrderService orderService = new OrderService();
        CartService cartService = new CartService();

        // 创建一个不使用优惠券的订单
        Order o1 = orderService.createOrder("user1", "merchant1", 100.0, 0.0, 0.0);
        assert o1 != null;
        assert o1.getTotalAmount() >= 0;

        // 创建一个使用优惠券的订单
        try {
            Order o2 = orderService.createOrderWithCoupon("user2", "merchant2", 200.0, null);
            assert o2 != null;
            assert o2.getTotalAmount() >= 0;
        } catch (Exception ignored) {
            // DAO / 数据库可能抛出异常，这里不关心持久化细节
        }

        // 购物车接口（目前未实现，预期抛 UnsupportedOperationException）
        try {
            cartService.addToCart("userPhone", "prod-1", 1);
        } catch (UnsupportedOperationException e) {
            // 目前是未实现接口，JBMC 可以看到这里有受控异常
        }
    }
}
