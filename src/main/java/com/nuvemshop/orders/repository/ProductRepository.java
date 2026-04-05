package com.nuvemshop.orders.repository;

import com.nuvemshop.orders.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    // used to check for duplicate SKUs before saving a new product
    boolean existsBySku(String sku);

    // returns products below the threshold sorted by lowest stock first
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= :threshold ORDER BY p.stockQuantity ASC")
    List<Product> findLowStockProducts(int threshold);

    // case-insensitive partial search on product name
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
