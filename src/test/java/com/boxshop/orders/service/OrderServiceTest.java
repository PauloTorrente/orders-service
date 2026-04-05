package com.boxshop.orders.service;

import com.boxshop.orders.dto.OrderDTOs.*;
import com.boxshop.orders.exception.BusinessException;
import com.boxshop.orders.exception.ResourceNotFoundException;
import com.boxshop.orders.model.Order;
import com.boxshop.orders.model.OrderStatus;
import com.boxshop.orders.model.Product;
import com.boxshop.orders.repository.OrderRepository;
import com.boxshop.orders.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Mockito replaces real repositories with fakes so tests don't touch the database
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService unit tests")
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock ProductRepository productRepository;
    @InjectMocks OrderService orderService;

    private Product product;

    // sets up a reusable product before each test
    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("Camiseta Básica")
                .sku("CAM-001")
                .price(new BigDecimal("49.90"))
                .stockQuantity(10)
                .build();
    }

    @Test
    @DisplayName("createOrder: should create order and decrease stock")
    void shouldCreateOrderAndDecreaseStock() {
        var request = new CreateOrderRequest("customer@test.com",
                List.of(new OrderItemRequest(1L, 2)));

        Order savedOrder = Order.builder()
                .id(1L)
                .customerEmail("customer@test.com")
                .items(new ArrayList<>())
                .total(BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenReturn(product);
        when(orderRepository.save(any())).thenReturn(savedOrder);

        var response = orderService.createOrder(request);

        assertThat(response).isNotNull();
        assertThat(response.customerEmail()).isEqualTo("customer@test.com");
        // confirms stock was actually saved after decreasing
        verify(productRepository, times(1)).save(product);
    }

    @Test
    @DisplayName("createOrder: should throw when product not found")
    void shouldThrowWhenProductNotFound() {
        var request = new CreateOrderRequest("customer@test.com",
                List.of(new OrderItemRequest(99L, 1)));

        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("createOrder: should throw when insufficient stock")
    void shouldThrowWhenInsufficientStock() {
        product.setStockQuantity(1);
        var request = new CreateOrderRequest("customer@test.com",
                List.of(new OrderItemRequest(1L, 5)));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    @DisplayName("updateStatus: should reject invalid status transition")
    void shouldRejectInvalidStatusTransition() {
        // DELIVERED is a terminal state, can't transition from it
        Order order = Order.builder()
                .id(1L)
                .status(OrderStatus.DELIVERED)
                .items(new ArrayList<>())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1L, new UpdateStatusRequest(OrderStatus.CANCELLED, "system")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    @DisplayName("updateStatus: cancelling order should restore stock")
    void shouldRestoreStockWhenCancelled() {
        Order order = Order.builder()
                .id(1L)
                .status(OrderStatus.PENDING)
                .items(new ArrayList<>())
                .total(BigDecimal.ZERO)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        var response = orderService.updateStatus(1L, new UpdateStatusRequest(OrderStatus.CANCELLED, "system"));

        assertThat(response).isNotNull();
    }
}
