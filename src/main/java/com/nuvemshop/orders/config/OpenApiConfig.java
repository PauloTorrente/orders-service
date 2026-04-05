package com.nuvemshop.orders.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// sets up the Swagger UI metadata visible at /swagger-ui.html
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Nuvemshop Orders Service API")
                        .description("""
                                RESTful API for order and inventory management.
                                
                                Key features:
                                - Order lifecycle management with validated status transitions
                                - Automatic stock control on order creation and cancellation
                                - Low-stock alerts for inventory monitoring
                                - Paginated endpoints for scalable listing
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Paulo")
                                .email("paulo@example.com")));
    }
}
