package com.nuvemshop.orders.controller;

import com.nuvemshop.orders.dto.ReportDTOs.*;
import com.nuvemshop.orders.service.ReportService;
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

    // GET /api/v1/reports/revenue?from=2024-01-01&to=2024-12-31
    @GetMapping("/revenue")
    @Operation(summary = "Revenue report by period", description = "Aggregates total revenue, order count and average ticket for a date range")
    public RevenueReportResponse revenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        // convert LocalDate to Instant (start of day and end of day UTC)
        Instant fromInstant = from.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant toInstant   = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        return reportService.revenueReport(fromInstant, toInstant);
    }

    // GET /api/v1/reports/revenue/breakdown?from=2024-01-01&to=2024-12-31
    @GetMapping("/revenue/breakdown")
    @Operation(summary = "Revenue breakdown by order status")
    public List<StatusBreakdownItem> breakdown(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        Instant fromInstant = from.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant toInstant   = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        return reportService.statusBreakdown(fromInstant, toInstant);
    }

    // GET /api/v1/reports/orders/{id}/history
    @GetMapping("/orders/{id}/history")
    @Operation(summary = "Full audit trail of status changes for an order")
    public List<OrderHistoryResponse> orderHistory(@PathVariable Long id) {
        return reportService.orderHistory(id);
    }
}
