package com.marketplace.models;

/**
 * 商品模型：表示平台上的商品实体及其基本操作。
 */
public class Product {
    private String productId;
    private String title;
    private String description;
    private double price;
    private int stock;
    private Enums.ProductStatus status;
    private String merchantId;
    private String merchantPhone;

    /**
     * 构造商品对象
     */
    public Product(String productId, String title, String description, double price, int stock, Enums.ProductStatus status, String merchantId, String merchantPhone) {
        this.productId = productId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.status = status;
        this.merchantId = merchantId;
        this.merchantPhone = merchantPhone;
    }

    public String getProductId() { return productId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public Enums.ProductStatus getStatus() { return status; }
    public String getMerchantId() { return merchantId; }
    public String getMerchantPhone() { return merchantPhone; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setStock(int stock) { this.stock = stock; }
    public void setStatus(Enums.ProductStatus status) { this.status = status; }

    public void updateInfo(String title, String description, double price, int stock) {
        setTitle(title);
        setDescription(description);
        setPrice(price);
        setStock(stock);
    }

    public void changeStatus(Enums.ProductStatus newStatus) {
        setStatus(newStatus);
    }
}
