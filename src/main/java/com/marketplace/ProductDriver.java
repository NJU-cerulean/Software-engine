package com.marketplace;

import com.marketplace.service.ProductService;
import com.marketplace.models.Product;

/**
 * 针对 ProductService 的简单 JBMC 驱动：
 * - 发布商品
 * - 搜索商品
 */
public class ProductDriver {
    public static void main(String[] args) throws Exception {
        ProductService productService = new ProductService();

        // 发布一个商品
        Product p = productService.publishProduct(
                "测试商品", "用于 JBMC 驱动的测试商品", 50.0, 10,
                "merchant1", "13800000000");
        assert p != null;
        assert p.getPrice() >= 0;

        // 搜索商品（关键字匹配）
        try {
            java.util.List<Product> res = productService.searchProducts("测试");
            assert res != null;
        } catch (Exception ignored) {
            // DAO / 数据库可能抛出异常，这里不关心持久化细节
        }
    }
}
