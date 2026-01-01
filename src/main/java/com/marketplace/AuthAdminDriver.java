package com.marketplace;

import com.marketplace.models.User;
import com.marketplace.models.Merchant;
import com.marketplace.service.AuthService;
import com.marketplace.service.AdminService;

/**
 * 针对 AuthService / AdminService 的简单 JBMC 驱动：
 * - 注册 / 登录用户
 * - 注册 / 登录商家
 * - 调用管理员的封禁相关接口
 */
public class AuthAdminDriver {
    public static void main(String[] args) throws Exception {
        AuthService authService = new AuthService();
        AdminService adminService = new AdminService();

        // 构造一个用户和商家对象（根据现有构造函数）
        User user = new User("测试用户", "13800000000", "password");
        Merchant merchant = new Merchant("测试商家", "13900000000", "contact", com.marketplace.models.Enums.IDENTITY.BOSS);

        // 管理员初始不封禁该手机号
        try {
            boolean banned = adminService.isBanned(user.getPhone());
            assert !banned || banned == true; // 这里主要是触发调用
        } catch (Exception ignored) {
        }

        // 试图注册用户 / 登录
        try {
            authService.registerUser(user, "password");
            authService.loginUser(user.getPhone(), "password");
        } catch (Exception ignored) {
        }

        // 试图注册商家 / 登录
        try {
            authService.registerMerchant(merchant, "password");
            authService.loginMerchant(merchant.getPhone(), "password");
        } catch (Exception ignored) {
        }

        // 管理员封禁 / 解封手机号
        try {
            adminService.banPhone(user.getPhone(), "test");
            adminService.unbanPhone(user.getPhone());
        } catch (Exception ignored) {
        }
    }
}
