package com.nuvemshop.orders.repository;

import com.nuvemshop.orders.model.Order;
import com.nuvemshop.orders.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

// Spring Data generates the SQL for these methods based on the method name
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByCustomerEmail(String customerEmail, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    // custom JPQL query to count orders by status
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatus(OrderStatus status);

    // total revenue across all delivered orders (the only real money)
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.status = 'DELIVERED'")
    BigDecimal sumTotalRevenue();
}
