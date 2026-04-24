package com.order.service.service;

import com.order.service.client.CustomerClient;
import com.order.service.client.RestaurantClient;
import com.order.service.dto.*;
import com.order.service.model.Order;
import com.order.service.model.OrderItem;
import com.order.service.repository.OrderRepository;
import com.shared.definitions.exception.ResourceNotFoundException;
import com.shared.definitions.exception.UnauthorizedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CustomerClient customerClient;
    private final RestaurantClient restaurantClient;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    @CircuitBreaker(name = "customerService", fallbackMethod = "placeOrderFallback")
    public OrderResponse placeOrder(Long customerId, PlaceOrderRequest request) {
        log.info("Placing order: customerId={}, restaurantId={}", customerId, request.getRestaurantId());
        var customer = customerClient.getCustomerSnapshot(customerId);
        var restaurant = restaurantClient.getRestaurantSnapshot(request.getRestaurantId());

        if (!restaurant.active()) {
            log.warn("Order rejected - restaurant not accepting orders: restaurantId={}", request.getRestaurantId());
            throw new IllegalStateException("Restaurant is currently not accepting orders");
        }

        Order order = new Order();
        order.setCustomerId(customerId);
        order.setRestaurantId(request.getRestaurantId());
        order.setCustomerUsername(customer.username());
        order.setRestaurantName(restaurant.name());
        order.setDeliveryAddress(request.getDeliveryAddress() != null
                ? request.getDeliveryAddress()
                : customer.deliveryAddress());
        order.setSpecialInstructions(request.getSpecialInstructions());
        order.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(restaurant.estimatedDeliveryMinutes()));

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemReq : request.getItems()) {
            var menuItem = restaurantClient.getMenuItemSnapshot(itemReq.getMenuItemId());

            if (!menuItem.available()) {
                log.warn("Order rejected - menu item unavailable: menuItemId={}, name={}", menuItem.id(), menuItem.name());
                throw new IllegalStateException("Menu item '" + menuItem.name() + "' is not available");
            }
            if (!menuItem.restaurantId().equals(request.getRestaurantId())) {
                log.warn("Order rejected - menu item does not belong to restaurant: menuItemId={}, restaurantId={}", menuItem.id(), request.getRestaurantId());
                throw new IllegalStateException("Menu item does not belong to this restaurant");
            }

            BigDecimal subtotal = menuItem.price().multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenuItemId(menuItem.id());
            orderItem.setMenuItemName(menuItem.name());
            orderItem.setQuantity(itemReq.getQuantity());
            orderItem.setUnitPrice(menuItem.price());
            orderItem.setSubtotal(subtotal);
            orderItem.setSpecialInstructions(itemReq.getSpecialInstructions());

            order.getItems().add(orderItem);
            total = total.add(subtotal);
        }

        order.setTotalAmount(total);
        Order savedOrder = orderRepository.save(order);

        rabbitTemplate.convertAndSend("order.events", "order.placed", new OrderPlacedEvent(
                savedOrder.getId(), savedOrder.getCustomerId(), savedOrder.getRestaurantId(), total
        ));

        log.info("Order placed successfully: orderId={}, customerId={}, total={}", savedOrder.getId(), customerId, total);
        return OrderResponse.fromEntity(savedOrder);
    }

    private OrderResponse placeOrderFallback(Long customerId, PlaceOrderRequest request, Throwable throwable) {
        log.warn("Order placement circuit breaker triggered: customerId={}, cause={}", customerId, throwable.getMessage());
        throw new IllegalStateException("Unable to place order because a dependent service is unavailable", throwable);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return OrderResponse.fromEntity(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getCustomerOrders(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream().map(OrderResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getRestaurantOrders(Long restaurantId) {
        return orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId)
                .stream().map(OrderResponse::fromEntity).toList();
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String status) {
        log.info("Updating order status: orderId={}, newStatus={}", orderId, status);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.setStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
        return OrderResponse.fromEntity(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long customerId) {
        log.info("Cancelling order: orderId={}, customerId={}", orderId, customerId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getCustomerId().equals(customerId)) {
            log.warn("Unauthorized cancel attempt: orderId={}, requesterId={}", orderId, customerId);
            throw new UnauthorizedException("You can only cancel your own orders");
        }

        if (order.getStatus() != Order.OrderStatus.PLACED
                && order.getStatus() != Order.OrderStatus.CONFIRMED) {
            log.warn("Cannot cancel order in status {}: orderId={}", order.getStatus(), orderId);
            throw new IllegalStateException("Cannot cancel order in status: " + order.getStatus());
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);

        rabbitTemplate.convertAndSend("order.events", "order.cancelled", new OrderCancelledEvent(
                saved.getId(), saved.getCustomerId(), saved.getRestaurantId()
        ));

        log.info("Order cancelled: orderId={}", orderId);
        return OrderResponse.fromEntity(saved);
    }
}
