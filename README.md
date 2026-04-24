# Food Delivery Platform (Microservices)

This repository contains a refactored microservices backend split from the original monolith.

## Services
- `api-gateway` (`:8080`)
- `auth-microservice` (`:8081`)
- `customer-microservice` (`:8082`)
- `order-microservice` (`:8083`)
- `restaurant-microservice` (`:8084`)
- `delivery-microservice` (`:8085`)
- `eureka-server` (`:8761`)

## Infrastructure
- RabbitMQ (`:5672`, management `:15672`)
- Dedicated PostgreSQL instance per service (`auth_db`, `customer_db`, `order_db`, `restaurant_db`, `delivery_db`)

## Run
```bash
docker compose up --build
```

## Gateway Routes
- `/auth/**` -> `auth-service`
- `/api/customers/**` -> `customer-service`
- `/api/restaurants/**` and `/api/menu-items/**` -> `restaurant-service`
- `/api/orders/**` -> `order-service` _(rate limited: 20 req/s; HTTP 429 on breach)_
- `/api/deliveries/**` -> `delivery-service`

## Event Flow
- `order-service` publishes `OrderPlacedEvent` to `order.events` (`order.placed`)
- `delivery-service` consumes and creates delivery assignment asynchronously

## Basic Verification
1. Register/login user via gateway auth route.
2. Create customer profile.
3. Query restaurant/menu snapshots.
4. Place order.
5. Query delivery by order id.
