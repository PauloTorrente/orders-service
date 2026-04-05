package com.boxshop.orders.dto;

import com.boxshop.orders.model.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderDTOs {

    public record CreateOrderRequest(
            @NotBlank(message = "Customer email is required")
            @Email(message = "Invalid email format")
            String customerEmail,

            // @Valid cascades validation into each item in the list
            @NotEmpty(message = "Order must have at least one item")
            @Valid
            List<OrderItemRequest> items
    ) {}

    public record OrderItemRequest(
            @NotNull(message = "Product ID is required")
            Long productId,

            @NotNull @Min(value = 1, message = "Quantity must be at least 1")
            Integer quantity
    ) {}

    public record UpdateStatusRequest(
            @NotNull(message = "Status is required")
            OrderStatus status,

            // optional — defaults to "system" if not provided
            String changedBy
    ) {}

    public record OrderResponse(
            Long id,
            String customerEmail,
            OrderStatus status,
            List<OrderItemResponse> items,
            BigDecimal total,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record OrderItemResponse(
            Long productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {}

    // lighter version used in list endpoints
    public record OrderSummaryResponse(
            Long id,
            String customerEmail,
            OrderStatus status,
            BigDecimal total,
            int itemCount,
            Instant createdAt
    ) {}
}
