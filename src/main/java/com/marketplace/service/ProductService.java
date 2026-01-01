package com.marketplace.service;

import com.marketplace.dao.ProductDAO;
import com.marketplace.models.Enums;
import com.marketplace.models.Product;
import com.marketplace.models.Coupon;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * 商品服务：负责商品发布与查询等业务逻辑。
 */
public class ProductService {
    private final ProductDAO dao = new ProductDAO();
    private final com.marketplace.dao.CouponDAO couponDAO = new com.marketplace.dao.CouponDAO();

    /**
     * 发布商品（生成 id 并保存）
     */
    public Product publishProduct(String title, String desc, double price, int stock, String merchantId, String merchantPhone) throws SQLException {
        Product p = new Product("prod-" + UUID.randomUUID(), title, desc, price, stock, Enums.ProductStatus.PUBLISHED, merchantId, merchantPhone);
        dao.save(p);
        return p;
    }

    /**
     * 商家创建优惠券（骨架接口）
     */
    public void createCoupon(String merchantId, String code, double discount, String validUntil, int totalQty) throws SQLException {
        couponDAO.save(new Coupon(merchantId, code, discount, validUntil, totalQty));
    }

    /**
     * 用户领取优惠券（骨架实现）
     */
    public boolean claimCoupon(String couponId, String userPhone) throws SQLException {
        return couponDAO.claimCoupon(couponId, userPhone);
    }

    /**
     * 列出用户已领取的优惠券（骨架展示）
     */
    public java.util.List<String> listUserCoupons(String userPhone) throws SQLException {
        return couponDAO.listUserCoupons(userPhone);
    }

    /**
     * 标记用户优惠券已使用
     */
    public void markUserCouponUsed(String userCouponId) throws SQLException {
        couponDAO.markUserCouponUsed(userCouponId);
    }

    /**
     * 根据 user_coupon id 查询对应优惠金额
     */
    public Double getDiscountForUserCoupon(String userCouponId) throws SQLException {
        return couponDAO.getDiscountByUserCouponId(userCouponId);
    }

    /**
     * 列出商家发布的优惠券
     */
    public java.util.List<String> listCouponsForMerchant(String merchantId) throws SQLException {
        return couponDAO.listCouponsByMerchant(merchantId);
    }

    /**
     * 列出已发布的商品
     */
    public List<Product> listPublished() throws SQLException {
        return dao.listPublished();
    }

    /**
     * 根据关键字搜索已发布商品（简单的包含匹配，模拟语义搜索）
     */
    public List<Product> searchProducts(String keyword) throws SQLException {
        List<Product> all = dao.listPublished();
        List<Product> res = new java.util.ArrayList<>();
        if (keyword == null || keyword.isEmpty()) return all;
        String k = keyword.toLowerCase();
        for (Product p : all) {
            if ((p.getTitle() != null && p.getTitle().toLowerCase().contains(k)) ||
                    (p.getDescription() != null && p.getDescription().toLowerCase().contains(k))) {
                res.add(p);
            }
        }
        return res;
    }
}
