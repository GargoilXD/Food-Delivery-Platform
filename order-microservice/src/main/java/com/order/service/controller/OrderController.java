package com.order.service.controller;

import com.order.service.dto.DeliveryResponse;
import com.order.service.dto.OrderResponse;
import com.order.service.dto.PlaceOrderRequest;
import com.order.service.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> placeOrder(@RequestParam Long customerId, @Valid @RequestBody PlaceOrderRequest request) {
        return ResponseEntity.ok(orderService.placeOrder(customerId, request));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getCustomerOrders(@RequestParam Long customerId) {
        return ResponseEntity.ok(orderService.getCustomerOrders(customerId));
    }

    @GetMapping("/restaurants/{restaurantId}/orders")
    public ResponseEntity<List<OrderResponse>> getRestaurantOrders(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(orderService.getRestaurantOrders(restaurantId));
    }

    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    @PostMapping("/orders/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long id, @RequestParam Long customerId) {
        return ResponseEntity.ok(orderService.cancelOrder(id, customerId));
    }

    @GetMapping("/deliveries/by-order/{orderId}")
    public ResponseEntity<DeliveryResponse> getDeliveryByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getDeliveryByOrderId(orderId));
    }

    @PatchMapping("/deliveries/{deliveryId}/status")
    public ResponseEntity<DeliveryResponse> updateDeliveryStatus(@PathVariable Long deliveryId, @RequestParam String status) {
        return ResponseEntity.ok(orderService.updateDeliveryStatus(deliveryId, status));
    }
}
