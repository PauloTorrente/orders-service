package com.boxshop.orders.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public class ReportDTOs {

    public record RevenueReportResponse(
            Instant from,
            Instant to,
            long totalOrders,
            long deliveredOrders,
            long cancelledOrders,
            BigDecimal totalRevenue,
            BigDecimal averageOrderValue
    ) {}

    public record StatusBreakdownItem(
            String status,
            long count,
            BigDecimal revenue
    ) {}

    public record OrderHistoryResponse(
            Long id,
            String fromStatus,
            String toStatus,
            String changedBy,
            Instant changedAt
    ) {}

    public record TopProductItem(
            Long productId,
            String productName,
            String sku,
            long unitsSold,
            BigDecimal revenue
    ) {}

    public record FunnelItem(
            String status,
            long count
    ) {}

    // one data point per day for the revenue line chart (delivered only)
    public record RevenueTimelineItem(
            String date,
            long orderCount,
            BigDecimal revenue
    ) {}

    // one data point per day showing counts for ALL statuses
    public record OrdersTimelineItem(
            String date,
            Map<String, Long> countByStatus
    ) {}
}
