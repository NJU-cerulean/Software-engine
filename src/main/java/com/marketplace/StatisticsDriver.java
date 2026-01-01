package com.marketplace;

import com.marketplace.models.Order;
import com.marketplace.service.StatisticsService;

/**
 * 针对 StatisticsService 的简单 JBMC 驱动：
 * - 构造一个订单并调用 recordOrder
 */
public class StatisticsDriver {
    public static void main(String[] args) {
        StatisticsService statisticsService = new StatisticsService();

        Order order = new Order("order-1", "user1", "merchant1", 80.0, 10.0, 5.0);
        statisticsService.recordOrder(order);
    }
}
