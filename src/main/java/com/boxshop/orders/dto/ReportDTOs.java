package com.boxshop.orders.dto;

import java.math.BigDecimal;
import java.time.Instant;

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

    // one data point per day for the revenue chart
    public record RevenueTimelineItem(
            String date,
            long orderCount,
            BigDecimal revenue
    ) {}
}
