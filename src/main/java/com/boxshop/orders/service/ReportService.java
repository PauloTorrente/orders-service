package com.boxshop.orders.service;

import com.boxshop.orders.dto.ReportDTOs.*;
import com.boxshop.orders.model.OrderStatus;
import com.boxshop.orders.repository.OrderHistoryRepository;
import com.boxshop.orders.repository.RevenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final RevenueRepository revenueRepository;
    private final OrderHistoryRepository orderHistoryRepository;

    @Transactional(readOnly = true)
    public RevenueReportResponse revenueReport(Instant from, Instant to) {
        long total     = revenueRepository.countByPeriod(from, to);
        long delivered = revenueRepository.countByPeriodAndStatus(from, to, OrderStatus.DELIVERED);
        long cancelled = revenueRepository.countByPeriodAndStatus(from, to, OrderStatus.CANCELLED);
        BigDecimal revenue = revenueRepository.sumRevenueByPeriod(from, to);

        BigDecimal avg = delivered > 0
                ? revenue.divide(BigDecimal.valueOf(delivered), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new RevenueReportResponse(from, to, total, delivered, cancelled, revenue, avg);
    }

    @Transactional(readOnly = true)
    public List<StatusBreakdownItem> statusBreakdown(Instant from, Instant to) {
        return revenueRepository.revenueBreakdownByStatus(from, to).stream()
                .map(row -> new StatusBreakdownItem(
                        row[0].toString(),
                        (Long) row[1],
                        (BigDecimal) row[2]
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderHistoryResponse> orderHistory(Long orderId) {
        return orderHistoryRepository.findByOrderIdOrderByChangedAtAsc(orderId).stream()
                .map(h -> new OrderHistoryResponse(
                        h.getId(),
                        h.getFromStatus().name(),
                        h.getToStatus().name(),
                        h.getChangedBy(),
                        h.getChangedAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TopProductItem> topProducts(Instant from, Instant to, int limit) {
        return revenueRepository.topProductsByUnitsSold(from, to).stream()
                .limit(limit)
                .map(row -> new TopProductItem(
                        (Long) row[0],
                        (String) row[1],
                        (String) row[2],
                        (Long) row[3],
                        (BigDecimal) row[4]
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FunnelItem> funnel(Instant from, Instant to) {
        return revenueRepository.countByEachStatus(from, to).stream()
                .map(row -> new FunnelItem(row[0].toString(), (Long) row[1]))
                .toList();
    }

    // returns one entry per day with revenue and order count — powers the stock chart
    @Transactional(readOnly = true)
    public List<RevenueTimelineItem> revenueTimeline(Instant from, Instant to) {
        return revenueRepository.revenueTimeline(from, to).stream()
                .map(row -> new RevenueTimelineItem(
                        row[0].toString(),   // LocalDate → "2026-02-01"
                        (Long) row[1],
                        (BigDecimal) row[2]
                ))
                .toList();
    }
}
