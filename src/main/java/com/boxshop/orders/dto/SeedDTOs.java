package com.boxshop.orders.dto;

import com.boxshop.orders.model.OrderStatus;
import java.time.Instant;
import java.util.List;

public class SeedDTOs {

    // used only by the seed endpoint to create backdated orders for demo data
    public record SeedOrderRequest(
            String customerEmail,
            List<OrderDTOs.OrderItemRequest> items,
            OrderStatus targetStatus,
            Instant createdAt
    ) {}
}
