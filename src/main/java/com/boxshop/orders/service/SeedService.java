package com.boxshop.orders.service;

import com.boxshop.orders.dto.OrderDTOs.*;
import com.boxshop.orders.dto.SeedDTOs.SeedOrderRequest;
import com.boxshop.orders.model.OrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

@Service
@RequiredArgsConstructor
public class SeedService {

    private final OrderService orderService;

    @PersistenceContext
    private EntityManager entityManager;

    // creates an order, advances it to the desired status, then backdates it
    @Transactional
    public void createBackdatedOrder(SeedOrderRequest req) {
        // step 1: create the order normally (gets createdAt = now)
        OrderResponse created = orderService.createOrder(
                new CreateOrderRequest(req.customerEmail(), req.items())
        );
        Long orderId = created.id();

        // step 2: advance through the status machine to reach targetStatus
        for (OrderStatus s : pathTo(req.targetStatus())) {
            orderService.updateStatus(orderId, new UpdateStatusRequest(s, "seed"));
        }

        // step 3: backdate created_at and updated_at via native SQL
        entityManager.createNativeQuery(
                "UPDATE orders SET created_at = ?, updated_at = ? WHERE id = ?"
        )
        .setParameter(1, Timestamp.from(req.createdAt()))
        .setParameter(2, Timestamp.from(req.createdAt()))
        .setParameter(3, orderId)
        .executeUpdate();
    }

    // returns the sequence of statuses needed to reach the target
    private OrderStatus[] pathTo(OrderStatus target) {
        return switch (target) {
            case CONFIRMED  -> new OrderStatus[]{ OrderStatus.CONFIRMED };
            case PROCESSING -> new OrderStatus[]{ OrderStatus.CONFIRMED, OrderStatus.PROCESSING };
            case SHIPPED    -> new OrderStatus[]{ OrderStatus.CONFIRMED, OrderStatus.PROCESSING, OrderStatus.SHIPPED };
            case DELIVERED  -> new OrderStatus[]{ OrderStatus.CONFIRMED, OrderStatus.PROCESSING, OrderStatus.SHIPPED, OrderStatus.DELIVERED };
            case CANCELLED  -> new OrderStatus[]{ OrderStatus.CANCELLED };
            default         -> new OrderStatus[]{};
        };
    }
}
