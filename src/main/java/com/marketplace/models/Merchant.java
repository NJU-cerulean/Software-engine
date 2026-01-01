package com.marketplace.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Merchant implements Client {
    private String merchantId;
    private String shopName;
    private String contactInfo;
    private int employeeCount;
    private Enums.IDENTITY identity;
    private String phone;
    private String password;
    private final List<Message> mymessage = new ArrayList<>();

    public Merchant(String shopName, String phone, String contactInfo, Enums.IDENTITY identity) {
        this.merchantId = "merchant-" + UUID.randomUUID();
        this.shopName = shopName;
        this.phone = phone;
        this.contactInfo = contactInfo;
        this.identity = identity;
    }

    // 用于从数据库恢复时设置 merchantId
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }

    public String getMerchantId() { return merchantId; }
    public String getShopName() { return shopName; }
    public String getContactInfo() { return contactInfo; }
    public int getEmployeeCount() { return employeeCount; }
    public Enums.IDENTITY getIdentity() { return identity; }
    public String getPhone() { return phone; }
    public void setPassword(String password) { this.password = password; }
    public List<Message> getMymessage() { return mymessage; }

    @Override
    public int getWeChatAssociation() { return 0; }
    @Override
    public int getQQAssociation() { return 0; }
    @Override
    public int getAppleAssociation() { return 0; }
    @Override
    public int getGoogleAssociation() { return 0; }

    @Override
    public void register() { /* DAO persist */ }

    @Override
    public void reportToAdmin(String content) { /* create complaint */ }

    @Override
    public boolean login(String password) {
        return this.password != null && this.password.equals(password);
    }

    @Override
    public void logout() { }

    public void manageProducts() { }
    public void processOrders() { }
    public void viewStatistics() { }
    public void createPromotion() { }
}
