package com.order.service.service;

import com.order.service.client.CustomerClient;
import com.order.service.client.RestaurantClient;
import com.order.service.dto.*;
import com.order.service.model.Delivery;
import com.order.service.model.Order;
import com.order.service.model.OrderItem;
import com.order.service.repository.DeliveryRepository;
import com.order.service.repository.OrderRepository;
import com.shared.definitions.exception.ResourceNotFoundException;
import com.shared.definitions.exception.UnauthorizedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final DeliveryRepository deliveryRepository;
    private final CustomerClient customerClient; // Feign
    private final RestaurantClient restaurantClient; // Feign
    private final RabbitTemplate rabbitTemplate;

    // Simulated driver pool (replace with Driver Service later)
    private static final String[] DRIVERS = {"Carlos M.", "Sarah J.", "Mike C.", "Priya P.", "James W."};
    private static final String[] PHONES = {"+1-555-0101", "+1-555-0102", "+1-555-0103", "+1-555-0104", "+1-555-0105"};

    public OrderService(OrderRepository orderRepository,
                        DeliveryRepository deliveryRepository,
                        CustomerClient customerClient,
                        RestaurantClient restaurantClient,
                        RabbitTemplate rabbitTemplate) {
        this.orderRepository = orderRepository;
        this.deliveryRepository = deliveryRepository;
        this.customerClient = customerClient;
        this.restaurantClient = restaurantClient;
        this.rabbitTemplate = rabbitTemplate;
    }

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

        Delivery delivery = createDeliveryForOrder(savedOrder, customer.deliveryAddress(), restaurant.address());
        savedOrder.setDelivery(delivery);

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

    private Delivery createDeliveryForOrder(Order order, String customerAddress, String restaurantAddress) {
        int driverIndex = (int) (Math.random() * DRIVERS.length);
        Delivery delivery = new Delivery();
        delivery.setStatus(Delivery.DeliveryStatus.ASSIGNED);
        delivery.setDriverName(DRIVERS[driverIndex]);
        delivery.setDriverPhone(PHONES[driverIndex]);
        delivery.setPickupAddress(restaurantAddress);
        delivery.setDeliveryAddress(order.getDeliveryAddress());
        delivery.setAssignedAt(LocalDateTime.now());
        Delivery saved = deliveryRepository.save(delivery);
        saved.setOrder(order);
        log.info("Delivery assigned: orderId={}, driver={}", order.getId(), DRIVERS[driverIndex]);
        return saved;
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

        Order.OrderStatus newStatus = Order.OrderStatus.valueOf(status.toUpperCase());
        order.setStatus(newStatus);

        if (newStatus == Order.OrderStatus.DELIVERED && order.getDelivery() != null) {
            Delivery d = order.getDelivery();
            d.setStatus(Delivery.DeliveryStatus.DELIVERED);
            d.setDeliveredAt(LocalDateTime.now());
            deliveryRepository.save(d);
        }

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

        if (order.getDelivery() != null) {
            Delivery d = order.getDelivery();
            d.setStatus(Delivery.DeliveryStatus.FAILED);
            deliveryRepository.save(d);
        }

        Order saved = orderRepository.save(order);

        rabbitTemplate.convertAndSend("order.events", "order.cancelled", new OrderCancelledEvent(
                saved.getId(), saved.getCustomerId(), saved.getRestaurantId()
        ));

        log.info("Order cancelled: orderId={}", orderId);
        return OrderResponse.fromEntity(saved);
    }

    // Delivery-specific endpoints (since Delivery is in same service)
    @Transactional(readOnly = true)
    public DeliveryResponse getDeliveryByOrderId(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        if (order.getDelivery() == null) {
            throw new ResourceNotFoundException("Delivery", "orderId", orderId);
        }
        return DeliveryResponse.fromEntity(order.getDelivery());
    }

    @Transactional
    public DeliveryResponse updateDeliveryStatus(Long deliveryId, String status) {
        log.info("Updating delivery status: deliveryId={}, newStatus={}", deliveryId, status);
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", "id", deliveryId));

        Delivery.DeliveryStatus newStatus = Delivery.DeliveryStatus.valueOf(status.toUpperCase());
        delivery.setStatus(newStatus);

        if (newStatus == Delivery.DeliveryStatus.PICKED_UP) {
            delivery.setPickedUpAt(LocalDateTime.now());
        } else if (newStatus == Delivery.DeliveryStatus.DELIVERED) {
            delivery.setDeliveredAt(LocalDateTime.now());
            if (delivery.getOrder() != null) {
                delivery.getOrder().setStatus(Order.OrderStatus.DELIVERED);
                orderRepository.save(delivery.getOrder());
            }
        }

        return DeliveryResponse.fromEntity(deliveryRepository.save(delivery));
    }
}
