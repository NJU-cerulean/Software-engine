package com.marketplace.models;

public class Admin {
    private String adminId;
    private String username;

    public Admin(String adminId, String username) {
        this.adminId = adminId;
        this.username = username;
    }
    // 简化：保留构造与示例性加载方法，管理员细粒度操作交由 AdminService 实现
    public void banUser(String userPhone) { /* delegate to AdminService if needed */ }
    public void unbanUser(String userPhone) { /* delegate to AdminService if needed */ }
    public boolean loadSampleData() throws java.sql.SQLException {
        com.marketplace.service.AdminService svc = new com.marketplace.service.AdminService();
        svc.seedSampleData();
        return true;
    }
}
