# Architecture Diagram

![alt text](diagram.png)
```mermaid
flowchart TD
    client[Client]
    gateway[API Gateway 8080]
    eureka[Eureka 8761]
    rabbit[RabbitMQ 5672]

    client --> gateway

    gateway --> auth[Auth Service 8081]
    gateway --> customer[Customer Service 8082]
    gateway --> order[Order Service 8083]
    gateway --> restaurant[Restaurant Service 8084]
    gateway --> delivery[Delivery Service 8085]

    auth --> authDb[(auth_db)]
    customer --> customerDb[(customer_db)]
    order --> orderDb[(order_db)]
    restaurant --> restaurantDb[(restaurant_db)]
    delivery --> deliveryDb[(delivery_db)]

    auth --> eureka
    customer --> eureka
    order --> eureka
    restaurant --> eureka
    delivery --> eureka
    gateway --> eureka

    customer -.->|"AuthClient (Feign)"| auth
    order -.->|"CustomerClient (Feign)"| customer
    order -.->|"RestaurantClient (Feign)"| restaurant
    order -.->|"DeliveryClient (Feign)"| delivery
    restaurant -.->|"CustomerClient (Feign)"| customer

    order -->|"order.placed / order.cancelled"| rabbit
    customer -->|"customer.created"| rabbit
    delivery -->|"delivery.status.updated"| rabbit
    rabbit -->|"order.placed / order.cancelled"| delivery
```

## Notes
- External traffic enters through the gateway.
- Inter-service lookup uses Eureka service names (dashed lines = Feign HTTP calls via Eureka).
- Delivery assignment is asynchronous: the delivery service listens to `order.placed` and `order.cancelled` queues on RabbitMQ.
- `delivery.status.updated` is published by the delivery service but has no consumer yet.
- `customer.created` is published by the customer service but has no consumer yet.
- `RateLimiterFilter` enforces a Resilience4j rate limit (20 req/s) on all `/api/orders/**` requests; excess requests receive HTTP 429 with a `Retry-After` header.
