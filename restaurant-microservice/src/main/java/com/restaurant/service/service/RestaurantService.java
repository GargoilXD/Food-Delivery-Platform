package com.restaurant.service.service;

import com.restaurant.service.client.CustomerClient;
import com.restaurant.service.dto.MenuItemRequest;
import com.restaurant.service.dto.MenuItemResponse;
import com.restaurant.service.dto.RestaurantRequest;
import com.restaurant.service.dto.RestaurantResponse;
import com.restaurant.service.model.MenuItem;
import com.restaurant.service.model.Restaurant;
import com.restaurant.service.repository.MenuItemRepository;
import com.restaurant.service.repository.RestaurantRepository;
import com.shared.definitions.exception.ResourceNotFoundException;
import com.shared.definitions.exception.UnauthorizedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final CustomerClient customerClient;

    @Transactional
    @CircuitBreaker(name = "customerService", fallbackMethod = "createRestaurantFallback")
    public RestaurantResponse createRestaurant(Long ownerId, RestaurantRequest request) {
        log.info("Creating restaurant: ownerId={}, name={}", ownerId, request.getName());
        var ownerSnapshot = customerClient.getCustomerSnapshot(ownerId);
        if (ownerSnapshot == null) {
            throw new ResourceNotFoundException("Customer", "id", ownerId);
        }

        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setCuisineType(request.getCuisineType());
        restaurant.setAddress(request.getAddress());
        restaurant.setCity(request.getCity());
        restaurant.setPhone(request.getPhone());
        restaurant.setEstimatedDeliveryMinutes(request.getEstimatedDeliveryMinutes());
        restaurant.setOwnerId(ownerId);

        RestaurantResponse response = RestaurantResponse.fromEntity(restaurantRepository.save(restaurant));
        log.info("Restaurant created successfully: id={}, name={}", response.getId(), response.getName());
        return response;
    }

    @Transactional(readOnly = true)
    public RestaurantResponse getById(Long id) {
        return restaurantRepository.findById(id)
                .map(RestaurantResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", id));
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> searchByCity(String city) {
        return restaurantRepository.findByCityIgnoreCaseAndActiveTrue(city)
                .stream().map(RestaurantResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> searchByCuisine(String cuisineType) {
        return restaurantRepository.findByCuisineTypeIgnoreCaseAndActiveTrue(cuisineType)
                .stream().map(RestaurantResponse::fromEntity).toList();
    }

    // ---- Menu Item management ----

    @Transactional
    @CircuitBreaker(name = "customerService", fallbackMethod = "addMenuItemFallback")
    public MenuItemResponse addMenuItem(Long restaurantId, Long ownerId, MenuItemRequest request) {
        log.info("Adding menu item: restaurantId={}, ownerId={}, name={}", restaurantId, ownerId, request.getName());
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", restaurantId));

        var ownerSnapshot = customerClient.getCustomerSnapshot(ownerId);
        if (ownerSnapshot == null || !restaurant.getOwnerId().equals(ownerId)) {
            log.warn("Unauthorized menu item addition: restaurantId={}, requesterId={}", restaurantId, ownerId);
            throw new UnauthorizedException("You don't own this restaurant");
        }

        MenuItem item = new MenuItem();
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setCategory(request.getCategory());
        item.setImageUrl(request.getImageUrl());
        item.setRestaurant(restaurant);

        MenuItemResponse response = MenuItemResponse.fromEntity(menuItemRepository.save(item));
        log.info("Menu item added successfully: id={}, restaurantId={}", response.getId(), restaurantId);
        return response;
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenu(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndAvailableTrue(restaurantId)
                .stream().map(MenuItemResponse::fromEntity).toList();
    }

    @Transactional
    public MenuItemResponse updateMenuItem(Long itemId, Long ownerId, MenuItemRequest request) {
        log.info("Updating menu item: itemId={}, ownerId={}", itemId, ownerId);
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", itemId));

        if (!item.getRestaurant().getOwnerId().equals(ownerId)) {
            throw new UnauthorizedException("You don't own this restaurant");
        }

        if (request.getName() != null) item.setName(request.getName());
        if (request.getDescription() != null) item.setDescription(request.getDescription());
        if (request.getPrice() != null) item.setPrice(request.getPrice());
        if (request.getCategory() != null) item.setCategory(request.getCategory());

        return MenuItemResponse.fromEntity(menuItemRepository.save(item));
    }

    @Transactional
    public void toggleMenuItemAvailability(Long itemId, Long ownerId) {
        log.info("Toggling menu item availability: itemId={}, ownerId={}", itemId, ownerId);
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", itemId));

        if (!item.getRestaurant().getOwnerId().equals(ownerId)) {
            throw new UnauthorizedException("You don't own this restaurant");
        }

        item.setAvailable(!item.isAvailable());
        menuItemRepository.save(item);
        log.info("Menu item availability toggled: itemId={}, available={}", itemId, item.isAvailable());
    }

    @Transactional(readOnly = true)
    public RestaurantSnapshot getRestaurantSnapshot(Long id) {
        Restaurant r = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", id));
        return new RestaurantSnapshot(r.getId(), r.getName(), r.getAddress(), r.isActive(), r.getEstimatedDeliveryMinutes());
    }

    @Transactional(readOnly = true)
    public MenuItemSnapshot getMenuItemSnapshot(Long id) {
        MenuItem m = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", id));
        return new MenuItemSnapshot(m.getId(), m.getRestaurant().getId(), m.getName(), m.getPrice(), m.isAvailable());
    }

    private RestaurantResponse createRestaurantFallback(Long ownerId, RestaurantRequest request, Throwable throwable) {
        log.warn("Restaurant creation circuit breaker triggered: ownerId={}, cause={}", ownerId, throwable.getMessage());
        throw new IllegalStateException("Customer service is unavailable. Please try again later.", throwable);
    }

    private MenuItemResponse addMenuItemFallback(Long restaurantId, Long ownerId, MenuItemRequest request, Throwable throwable) {
        log.warn("Add menu item circuit breaker triggered: restaurantId={}, cause={}", restaurantId, throwable.getMessage());
        throw new IllegalStateException("Customer service is unavailable. Please try again later.", throwable);
    }

    public record RestaurantSnapshot(Long id, String name, String address, boolean active, int estimatedDeliveryMinutes) {}
    public record MenuItemSnapshot(Long id, Long restaurantId, String name, java.math.BigDecimal price, boolean available) {}
}
