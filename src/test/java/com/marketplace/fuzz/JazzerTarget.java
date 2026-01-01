package com.marketplace.fuzz;

import com.marketplace.db.DBUtil;
import com.marketplace.service.MessageService;
import com.marketplace.service.OrderService;
import com.marketplace.service.ProductService;


import java.sql.SQLException;
import java.nio.charset.StandardCharsets;

public final class JazzerTarget {
    static {
        try {
            DBUtil.seedSampleData();
        } catch (SQLException ignored) {
        }
    }

    // 使用 byte[] 接口避免对 Jazzer API 的直接依赖，便于 maven 编译
    public static void fuzzerTestOneInput(byte[] data) {
        ProductService productService = new ProductService();
        MessageService messageService = new MessageService();
        OrderService orderService = new OrderService();

        if (data == null) return;
        String all = new String(data, StandardCharsets.UTF_8);
        int action = (all.hashCode() & 0x7fffffff) % 7;
        try {
            switch (action) {
                case 0: {
                    String keyword = substringSafe(all, 0, 100);
                    productService.searchProducts(keyword);
                    break;
                }
                case 1: {
                    String title = substringSafe(all, 0, 200);
                    String desc = substringSafe(all, 200, 500);
                    double price = Math.abs((double) all.hashCode()) % 100000;
                    int stock = Math.abs(all.length()) % 10000;
                    String merchantId = substringSafe(all, 100, 150);
                    String merchantPhone = substringSafe(all, 150, 180);
                    productService.publishProduct(title, desc, price, stock, merchantId, merchantPhone);
                    break;
                }
                case 2: {
                    String sender = substringSafe(all, 0, 50);
                    String receiver = substringSafe(all, 50, 100);
                    String content = substringSafe(all, 100, 600);
                    messageService.sendMessagePublic(sender, receiver, content);
                    break;
                }
                case 3: {
                    String userId = substringSafe(all, 0, 50);
                    String merchantId = substringSafe(all, 50, 100);
                    double total = Math.abs((double) all.hashCode()) % 100000;
                    orderService.createOrder(userId, merchantId, total, 0.0, 0.0);
                    break;
                }
                case 4: {
                    String userId = substringSafe(all, 0, 50);
                    orderService.listByUser(userId);
                    break;
                }
                case 5: {
                    String u = substringSafe(all, 0, 50);
                    String o = substringSafe(all, 50, 100);
                    messageService.getConversation(u, o);
                    break;
                }
                case 6: {
                    String couponId = substringSafe(all, 0, 60);
                    String userPhone = substringSafe(all, 60, 90);
                    try {
                        productService.claimCoupon(couponId, userPhone);
                    } catch (SQLException ignored) { }
                    break;
                }
                default:
                    break;
            }
        } catch (SQLException e) {
            // 忽略 SQL 异常以关注运行时崩溃
        }
    }

    private static String substringSafe(String s, int start, int end) {
        if (s == null) return "";
        if (start < 0) start = 0;
        if (end < start) return "";
        if (start >= s.length()) return "";
        end = Math.min(end, s.length());
        return s.substring(start, end);
    }
}
