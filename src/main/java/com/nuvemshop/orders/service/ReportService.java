package com.nuvemshop.orders.service;

import com.nuvemshop.orders.dto.ReportDTOs.*;
import com.nuvemshop.orders.model.OrderStatus;
import com.nuvemshop.orders.repository.OrderHistoryRepository;
import com.nuvemshop.orders.repository.RevenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final RevenueRepository revenueRepository;
    private final OrderHistoryRepository orderHistoryRepository;

    @Transactional(readOnly = true)
    public RevenueReportResponse revenueReport(Instant from, Instant to) {
        long total      = revenueRepository.countByPeriod(from, to);
        long delivered  = revenueRepository.countByPeriodAndStatus(from, to, OrderStatus.DELIVERED);
        long cancelled  = revenueRepository.countByPeriodAndStatus(from, to, OrderStatus.CANCELLED);
        BigDecimal revenue = revenueRepository.sumRevenueByPeriod(from, to);

        // average based on delivered orders only — cancelled don't count as revenue
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
}
