package com.boxshop.orders.service;

import com.boxshop.orders.dto.ProductDTOs.*;
import com.boxshop.orders.exception.BusinessException;
import com.boxshop.orders.exception.ResourceNotFoundException;
import com.boxshop.orders.model.Product;
import com.boxshop.orders.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    // products with stock at or below this value will show in low-stock alerts
    private static final int LOW_STOCK_THRESHOLD = 5;

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        // SKU must be unique across all products
        if (productRepository.existsBySku(request.sku())) {
            throw new BusinessException("SKU already exists: " + request.sku());
        }

        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stockQuantity(request.stockQuantity())
                .sku(request.sku())
                .build();

        return toResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return productRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> list(String name, Pageable pageable) {
        // filters by name if provided, otherwise returns everything
        if (name != null && !name.isBlank()) {
            return productRepository.findByNameContainingIgnoreCase(name, pageable).map(this::toResponse);
        }
        return productRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public ProductResponse updateStock(Long id, UpdateStockRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        product.setStockQuantity(request.stockQuantity());
        return toResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getLowStockAlerts() {
        return productRepository.findLowStockProducts(LOW_STOCK_THRESHOLD)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // converts Product entity to response DTO
    private ProductResponse toResponse(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getStockQuantity(),
                p.getSku(),
                p.getCreatedAt()
        );
    }
}
