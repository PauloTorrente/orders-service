package com.boxshop.orders.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

// records every status change so we have a full audit trail of the order lifecycle
@Entity
@Table(name = "order_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus toStatus;

    // who triggered the change (email, system, etc.)
    @Column(nullable = false)
    private String changedBy;

    @Column(nullable = false, updatable = false)
    private Instant changedAt;

    @PrePersist
    public void prePersist() {
        this.changedAt = Instant.now();
    }
}
