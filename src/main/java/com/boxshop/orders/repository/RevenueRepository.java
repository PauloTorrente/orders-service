package com.boxshop.orders.repository;

import com.boxshop.orders.model.Order;
import com.boxshop.orders.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface RevenueRepository extends JpaRepository<Order, Long> {

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :from AND :to AND o.status = :status")
    long countByPeriodAndStatus(Instant from, Instant to, OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.createdAt BETWEEN :from AND :to AND o.status = 'DELIVERED'")
    BigDecimal sumRevenueByPeriod(Instant from, Instant to);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :from AND :to")
    long countByPeriod(Instant from, Instant to);

    @Query("SELECT o.status, COUNT(o), COALESCE(SUM(o.total), 0) FROM Order o WHERE o.createdAt BETWEEN :from AND :to GROUP BY o.status")
    List<Object[]> revenueBreakdownByStatus(Instant from, Instant to);

    @Query("""
        SELECT oi.product.id, oi.product.name, oi.product.sku,
               SUM(oi.quantity),
               SUM(oi.unitPrice * oi.quantity)
        FROM OrderItem oi
        JOIN oi.order o
        WHERE o.createdAt BETWEEN :from AND :to
          AND o.status = 'DELIVERED'
        GROUP BY oi.product.id, oi.product.name, oi.product.sku
        ORDER BY SUM(oi.quantity) DESC
        """)
    List<Object[]> topProductsByUnitsSold(Instant from, Instant to);

    @Query("SELECT o.status, COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :from AND :to GROUP BY o.status")
    List<Object[]> countByEachStatus(Instant from, Instant to);

    // daily revenue for delivered orders — powers the line chart
    @Query("""
        SELECT CAST(o.createdAt AS LocalDate),
               COUNT(o),
               SUM(o.total)
        FROM Order o
        WHERE o.createdAt BETWEEN :from AND :to
          AND o.status = 'DELIVERED'
        GROUP BY CAST(o.createdAt AS LocalDate)
        ORDER BY CAST(o.createdAt AS LocalDate)
        """)
    List<Object[]> revenueTimeline(Instant from, Instant to);

    // daily count for ALL statuses — powers the status bar chart
    @Query("""
        SELECT CAST(o.createdAt AS LocalDate),
               o.status,
               COUNT(o)
        FROM Order o
        WHERE o.createdAt BETWEEN :from AND :to
        GROUP BY CAST(o.createdAt AS LocalDate), o.status
        ORDER BY CAST(o.createdAt AS LocalDate)
        """)
    List<Object[]> ordersTimelineByStatus(Instant from, Instant to);
}
