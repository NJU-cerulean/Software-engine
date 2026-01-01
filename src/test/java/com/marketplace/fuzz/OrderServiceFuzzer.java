package com.marketplace.fuzz;

import com.marketplace.db.DBUtil;
import com.marketplace.service.OrderService;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class OrderServiceFuzzer {
    public static void fuzzerTestOneInput(byte[] data) {
        try {
            DBUtil.seedSampleData();
        } catch (java.sql.SQLException e) {
            return;
        }
        String s = new String(data, StandardCharsets.UTF_8);
        OrderService os = new OrderService();
        int choice = (s.length() > 0) ? Math.abs(s.charAt(0)) % 4 : 0;
        try {
            switch (choice) {
                case 0:
                    // create order with simple parsed numbers
                    double amt = Math.abs(s.hashCode() % 10000) / 100.0;
                    os.createOrder("user-1", "merchant-1", amt, 0.0, 0.0);
                    break;
                case 1:
                    // attempt with coupon id
                    os.createOrderWithCoupon("user-1", "merchant-1", 50.0, s.length() > 2 ? "usercoupon-1" : null);
                    break;
                case 2:
                    os.listByUser("user-1");
                    break;
                case 3:
                    // edge: negative-ish amount
                    os.createOrder("user-1", "merchant-1", -Math.abs(s.hashCode() % 100), 0.0, 0.0);
                    break;
                default:
                    os.listByUser("user-1");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
