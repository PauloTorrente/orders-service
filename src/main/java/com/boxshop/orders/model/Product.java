package com.boxshop.orders.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public boolean hasStock(int quantity) {
        return this.stockQuantity >= quantity;
    }

    // throws if not enough stock, otherwise subtracts
    public void decreaseStock(int quantity) {
        if (!hasStock(quantity)) {
            throw new IllegalStateException(
                "Insufficient stock for product: " + this.name +
                ". Available: " + this.stockQuantity + ", requested: " + quantity
            );
        }
        this.stockQuantity -= quantity;
    }

    // used when an order is cancelled to restore stock
    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;
    }
}
