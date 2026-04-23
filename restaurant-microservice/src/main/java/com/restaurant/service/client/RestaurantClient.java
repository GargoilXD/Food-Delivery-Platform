package com.restaurant.service.client;

import com.restaurant.service.service.RestaurantService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "restaurant-service")
public interface RestaurantClient {
    @GetMapping("/restaurants/snapshot/{id}")
    RestaurantService.RestaurantSnapshot getRestaurantSnapshot(@PathVariable Long id);

    @GetMapping("/menu-items/snapshot/{id}")
    RestaurantService.MenuItemSnapshot getMenuItemSnapshot(@PathVariable Long id);
}
