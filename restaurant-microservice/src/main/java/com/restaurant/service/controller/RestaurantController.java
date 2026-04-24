package com.restaurant.service.controller;

import com.restaurant.service.dto.MenuItemRequest;
import com.restaurant.service.dto.MenuItemResponse;
import com.restaurant.service.dto.RestaurantRequest;
import com.restaurant.service.dto.RestaurantResponse;
import com.restaurant.service.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class RestaurantController {
    private final RestaurantService restaurantService;

    @PostMapping("/restaurants")
    public ResponseEntity<RestaurantResponse> createRestaurant(@RequestParam Long ownerId, @Valid @RequestBody RestaurantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.createRestaurant(ownerId, request));
    }

    @GetMapping("/restaurants/{id}")
    public ResponseEntity<RestaurantResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getById(id));
    }

    @GetMapping("/restaurants")
    public ResponseEntity<List<RestaurantResponse>> search(@RequestParam(required = false) String city, @RequestParam(required = false) String cuisineType) {
        if (city != null && !city.isBlank()) {
            return ResponseEntity.ok(restaurantService.searchByCity(city));
        }
        return ResponseEntity.ok(restaurantService.searchByCuisine(cuisineType));
    }

    @PostMapping("/restaurants/{restaurantId}/menu-items")
    public ResponseEntity<MenuItemResponse> addMenuItem(
            @PathVariable Long restaurantId,
            @RequestParam Long ownerId,
            @Valid @RequestBody MenuItemRequest request
    ) {
        return ResponseEntity.ok(restaurantService.addMenuItem(restaurantId, ownerId, request));
    }

    @GetMapping("/restaurants/{restaurantId}/menu-items")
    public ResponseEntity<List<MenuItemResponse>> getMenu(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(restaurantService.getMenu(restaurantId));
    }

    @PutMapping("/menu-items/{itemId}")
    public ResponseEntity<MenuItemResponse> updateMenuItem(
            @PathVariable Long itemId,
           @RequestParam Long ownerId,
           @RequestBody MenuItemRequest request
    ) {
        return ResponseEntity.ok(restaurantService.updateMenuItem(itemId, ownerId, request));
    }

    @PatchMapping("/menu-items/{itemId}/toggle")
    public ResponseEntity<Void> toggleItemAvailability(@PathVariable Long itemId, @RequestParam Long ownerId) {
        restaurantService.toggleMenuItemAvailability(itemId, ownerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/restaurants/snapshot/{id}")
    public ResponseEntity<RestaurantService.RestaurantSnapshot> restaurantSnapshot(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getRestaurantSnapshot(id));
    }

    @GetMapping("/menu-items/snapshot/{id}")
    public ResponseEntity<RestaurantService.MenuItemSnapshot> menuItemSnapshot(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getMenuItemSnapshot(id));
    }
}
