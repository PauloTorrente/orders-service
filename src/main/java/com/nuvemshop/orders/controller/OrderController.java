package com.nuvemshop.orders.controller;

import com.nuvemshop.orders.dto.OrderDTOs.*;
import com.nuvemshop.orders.model.OrderStatus;
import com.nuvemshop.orders.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {

    private final OrderService orderService;

    // returns 201 Created instead of 200 when a new order is placed
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new order", description = "Creates an order and decreases stock for each item")
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    // supports filtering by email or status, and adds X-Total-Revenue to the response headers
    @GetMapping
    @Operation(summary = "List all orders (paginated) with X-Total-Revenue header")
    public ResponseEntity<Page<OrderSummaryResponse>> listOrders(
            @RequestParam(required = false) String customerEmail,
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        Page<OrderSummaryResponse> page;
        if (customerEmail != null) page = orderService.listByCustomer(customerEmail, pageable);
        else if (status != null)   page = orderService.listByStatus(status, pageable);
        else                       page = orderService.listOrders(pageable);

        // expose total revenue in header so the frontend can show it without a separate request
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Revenue",   orderService.totalRevenue().toPlainString());
        headers.add("X-Total-Orders",    String.valueOf(page.getTotalElements()));
        headers.add("Access-Control-Expose-Headers", "X-Total-Revenue,X-Total-Orders");

        return ResponseEntity.ok().headers(headers).body(page);
    }

    // PATCH because we're only changing one field, not replacing the whole resource
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status", description = "Validates allowed status transitions. Cancelling an order restores stock.")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request
    ) {
        return ResponseEntity.ok(orderService.updateStatus(id, request));
    }
}
