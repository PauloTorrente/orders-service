package com.boxshop.orders.dto;

import com.boxshop.orders.model.OrderStatus;
import java.time.Instant;
import java.util.List;

public class SeedDTOs {

    // creates a backdated order for demo/seed purposes
    public record SeedOrderRequest(
            String customerEmail,
            List<OrderDTOs.OrderItemRequest> items,
            OrderStatus targetStatus,
            Instant createdAt
    ) {}
}
