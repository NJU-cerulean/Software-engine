package com.marketplace.fuzz;

import com.marketplace.db.DBUtil;
import com.marketplace.service.ProductService;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class ProductServiceFuzzer {
    public static void fuzzerTestOneInput(byte[] data) {
        try {
            DBUtil.seedSampleData();
        } catch (java.sql.SQLException e) {
            return;
        }
        String s = new String(data, StandardCharsets.UTF_8);
        ProductService ps = new ProductService();
        int choice = (s.length() > 0) ? Math.abs(s.charAt(0)) % 5 : 0;
        try {
            switch (choice) {
                case 0:
                    ps.listPublished();
                    break;
                case 1:
                    ps.searchProducts(s);
                    break;
                case 2:
                    double price = Math.abs(s.hashCode() % 10000) / 100.0;
                    int stock = Math.abs(s.length() % 200);
                    ps.publishProduct("t-" + s, "d-" + s, price, stock, "merchant-1", "13800000000");
                    break;
                case 3:
                    ps.createCoupon("merchant-1", "code-" + s, 5.0, "2099-12-31", 10);
                    break;
                case 4:
                    ps.claimCoupon("coupon-1", "13800000000");
                    break;
                default:
                    ps.listPublished();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
