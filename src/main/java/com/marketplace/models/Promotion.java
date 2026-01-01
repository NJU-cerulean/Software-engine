package com.marketplace.models;

import java.util.Date;

public class Promotion {
    private String promotionId;
    private String merchantId;
    private Enums.PromotionType type;
    private double discount;
    private Date validUntil;

    public Promotion(String promotionId, String merchantId, Enums.PromotionType type, double discount, Date validUntil) {
        this.promotionId = promotionId;
        this.merchantId = merchantId;
        this.type = type;
        this.discount = discount;
        this.validUntil = validUntil;
    }

    public boolean applyPromotion() { return true; }
    public boolean validateCoupon() { return validUntil == null || validUntil.after(new Date()); }
}
