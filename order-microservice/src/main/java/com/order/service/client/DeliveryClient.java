package com.order.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "delivery-service")
public interface DeliveryClient {
    @GetMapping("/deliveries/by-order/{orderId}")
    DeliverySnapshot getDeliveryByOrderId(@PathVariable Long orderId);

    record DeliverySnapshot(
            Long id, String status, String driverName, String driverPhone,
            String pickupAddress, String deliveryAddress
    ) {}
}
