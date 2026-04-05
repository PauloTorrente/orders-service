package com.boxshop.orders.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.boxshop.orders.dto.OrderDTOs.*;
import com.boxshop.orders.model.Product;
import com.boxshop.orders.repository.OrderRepository;
import com.boxshop.orders.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// boots the full Spring context and runs real HTTP cycles
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("OrderController integration tests")
class OrderControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;
    @Autowired ProductRepository productRepository;
    @Autowired OrderRepository orderRepository;

    private Product product;

    // @Transactional rolls back after each test so they don't interfere
    @BeforeEach
    void setUp() {
        product = productRepository.save(Product.builder()
                .name("Test Sneaker").sku("TEST-" + System.currentTimeMillis())
                .price(new BigDecimal("199.90")).stockQuantity(10)
                .description("Test product").build());
    }

    @Test
    @DisplayName("POST /api/v1/orders → 201 with correct total")
    void shouldCreateOrderAndReturn201() throws Exception {
        var request = new CreateOrderRequest("customer@test.com",
                List.of(new OrderItemRequest(product.getId(), 2)));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerEmail").value("customer@test.com"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.total").value(399.80))
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Test
    @DisplayName("POST /api/v1/orders → 422 when stock insufficient")
    void shouldReturn422WhenStockInsufficient() throws Exception {
        var request = new CreateOrderRequest("customer@test.com",
                List.of(new OrderItemRequest(product.getId(), 999)));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail", containsString("Insufficient stock")));
    }

    @Test
    @DisplayName("POST /api/v1/orders → 400 when email invalid")
    void shouldReturn400WhenEmailInvalid() throws Exception {
        var request = new CreateOrderRequest("not-an-email",
                List.of(new OrderItemRequest(product.getId(), 1)));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.customerEmail").exists());
    }

    @Test
    @DisplayName("GET /api/v1/orders → has X-Total-Revenue header")
    void shouldReturnOrdersWithRevenueHeader() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Total-Revenue"))
                .andExpect(header().exists("X-Total-Orders"))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("PATCH /api/v1/orders/{id}/status → 422 on invalid transition")
    void shouldReturn422OnInvalidStatusTransition() throws Exception {
        var createReq = new CreateOrderRequest("test@test.com",
                List.of(new OrderItemRequest(product.getId(), 1)));

        String body = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long orderId = mapper.readTree(body).get("id").asLong();

        var updateReq = new UpdateStatusRequest(
                com.boxshop.orders.model.OrderStatus.DELIVERED, "test");

        mockMvc.perform(patch("/api/v1/orders/{id}/status", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateReq)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail", containsString("Invalid status transition")));
    }

    @Test
    @DisplayName("GET /api/v1/products/low-stock-alerts → returns low stock items")
    void shouldReturnLowStockProducts() throws Exception {
        productRepository.save(Product.builder()
                .name("Low Stock Item").sku("LOW-" + System.currentTimeMillis())
                .price(new BigDecimal("50.00")).stockQuantity(2).build());

        mockMvc.perform(get("/api/v1/products/low-stock-alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }
}
