package com.nuvemshop.orders.dto;

import com.nuvemshop.orders.model.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

// groups all order-related request and response records in one place
public class OrderDTOs {

    // request records (what the client sends)

    public record CreateOrderRequest(
            @NotBlank(message = "Customer email is required")
            @Email(message = "Invalid email format")
            String customerEmail,

            // @Valid makes Spring also validate each item inside the list
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

            // who triggered the change — optional, defaults to "system"
            String changedBy
    ) {}

    // response records (what the API returns)

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

    // lighter version used in list endpoints to avoid loading all item details
    public record OrderSummaryResponse(
            Long id,
            String customerEmail,
            OrderStatus status,
            BigDecimal total,
            int itemCount,
            Instant createdAt
    ) {}
}
