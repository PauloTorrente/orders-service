# 🛒 Nuvemshop Orders Service

> **Portfolio project** — RESTful API for e-commerce order and inventory management, built as a technical challenge for Nuvemshop.

---

## 🎯 What this project solves

Every e-commerce platform needs reliable order management. This service handles:

- **Order lifecycle** — from creation to delivery, with enforced status transitions
- **Automatic stock control** — stock decreases on order creation, restores on cancellation
- **Inventory alerts** — endpoint to detect products with critically low stock
- **Clean error handling** — structured Problem Detail responses (RFC 7807)

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     REST Controllers                     │
│         OrderController  │  ProductController           │
└──────────────┬───────────┴──────────────────────────────┘
               │
┌──────────────▼───────────────────────────────────────────┐
│                      Service Layer                        │
│          OrderService    │   ProductService               │
│   (business rules, stock control, status machine)         │
└──────────────┬───────────┴──────────────────────────────-┘
               │
┌──────────────▼────────────────────────────────────────────┐
│                   Repository Layer (JPA)                   │
│        OrderRepository   │   ProductRepository             │
└──────────────┬────────────┴───────────────────────────────-┘
               │
┌──────────────▼────────────┐
│    H2 (dev) / PostgreSQL   │
│         (production)       │
└────────────────────────────┘
```

---

## 📦 Tech stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.2 |
| Persistence | Spring Data JPA + Hibernate |
| Database | H2 (dev) → PostgreSQL (prod) |
| Validation | Jakarta Bean Validation |
| Documentation | SpringDoc OpenAPI (Swagger UI) |
| Tests | JUnit 5 + Mockito + AssertJ |
| Build | Maven |

---

## 🚀 Running locally

**Prerequisites:** Java 21+, Maven 3.9+

```bash
git clone https://github.com/your-username/nuvemshop-orders-service.git
cd nuvemshop-orders-service
mvn spring-boot:run
```

The app starts on `http://localhost:8080` with 5 sample products pre-loaded.

| Resource | URL |
|----------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| API Docs (JSON) | http://localhost:8080/api-docs |
| H2 Console | http://localhost:8080/h2-console |

---

## 📡 API endpoints

### Products

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/products` | Create product |
| `GET` | `/api/v1/products` | List (paginated, filter by name) |
| `GET` | `/api/v1/products/{id}` | Get by ID |
| `PATCH` | `/api/v1/products/{id}/stock` | Update stock quantity |
| `GET` | `/api/v1/products/low-stock-alerts` | Products with ≤5 units |

### Orders

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/orders` | Create order (decreases stock) |
| `GET` | `/api/v1/orders` | List (paginated, filter by email or status) |
| `GET` | `/api/v1/orders/{id}` | Get by ID |
| `PATCH` | `/api/v1/orders/{id}/status` | Update status |

---

## 🔄 Order status machine

```
PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
   ↓           ↓           ↓
CANCELLED   CANCELLED   CANCELLED  (restores stock)
```

Attempting an invalid transition returns `422 Unprocessable Entity`.

---

## 💡 Quick test (curl)

```bash
# 1. Create a product
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Camiseta","sku":"CAM-999","price":59.90,"stockQuantity":10}'

# 2. Create an order
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{"customerEmail":"cliente@loja.com","items":[{"productId":1,"quantity":2}]}'

# 3. Advance status
curl -X PATCH http://localhost:8080/api/v1/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{"status":"CONFIRMED"}'

# 4. Check low-stock alerts
curl http://localhost:8080/api/v1/products/low-stock-alerts
```

---

## 🧪 Running tests

```bash
mvn test
```

Tests cover: order creation, stock validation, insufficient stock rejection, invalid status transitions, and stock restoration on cancellation.

---

## 🔧 Production migration (PostgreSQL)

Replace H2 in `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ordersdb
spring.datasource.username=postgres
spring.datasource.password=yourpassword
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
spring.h2.console.enabled=false
```

---

## 📁 Project structure

```
src/
├── main/java/com/nuvemshop/orders/
│   ├── controller/      # REST endpoints (thin layer)
│   ├── service/         # Business logic & rules
│   ├── repository/      # JPA data access
│   ├── model/           # JPA entities + OrderStatus enum
│   ├── dto/             # Request/Response records
│   ├── exception/       # Custom exceptions + global handler
│   └── config/          # OpenAPI, data initializer
└── test/
    └── service/         # Unit tests (Mockito)
```

---

*Made with ☕ and Java 21 — [Paulo] — 2026*
