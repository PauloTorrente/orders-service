package com.boxshop.orders.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

// groups all product-related request and response records in one place
public class ProductDTOs {

    // request records (what the client sends)

    public record CreateProductRequest(
            @NotBlank(message = "Product name is required")
            @Size(min = 2, max = 255)
            String name,

            @Size(max = 1000)
            String description,

            @NotNull
            @DecimalMin(value = "0.01", message = "Price must be greater than zero")
            BigDecimal price,

            @NotNull
            @Min(value = 0, message = "Stock cannot be negative")
            Integer stockQuantity,

            @NotBlank(message = "SKU is required")
            String sku
    ) {}

    public record UpdateStockRequest(
            @NotNull
            @Min(value = 0, message = "Stock cannot be negative")
            Integer stockQuantity
    ) {}

    // response record (what the API returns)
    public record ProductResponse(
            Long id,
            String name,
            String description,
            BigDecimal price,
            Integer stockQuantity,
            String sku,
            Instant createdAt
    ) {}
}
