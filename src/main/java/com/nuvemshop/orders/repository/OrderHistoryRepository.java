package com.nuvemshop.orders.repository;

import com.nuvemshop.orders.model.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {

    // returns full audit trail for a given order, oldest first
    List<OrderHistory> findByOrderIdOrderByChangedAtAsc(Long orderId);
}
