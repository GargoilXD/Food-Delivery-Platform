# Migration Decision Log

## 1) Domain Boundaries
- Split monolith into `auth`, `customer`, `restaurant`, `order`, and `delivery`.
- Kept data ownership local to each service DB.
- Removed monolith cross-entity traversal from DTO conversion paths.

## 2) Inter-Service Communication
- Adopted OpenFeign by service name through Eureka registration.
- Exposed dedicated internal snapshot endpoints for order validation.
- Removed hardcoded malformed Feign URL placeholders and used discovery names.

## 3) Event-Driven Delivery
- `order-service` publishes `OrderPlacedEvent` to `order.events` (`order.placed`).
- `delivery-service` consumes event to create assignment asynchronously.
- Added idempotency guard (`findByOrderId` before insert) in delivery consumer.

## 4) Gateway and Security
- Gateway routes normalized to `/api/customers/**`, `/api/restaurants/**`, `/api/orders/**`, `/api/deliveries/**`.
- JWT validation uses auth JWKS endpoint.
- Removed servlet-side shared dependency from gateway to avoid WebFlux/Spring MVC security bean conflicts.
- Added `RateLimiterFilter` (order = -1) targeting `/api/orders/**`; uses Resilience4j with a 20 req/s limit and returns HTTP 429 with `Retry-After` on breach.

## 5) Build and Runtime Packaging
- Standardized Dockerfiles to reactor-aware Maven builds (`-pl ... -am`) so parent/shared modules resolve in-container.
- Added per-service Postgres containers and environment wiring in compose.

## 6) Fault Tolerance
- Added baseline Resilience4j circuit breaker on order placement path with fallback error.
- Added resilience configuration entries under `order-microservice` settings.

## 7) Order/Delivery Separation
- Removed `Delivery` JPA entity, `DeliveryRepository`, and `DeliveryResponse` DTO from `order-microservice`.
- Removed delivery proxy endpoints (`GET /deliveries/by-order/{orderId}`, `PATCH /deliveries/{deliveryId}/status`) from `OrderController` — these belong exclusively to `delivery-microservice`.
- Removed inline delivery state mutations from `OrderService` (driver pool simulation, `createDeliveryForOrder`, status sync on cancel/deliver). Delivery lifecycle is fully event-driven via RabbitMQ.
- Added `DeliveryClient` Feign interface in `order-microservice` for any future typed calls to `delivery-service`.
- `OrderResponse` no longer includes `deliveryStatus`, `driverName`, or `driverPhone`; clients requiring delivery tracking should query `GET /deliveries/by-order/{orderId}` directly.
