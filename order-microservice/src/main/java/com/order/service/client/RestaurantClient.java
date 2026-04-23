package com.order.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "restaurant-service")
public interface RestaurantClient {
    @GetMapping("/restaurants/snapshot/{id}")
    RestaurantSnapshot getRestaurantSnapshot(@PathVariable Long id);

    @GetMapping("/menu-items/snapshot/{id}")
    MenuItemSnapshot getMenuItemSnapshot(@PathVariable Long id);

    record RestaurantSnapshot(Long id, String name, String address, boolean active, int estimatedDeliveryMinutes) {}
    record MenuItemSnapshot(Long id, Long restaurantId, String name, java.math.BigDecimal price, boolean available) {}
}
