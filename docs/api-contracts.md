# API Contracts

## Auth Service (`/auth/**`)
- `POST /auth/register` -> register user, returns JWT payload.
- `POST /auth/login` -> authenticate user, returns JWT payload.
- `GET /auth/profile` -> authenticated profile details.
- `GET /auth/users/exists/{id}` -> internal existence check.
- `GET /.well-known/jwks.json` -> JWT keyset for gateway validation.

## Customer Service (`/api/customers/**` via gateway)
- `POST /customers` -> create customer profile.
- `GET /customers/{id}` -> fetch customer by id.
- `GET /customers/by-user?userId=...` -> fetch customer by auth user id.
- `GET /customers/snapshot/{id}` -> internal snapshot endpoint.

## Restaurant Service (`/api/restaurants/**` and `/api/menu-items/**`)
- `POST /restaurants?ownerId=...` -> create restaurant (owner only).
- `GET /restaurants/{id}` -> restaurant details.
- `GET /restaurants?city=...` or `?cuisineType=...` -> search restaurants.
- `POST /restaurants/{restaurantId}/menu-items?ownerId=...` -> add menu item.
- `GET /restaurants/{restaurantId}/menu-items` -> list active menu items.
- `PUT /menu-items/{itemId}?ownerId=...` -> update menu item.
- `PATCH /menu-items/{itemId}/toggle?ownerId=...` -> toggle availability.
- `GET /restaurants/snapshot/{id}` -> internal snapshot for order validation.
- `GET /menu-items/snapshot/{id}` -> internal menu item snapshot.

## Order Service (`/api/orders/**`)
- `POST /orders?customerId=...` -> place order.
- `GET /orders/{id}` -> get order by id.
- `GET /orders?customerId=...` -> list customer orders.
- `GET /orders/restaurants/{restaurantId}` -> list restaurant orders.
- `PATCH /orders/{id}/status?status=...` -> update order status.
- `POST /orders/{id}/cancel?customerId=...` -> cancel order.

## Delivery Service (`/api/deliveries/**`)
- `GET /deliveries/{id}` -> fetch delivery by id.
- `GET /deliveries/by-order/{orderId}` -> fetch delivery assigned to order.
- `PATCH /deliveries/{id}/status?status=...` -> update delivery status.
