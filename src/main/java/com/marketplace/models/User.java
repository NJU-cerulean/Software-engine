package com.marketplace.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 用户模型：包含用户基本信息与简单行为（登录、购物车等）。
 */
public class User implements Client {
    private String userId;
    private String username;
    private Enums.VIPLevel vipLevel;
    private int loginCount;
    private Date lastLogin;
    private String phone;
    private String password;
    private final List<Product> shoppingCart = new ArrayList<>();
    private final List<Message> mymessage = new ArrayList<>();

    // social bindings (simple counters)
    private int weChatAssociation;
    private int qqAssociation;
    private int appleAssociation;
    private int googleAssociation;

    /**
     * 构造新用户，生成唯一 userId
     */
    public User(String username, String phone, String password) {
        this.userId = "user-" + UUID.randomUUID();
        this.username = username;
        this.phone = phone;
        this.password = password;
        this.vipLevel = Enums.VIPLevel.NORMAL;
        this.loginCount = 0;
    }

    // getters/setters
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public Enums.VIPLevel getVipLevel() { return vipLevel; }
    public int getLoginCount() { return loginCount; }
    public Date getLastLogin() { return lastLogin; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setPassword(String password) { this.password = password; }
    public List<Product> getShoppingCart() { return shoppingCart; }
    public List<Message> getMymessage() { return mymessage; }

    // Client interface minimal impl
    public int getWeChatAssociation() { return weChatAssociation; }
    public int getQQAssociation() { return qqAssociation; }
    public int getAppleAssociation() { return appleAssociation; }
    public int getGoogleAssociation() { return googleAssociation; }

    @Override
    public void register() { /* persisted by DAO */ }

    @Override
    public void reportToAdmin(String content) { /* create complaint via service */ }

    @Override
    public boolean login(String password) {
        boolean ok = this.password != null && this.password.equals(password);
        if (ok) {
            loginCount++;
            lastLogin = new Date();
        }
        return ok;
    }

    @Override
    public void logout() { /* nothing for now */ }

    // domain behaviors
    public List<Product> searchProducts(List<Product> source, String keyword) {
        List<Product> res = new ArrayList<>();
        for (Product p : source) {
            if (p.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
                res.add(p);
            }
        }
        return res;
    }

    public void placeOrder(Order order) { /* created via OrderService */ }

    public void showShoppingCart() {
        if (shoppingCart.isEmpty()) {
            System.out.println("购物车为空");
            return;
        }
        System.out.println("购物车内容:");
        for (Product p : shoppingCart) {
            System.out.println(p.getTitle() + " x1 - " + p.getPrice());
        }
    }
}
