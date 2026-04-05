package com.boxshop.orders.controller;

import com.boxshop.orders.dto.SeedDTOs.SeedOrderRequest;
import com.boxshop.orders.service.SeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// dev-only — creates backdated orders for demo data
@RestController
@RequestMapping("/api/v1/seed")
@RequiredArgsConstructor
@Tag(name = "Seed", description = "Dev-only endpoints for demo data generation")
public class SeedController {

    private final SeedService seedService;

    @PostMapping("/order")
    @Operation(summary = "Create a single backdated order")
    public ResponseEntity<Void> seedOrder(@RequestBody SeedOrderRequest request) {
        seedService.createBackdatedOrder(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/orders/batch")
    @Operation(summary = "Create multiple backdated orders at once")
    public ResponseEntity<Void> seedBatch(@RequestBody List<SeedOrderRequest> requests) {
        requests.forEach(seedService::createBackdatedOrder);
        return ResponseEntity.ok().build();
    }
}
