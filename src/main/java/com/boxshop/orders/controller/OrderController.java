package com.boxshop.orders.controller;

import com.boxshop.orders.dto.OrderDTOs.*;
import com.boxshop.orders.model.OrderStatus;
import com.boxshop.orders.service.OrderService;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new order")
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    // filters by customerEmail or status if provided; adds X-Total-Revenue header
    @GetMapping
    @Operation(summary = "List orders (paginated)")
    public ResponseEntity<Page<OrderSummaryResponse>> listOrders(
            @RequestParam(required = false) String customerEmail,
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        Page<OrderSummaryResponse> page;
        if (customerEmail != null)   page = orderService.listByCustomer(customerEmail, pageable);
        else if (status != null)     page = orderService.listByStatus(status, pageable);
        else                         page = orderService.listOrders(pageable);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Revenue", orderService.totalRevenue().toPlainString());
        headers.add("X-Total-Orders",  String.valueOf(page.getTotalElements()));
        headers.add("Access-Control-Expose-Headers", "X-Total-Revenue,X-Total-Orders");

        return ResponseEntity.ok().headers(headers).body(page);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request
    ) {
        return ResponseEntity.ok(orderService.updateStatus(id, request));
    }
}
