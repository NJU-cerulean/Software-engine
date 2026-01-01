package com.marketplace.service;

import com.marketplace.db.DBUtil;
import com.marketplace.dao.ProductDAO;
import com.marketplace.models.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ProductServiceTest {
    private final ProductService svc = new ProductService();
    private final ProductDAO dao = new ProductDAO();

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
    public void testPublishProduct_creates_and_findable() throws SQLException {
        Product p = svc.publishProduct("T1", "描述1", 9.9, 10, "m1", "10000000001");
        assertNotNull(p.getProductId());
        Product found = dao.findById(p.getProductId());
        assertNotNull(found);
        assertEquals("T1", found.getTitle());
    }

    @Test
    public void testListPublished_contains_seeded() throws SQLException {
        List<Product> list = svc.listPublished();
        assertTrue(list.size() >= 3);
    }

    @Test
    public void testSearchProducts_null_returns_all() throws SQLException {
        List<Product> all = svc.searchProducts(null);
        assertEquals(svc.listPublished().size(), all.size());
    }

    @Test
    public void testSearchProducts_empty_returns_all() throws SQLException {
        List<Product> all = svc.searchProducts("");
        assertEquals(svc.listPublished().size(), all.size());
    }

    @Test
    public void testSearchProducts_title_match() throws SQLException {
        Product p = svc.publishProduct("独特标题XYZ", "其它", 1.0, 1, "m1", "10000000001");
        List<com.marketplace.models.Product> res = svc.searchProducts("XYZ");
        assertTrue(res.stream().anyMatch(x -> x.getProductId().equals(p.getProductId())));
    }

    @Test
    public void testSearchProducts_description_match() throws SQLException {
        Product p = svc.publishProduct("titleB", "包含关键字magicDesc", 1.0, 1, "m1", "10000000001");
        List<com.marketplace.models.Product> res = svc.searchProducts("magicDesc");
        assertTrue(res.stream().anyMatch(x -> x.getProductId().equals(p.getProductId())));
    }

    @Test
    public void testSearchProducts_case_insensitive() throws SQLException {
        Product p = svc.publishProduct("CaseTitle", "MiXeD desc", 1.0, 1, "m1", "10000000001");
        List<com.marketplace.models.Product> res1 = svc.searchProducts("casetitle");
        List<com.marketplace.models.Product> res2 = svc.searchProducts("mixed");
        assertTrue(res1.stream().anyMatch(x -> x.getProductId().equals(p.getProductId())));
        assertTrue(res2.stream().anyMatch(x -> x.getProductId().equals(p.getProductId())));
    }

    @Test
    public void testSearchProducts_no_match_returns_empty() throws SQLException {
        List<com.marketplace.models.Product> res = svc.searchProducts("不存在的关键词_zzz");
        assertTrue(res.isEmpty());
    }

    @Test
    public void testPublishProduct_multiple_unique_ids() throws SQLException {
        Product a = svc.publishProduct("A1","d",1.0,1,"m1","10000000001");
        Product b = svc.publishProduct("A2","d",1.0,1,"m1","10000000001");
        assertNotEquals(a.getProductId(), b.getProductId());
    }

    @Test
    public void testReduceStock_and_delete() throws SQLException {
        Product p = svc.publishProduct("ToReduce","x",5.0,5,"m1","10000000001");
        dao.reduceStock(p.getProductId(), 2);
        Product after = dao.findById(p.getProductId());
        assertEquals(3, after.getStock());
        dao.deleteProduct(p.getProductId());
        assertNull(dao.findById(p.getProductId()));
    }
}
