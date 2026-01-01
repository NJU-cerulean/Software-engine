package com.marketplace.models;

import java.util.UUID;


public class Coupon {
    private String couponId;
    private String merchantId;
    private String code;
    private double discount;
    private String validUntil;
    private int totalQty;
    private int claimedQty;

    public Coupon(String merchantId, String code, double discount, String validUntil, int totalQty) {
        this.couponId = "coupon-" + UUID.randomUUID();
        this.merchantId = merchantId;
        this.code = code;
        this.discount = discount;
        this.validUntil = validUntil;
        this.totalQty = totalQty;
        this.claimedQty = 0;
    }

    // getters/setters
    public String getCouponId() { return couponId; }
    public String getMerchantId() { return merchantId; }
    public String getCode() { return code; }
    public double getDiscount() { return discount; }
    public String getValidUntil() { return validUntil; }
    public int getTotalQty() { return totalQty; }
    public int getClaimedQty() { return claimedQty; }
    public void setClaimedQty(int q) { this.claimedQty = q; }

    /** 用于简单展示的字符串 */
    @Override
    public String toString() {
        return code + " (减免: " + discount + ") 有效期: " + (validUntil == null || validUntil.isEmpty() ? "-" : validUntil);
    }
}
