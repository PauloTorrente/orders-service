package com.boxshop.orders.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

public class ProductDTOs {

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
