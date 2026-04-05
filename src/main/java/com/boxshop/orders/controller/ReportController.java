package com.boxshop.orders.controller;

import com.boxshop.orders.dto.ReportDTOs.*;
import com.boxshop.orders.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Sales and audit reports")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/revenue")
    @Operation(summary = "Revenue summary for a date range")
    public RevenueReportResponse revenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return reportService.revenueReport(toInstant(from), toInstant(to.plusDays(1)));
    }

    @GetMapping("/revenue/breakdown")
    @Operation(summary = "Revenue breakdown by order status")
    public List<StatusBreakdownItem> breakdown(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return reportService.statusBreakdown(toInstant(from), toInstant(to.plusDays(1)));
    }

    @GetMapping("/revenue/timeline")
    @Operation(summary = "Daily revenue timeline — powers the stock chart")
    public List<RevenueTimelineItem> timeline(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return reportService.revenueTimeline(toInstant(from), toInstant(to.plusDays(1)));
    }

    @GetMapping("/top-products")
    @Operation(summary = "Top products by units sold in delivered orders")
    public List<TopProductItem> topProducts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "8") int limit
    ) {
        return reportService.topProducts(toInstant(from), toInstant(to.plusDays(1)), limit);
    }

    @GetMapping("/funnel")
    @Operation(summary = "Order count per status for conversion funnel")
    public List<FunnelItem> funnel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return reportService.funnel(toInstant(from), toInstant(to.plusDays(1)));
    }

    @GetMapping("/orders/{id}/history")
    @Operation(summary = "Full audit trail for an order")
    public List<OrderHistoryResponse> orderHistory(@PathVariable Long id) {
        return reportService.orderHistory(id);
    }

    private Instant toInstant(LocalDate d) {
        return d.atStartOfDay(ZoneOffset.UTC).toInstant();
    }
}
