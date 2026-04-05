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

    // counts orders created in a date range for a given status
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :from AND :to AND o.status = :status")
    long countByPeriodAndStatus(Instant from, Instant to, OrderStatus status);

    // sums revenue only for delivered orders (the only "real" money)
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.createdAt BETWEEN :from AND :to AND o.status = 'DELIVERED'")
    BigDecimal sumRevenueByPeriod(Instant from, Instant to);

    // counts all orders in the period regardless of status
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :from AND :to")
    long countByPeriod(Instant from, Instant to);

    // groups orders by status with their revenue for the breakdown table
    @Query("SELECT o.status, COUNT(o), COALESCE(SUM(o.total), 0) FROM Order o WHERE o.createdAt BETWEEN :from AND :to GROUP BY o.status")
    List<Object[]> revenueBreakdownByStatus(Instant from, Instant to);
}
