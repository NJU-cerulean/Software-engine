package com.marketplace.models;

import java.util.Date;

public class Order {
    private String orderId;
    private String userId;
    private String merchantId;
    private double totalAmount;
    private double discount;
    private double payByPlatform;
    private Enums.OrderStatus status;
    private Date createTime;

    public Order(String orderId, String userId, String merchantId, double totalAmount, double discount, double payByPlatform) {
        this.orderId = orderId;
        this.userId = userId;
        this.merchantId = merchantId;
        this.totalAmount = totalAmount;
        this.discount = discount;
        this.payByPlatform = payByPlatform;
        this.status = Enums.OrderStatus.PENDING_PAYMENT;
        this.createTime = new Date();
    }

    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public String getMerchantId() { return merchantId; }
    public double getTotalAmount() { return totalAmount; }
    public double getDiscount() { return discount; }
    public double getPayByPlatform() { return payByPlatform; }
    public Enums.OrderStatus getStatus() { return status; }
    public Date getCreateTime() { return createTime; }

    public double calculateTotal() {
        return totalAmount - discount - payByPlatform;
    }

    public void updateStatus(Enums.OrderStatus newStatus) { this.status = newStatus; }
    public void cancelOrder() { this.status = Enums.OrderStatus.RETURNING; }
}
