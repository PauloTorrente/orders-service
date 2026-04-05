package com.boxshop.orders.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class ReportDTOs {

    // summary of revenue in a given period
    public record RevenueReportResponse(
            Instant from,
            Instant to,
            long totalOrders,
            long deliveredOrders,
            long cancelledOrders,
            BigDecimal totalRevenue,
            BigDecimal averageOrderValue
    ) {}

    // one entry per status showing count and revenue
    public record StatusBreakdownItem(
            String status,
            long count,
            BigDecimal revenue
    ) {}

    // audit history entry for a single status change
    public record OrderHistoryResponse(
            Long id,
            String fromStatus,
            String toStatus,
            String changedBy,
            Instant changedAt
    ) {}
}
