package com.marketplace.service;

import com.marketplace.models.Order;

/**
 * 统计服务：记录订单相关的统计信息（当前为简单记录/打印实现，可扩展为持久化）。
 */
public class StatisticsService {

    /**
     * 记录一个订单事件（示例实现：打印或更新内存统计）
     */
    public void recordOrder(Order o) {
        // 简单打印，后续可扩展为写入数据库或统计表
        System.out.println("[统计] 记录订单：" + o.getOrderId() + " 用户:" + o.getUserId() + " 商家:" + o.getMerchantId() + " 总额:" + o.getTotalAmount());
    }
}
