package com.marketplace.fuzz;

import com.marketplace.db.DBUtil;
import com.marketplace.service.*;
import com.marketplace.models.Enums;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Random;

/**
 * 更加综合的 fuzz 入口：
 * - 将输入拆分为多个片段并通过不同解析策略生成字符串、整型、浮点值
 * - 依次调用更多服务（AuthService, AdminService, ComplaintService, SettingsService 等）
 * - 捕获并忽略已知的受控异常（例如 SQLException、UnsupportedOperationException），让未知运行时异常向上抛出以便 Jazzer 报告
 */
public final class CombinedFuzzer {
    static {
        try {
            DBUtil.seedSampleData();
        } catch (Exception ignored) {
        }
    }

    public static void fuzzerTestOneInput(byte[] data) {
        if (data == null) return;

        // 预备服务实例
        ProductService ps = new ProductService();
        OrderService os = new OrderService();
        MessageService ms = new MessageService();
        AuthService auth = new AuthService();
        AdminService admin = new AdminService();
        ComplaintService cs = new ComplaintService();
        SettingsService settings = new SettingsService();
        CartService cart = new CartService();

        // 解析输入：多种视角
        String s = new String(data, StandardCharsets.UTF_8);
        int hash = (s != null) ? s.hashCode() : 0;
        Random rnd = new Random(hash);

        // 生成多个 derived 参数
        String small = substrSafe(s, 0, Math.min(64, s.length()));
        String mid = substrSafe(s, 0, Math.min(256, s.length()));
        String longS = substrSafe(s, 0, Math.min(1024, s.length()));
        int idx = Math.abs(hash) % 10;
        double amount = Math.abs((double) hash) % 100000;
        String phone = generatePhone(rnd);

        // 调用多个服务方法，按组合执行；对已知受控异常做捕获
        try {
            // 不再依赖独立 fuzzer 类，直接调用服务以实现更稳定的综合测试

            // 更广泛的方法调用
            // Product related
            try {
                ps.searchProducts(small);
                ps.publishProduct("t-" + mid, "d-" + longS, amount, Math.abs(hash) % 1000, "m-" + idx, phone);
                ps.createCoupon("m-" + idx, "code-" + small, (rnd.nextDouble() * 50), "2099-12-31", rnd.nextInt(100));
            } catch (SQLException ignored) { }

            // Order related
            try {
                os.createOrder("user-" + idx, "merchant-" + idx, amount, 0.0, 0.0);
                os.createOrderWithCoupon("user-" + idx, "merchant-" + idx, amount / 2.0, (s.length() > 3) ? "coupon-" + small : null);
                os.listByUser("user-" + (Math.abs(hash) % 3));
            } catch (SQLException e) { }

            // Message
            try {
                ms.sendMessagePublic("from-" + idx, "to-" + ((idx + 1) % 5), longS);
                ms.getConversation("from-" + idx, "to-" + ((idx + 1) % 5));
            } catch (SQLException ignored) { }

            // Auth and Admin operations — exercise edge cases
            try {
                // 尝试注册并登录（不抛出异常时为正常流程）
                auth.registerUser(new com.marketplace.models.User("u-" + idx, "name" + idx, phone), "p" + small);
            } catch (SQLException ignored) { }
            try {
                admin.banPhone(phone, "fuzz-ban");
                admin.isBanned(phone);
                admin.unbanPhone(phone);
            } catch (SQLException ignored) { }

            // Complaint
            try {
                cs.submitComplaint("user-" + idx, "target-" + idx, Enums.ComplaintType.SERVICE);
                cs.listOpenComplaints();
            } catch (Exception ignored) { }

            // Settings (no-op implementations but exercise code paths)
            settings.setNotificationsEnabled(rnd.nextBoolean());
            settings.setAllowContactDisplay(rnd.nextBoolean());
            settings.setRecommendationLevel(rnd.nextInt(5));

            // CartService methods may be unimplemented —调用以覆盖接口并捕获 UnsupportedOperationException
            try {
                cart.addToCart(phone, "prod-" + idx, Math.abs(hash) % 5);
            } catch (UnsupportedOperationException | SQLException ignored) { }

            try {
                cart.checkout("user-" + idx);
            } catch (UnsupportedOperationException | SQLException ignored) { }

            // 管理员清理/查看接口
            try {
                admin.listAllUserPhonesSorted();
                admin.listBannedPhonesSorted();
            } catch (SQLException ignored) { }

        } catch (RuntimeException rte) {
            // 不干涉未经捕获的运行时异常，让 Jazzer 记录为崩溃
            throw rte;
        }
    }

    private static String substrSafe(String s, int start, int end) {
        if (s == null) return "";
        if (start < 0) start = 0;
        if (end < start) return "";
        if (start >= s.length()) return "";
        end = Math.min(end, s.length());
        return s.substring(start, end);
    }

    private static String generatePhone(Random rnd) {
        // 简单生成 11 位模拟手机号（数字字符）
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 11; i++) sb.append((char) ('0' + rnd.nextInt(10)));
        return sb.toString();
    }
}
