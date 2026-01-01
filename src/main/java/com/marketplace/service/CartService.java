package com.marketplace.service;

import java.sql.SQLException;
import java.util.List;

/**
 * 购物车服务接口（当前为占位/接口定义，具体持久化与结算逻辑尚未实现）
 * TODO:
 * - 实现持久化版本（carts/cart_items 表）或内存原型
 * - 考虑并发/库存预占（下单原子性）
 * - 支持优惠券在结算时自动核销与合并规则
 */
public class CartService {

   
    public void addToCart(String userPhone, String productId, int qty) throws SQLException {
        throw new UnsupportedOperationException("addToCart not implemented");
    }

    
    public void removeFromCart(String userPhone, String productId) throws SQLException {
        throw new UnsupportedOperationException("removeFromCart not implemented");
    }

    /**
     * 列出用户购物车项（接口）
     */
    // TODO: 实现 listCart
    public List<String> listCart(String userPhone) throws SQLException {
        throw new UnsupportedOperationException("listCart not implemented");
    }

    /**
     * 结算购物车（接口）
     */
    // TODO: 实现 checkout（需处理优惠券、库存与订单创建的一致性）
    public boolean checkout(String userPhone) throws SQLException {
        throw new UnsupportedOperationException("checkout not implemented");
    }
}
