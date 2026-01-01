package com.marketplace.integration;

import com.marketplace.db.DBUtil;
import com.marketplace.dao.ProductDAO;
import com.marketplace.service.MessageService;
import com.marketplace.service.ProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTests {
    private final ProductService psvc = new ProductService();
    private final ProductDAO pdao = new ProductDAO();
    private final MessageService msvc = new MessageService();

    @BeforeEach
    public void setup() throws SQLException {
        DBUtil.clearAllData();
        DBUtil.seedSampleData();
    }

    @AfterEach
    public void tearDown() throws SQLException {
        DBUtil.clearAllData();
    }

    @Test
    public void testPublish_reduceStock_and_notifyMerchant() throws SQLException {
        // 发布商品 -> 模拟购买 -> 减库存 -> 通知商家 -> 检查消息与库存
        var prod = psvc.publishProduct("整合商品","desc",100.0,3,"m1","10000000001");
        // 模拟下单减少 2
        pdao.reduceStock(prod.getProductId(), 2);
        var after = pdao.findById(prod.getProductId());
        assertEquals(1, after.getStock());
        // 发通知给商家
        msvc.sendToMerchant("m1", "有购买，商品:" + prod.getProductId());
        List<String> msgs = msvc.getMessagesFor("m1");
        assertTrue(msgs.stream().anyMatch(s -> s.contains(prod.getProductId())));
    }

    @Test
    public void testCreate_coupon_claim_and_list() throws SQLException {
        // 商家创建优惠券 -> 用户领取 -> 列出用户优惠券
        psvc.createCoupon("m1","CODE123",10.0,"2099-01-01",5);
        // 找到商家的优惠券 id
        List<String> coupons = psvc.listCouponsForMerchant("m1");
        assertFalse(coupons.isEmpty());
        String first = coupons.get(0);
        String couponId = first.split(" \\|")[0].trim();
        boolean ok = psvc.claimCoupon(couponId, "19900000000");
        assertTrue(ok);
        List<String> userCoupons = psvc.listUserCoupons("19900000000");
        assertFalse(userCoupons.isEmpty());
    }
}
