package com.marketplace.models;

import java.util.Map;

/**
 * 统计数据承载类型，使用 record 简化样板代码。
 */
public record Statistics(String orderId, Enums.StatsType type, Map<String, Object> data) {
}
