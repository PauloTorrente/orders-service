package com.nuvemshop.orders.config;

import com.nuvemshop.orders.model.Product;
import com.nuvemshop.orders.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    // runs once on startup to populate the database with sample products
    @Bean
    CommandLineRunner seedData(ProductRepository productRepository) {
        return args -> {
            // skip seeding if data already exists
            if (productRepository.count() > 0) return;

            List<Product> products = List.of(
                Product.builder().name("Camiseta Básica Preta").sku("CAM-001").price(new BigDecimal("49.90")).stockQuantity(100).description("Camiseta 100% algodão").build(),
                Product.builder().name("Tênis Casual Branco").sku("TEN-001").price(new BigDecimal("189.90")).stockQuantity(50).description("Tênis confortável para o dia a dia").build(),
                Product.builder().name("Mochila Urbana 20L").sku("MOC-001").price(new BigDecimal("129.90")).stockQuantity(3).description("Mochila resistente à água").build(),
                Product.builder().name("Óculos de Sol UV400").sku("OCS-001").price(new BigDecimal("79.90")).stockQuantity(2).description("Proteção UV400 polarizado").build(),
                Product.builder().name("Caneca Térmica 500ml").sku("CAN-001").price(new BigDecimal("59.90")).stockQuantity(200).description("Mantém a temperatura por 12h").build()
            );

            productRepository.saveAll(products);
            log.info("✅ Sample products seeded: {} items", products.size());
            log.info("🚀 Swagger UI available at http://localhost:8080/swagger-ui.html");
        };
    }
}
