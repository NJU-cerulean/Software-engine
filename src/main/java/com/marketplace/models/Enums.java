package com.marketplace.models;

public class Enums {
    public enum VIPLevel {NORMAL, BRONZE, SILVER, GOLD}
    public enum OrderStatus {PENDING_PAYMENT, PENDING_SHIPMENT, SHIPPED, COMPLETED, RETURNING}
    public enum IDENTITY {BOSS, EMPLOYEE}
    public enum ProductStatus {DRAFT, PUBLISHED, SOLD_OUT, BANNED}
    public enum ComplaintType {SERVICE, QUALITY, FRAUD}
    public enum PromotionType {PERCENTAGE, FIXED}
    public enum StatsType {SALES, INVENTORY}
    public enum ComplaintStatus {OPEN, IN_PROGRESS, RESOLVED}
}
