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

    @Transactional
    public void createBackdatedOrder(SeedOrderRequest req) {
        // create normally, advance status, then backdate via native SQL
        OrderResponse created = orderService.createOrder(
                new CreateOrderRequest(req.customerEmail(), req.items()));
        Long orderId = created.id();

        for (OrderStatus s : pathTo(req.targetStatus())) {
            orderService.updateStatus(orderId, new UpdateStatusRequest(s, "seed"));
        }

        entityManager.createNativeQuery(
                "UPDATE orders SET created_at = ?, updated_at = ? WHERE id = ?")
                .setParameter(1, Timestamp.from(req.createdAt()))
                .setParameter(2, Timestamp.from(req.createdAt()))
                .setParameter(3, orderId)
                .executeUpdate();
    }

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
