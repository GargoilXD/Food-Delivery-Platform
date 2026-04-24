# Migration Decision Log

## 1) Project Scaffolding
- Bootstrapped a multi-module Maven project: `api-gateway`, `auth-microservice`, `customer-microservice`, `delivery-microservice`, `order-microservice`, `restaurant-microservice`, `eureka-server`, and `shared`.
- Added per-service `Dockerfile`s and a root `docker-compose.yaml` for container orchestration.

## 2) Service Configuration
- Added `application.yml` for every service: PostgreSQL datasource per service DB (`auth_db`, `customer_db`, `order_db`, `delivery_db`, `restaurant_db`), RabbitMQ broker connection, and Eureka registration.
- Used environment variable placeholders (`${DB_URL:...}`) throughout to support both local and containerised runs.
- Extended `docker-compose.yaml` with per-service Postgres containers and environment wiring.

## 3) API Gateway: Security and Rate Limiting
- Added `GatewaySecurityConfig` with WebFlux-based JWT validation against the auth service JWKS endpoint.
- Implemented `RateLimiterFilter` (order = -1) using Resilience4j targeting `/api/orders/**`; enforces 20 req/s and returns HTTP 429 with a `Retry-After` header on breach.

## 4) Auth Service: JWT Authentication
- Implemented RSA-signed JWT tokens via `JwtTokenProvider`.
- Added `CustomUserDetailsService`, `UserRepository`, `UserService`, and `AuthController` with `POST /auth/register` and `POST /auth/login`.
- Exposed `GET /.well-known/jwks.json` via `JwksController` for gateway-side token validation.
- Added `AuthClient` Feign interface for internal user-existence checks.
- Centralised exception types (`ResourceNotFoundException`, `UnauthorizedException`, `DuplicateResourceException`) with a `GlobalExceptionHandler`.

## 5) Domain Models and Repositories
- Added JPA entities across services: `Customer`, `Order`, `OrderItem`, `Delivery` (in both order and delivery services), `Restaurant`, `MenuItem`.
- Defined `JpaRepository` interfaces with custom query methods (`findByCustomerId`, `findByOrderId`, etc.).
- Added shared exception classes under the `shared` module for reuse across services.

## 6) Customer Service
- Implemented `CustomerService` with create and lookup operations, publishing a `CustomerCreatedEvent` to RabbitMQ.
- Added `CustomerController` exposing `POST /customers`, `GET /customers/{id}`, `GET /customers/by-user`, and `GET /customers/snapshot/{id}`.
- Moved `RabbitConfig` and `SecurityConfig` from a nested `client/config` package to the top-level `config` package.

## 7) Delivery Service: Event-Driven Architecture
- Implemented `DeliveryService` with RabbitMQ listeners: `onOrderPlaced` (creates delivery entry) and `onOrderCancelled` (marks delivery as `FAILED`).
- Added `DeliveryController` exposing `GET /deliveries/{id}`, `GET /deliveries/by-order/{orderId}`, and `PATCH /deliveries/{id}/status`.
- Configured dead-letter queues (DLQ) for failed message handling.
- `DeliveryService` publishes a `DeliveryStatusUpdatedEvent` on every status change.

## 8) Order and Restaurant Services
- Implemented `OrderService` with order placement, status updates, and cancellation; published `OrderPlacedEvent` and `OrderCancelledEvent` to RabbitMQ.
- Added `CustomerClient` and `RestaurantClient` Feign interfaces in order service for snapshot calls at order time.
- Added `RestaurantController`, restaurant/menu-item DTOs, and `RestaurantService`.
- At this stage, `OrderService` also embedded a local `Delivery` JPA entity with simulated driver assignment: a known coupling issue to be resolved later.

## 9) RabbitMQ and Shared Module Fixes
- Fixed inter-service RabbitMQ communication by correcting `docker-compose.yaml` broker wiring.
- Added `SharedAutoConfiguration` and `AutoConfiguration.imports` so the `shared` module's `GlobalExceptionHandler` is picked up automatically by all Spring Boot services without explicit component scanning.

## 10) Order/Delivery Decoupling
- Removed `Delivery` JPA entity, `DeliveryRepository`, and `DeliveryResponse` DTO from `order-microservice`.
- Removed delivery proxy endpoints (`GET /deliveries/by-order/{orderId}`, `PATCH /deliveries/{deliveryId}/status`) from `OrderController`: these belong exclusively to `delivery-microservice`.
- Removed inline delivery state mutations from `OrderService`: driver pool simulation, `createDeliveryForOrder`, and delivery status sync on cancel/deliver. Delivery lifecycle is fully event-driven via RabbitMQ.
- Added `DeliveryClient` Feign interface in `order-microservice` for future typed calls to the delivery service.
- `OrderResponse` no longer includes `deliveryStatus`, `driverName`, or `driverPhone`; clients requiring delivery tracking should query `GET /deliveries/by-order/{orderId}` on the delivery service directly.
- Added initial documentation: `README.md`, `docs/api-contracts.md`, `docs/architecture.md`, `docs/migration-log.md`, and a Postman collection.

## 11) Codebase Cleanup and Documentation
- Replaced manual constructors with `@RequiredArgsConstructor` (Lombok) across all services.
- Stripped verbose Lombok annotations (`@Data`, `@Builder`, etc.) from model/DTO classes in favour of plain getters/setters and records where appropriate.
- Corrected restaurant orders endpoint path from `GET /restaurants/{restaurantId}/orders` to `GET /orders/restaurants/{restaurantId}`.
- Updated `docs/api-contracts.md` and `docs/architecture.md` to reflect decoupled delivery ownership and the new `DeliveryClient` Feign edge.
