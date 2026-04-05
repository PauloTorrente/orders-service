package com.boxshop.orders.service;

import com.boxshop.orders.dto.OrderDTOs.*;
import com.boxshop.orders.exception.BusinessException;
import com.boxshop.orders.exception.ResourceNotFoundException;
import com.boxshop.orders.model.*;
import com.boxshop.orders.repository.OrderHistoryRepository;
import com.boxshop.orders.repository.OrderRepository;
import com.boxshop.orders.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderHistoryRepository orderHistoryRepository;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = Order.builder()
                .customerEmail(request.customerEmail())
                .items(new ArrayList<>())
                .total(BigDecimal.ZERO)
                .build();

        for (OrderItemRequest itemReq : request.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", itemReq.productId()));

            if (!product.hasStock(itemReq.quantity())) {
                throw new BusinessException(
                    "Insufficient stock for product '" + product.getName() +
                    "'. Available: " + product.getStockQuantity() + ", requested: " + itemReq.quantity()
                );
            }

            product.decreaseStock(itemReq.quantity());
            productRepository.save(product);

            order.addItem(OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.quantity())
                    .unitPrice(product.getPrice())
                    .build());
        }

        return toResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        return orderRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> listOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> listByCustomer(String email, Pageable pageable) {
        return orderRepository.findByCustomerEmail(email, pageable).map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> listByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable).map(this::toSummary);
    }

    // used by the controller to populate the X-Total-Revenue header
    @Transactional(readOnly = true)
    public BigDecimal totalRevenue() {
        return orderRepository.sumTotalRevenue();
    }

    @Transactional
    public OrderResponse updateStatus(Long id, UpdateStatusRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));

        OrderStatus previous = order.getStatus();
        validateStatusTransition(previous, request.status());
        order.setStatus(request.status());

        // give stock back when cancelled
        if (request.status() == OrderStatus.CANCELLED) {
            order.getItems().forEach(item -> {
                item.getProduct().increaseStock(item.getQuantity());
                productRepository.save(item.getProduct());
            });
        }

        Order saved = orderRepository.save(order);

        // save audit entry
        orderHistoryRepository.save(OrderHistory.builder()
                .order(saved)
                .fromStatus(previous)
                .toStatus(request.status())
                .changedBy(request.changedBy() != null ? request.changedBy() : "system")
                .build());

        return toResponse(saved);
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case PENDING    -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED  -> next == OrderStatus.PROCESSING || next == OrderStatus.CANCELLED;
            case PROCESSING -> next == OrderStatus.SHIPPED || next == OrderStatus.CANCELLED;
            case SHIPPED    -> next == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };

        if (!valid) {
            throw new BusinessException("Invalid status transition from " + current + " to " + next);
        }
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(i -> new OrderItemResponse(
                        i.getProduct().getId(),
                        i.getProduct().getName(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()))
                ))
                .toList();

        return new OrderResponse(
                order.getId(), order.getCustomerEmail(), order.getStatus(),
                items, order.getTotal(), order.getCreatedAt(), order.getUpdatedAt()
        );
    }

    private OrderSummaryResponse toSummary(Order order) {
        return new OrderSummaryResponse(
                order.getId(), order.getCustomerEmail(), order.getStatus(),
                order.getTotal(), order.getItems().size(), order.getCreatedAt()
        );
    }
}
