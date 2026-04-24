package com.restaurant.service.dto;

import com.restaurant.service.model.MenuItem;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class MenuItemResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private boolean available;
    private String imageUrl;
    private Long restaurantId;
    private String restaurantName;

    public static MenuItemResponse fromEntity(MenuItem m) {
        MenuItemResponse dto = new MenuItemResponse();
        dto.setId(m.getId());
        dto.setName(m.getName());
        dto.setDescription(m.getDescription());
        dto.setPrice(m.getPrice());
        dto.setCategory(m.getCategory());
        dto.setAvailable(m.isAvailable());
        dto.setImageUrl(m.getImageUrl());
        dto.setRestaurantId(m.getRestaurant().getId());
        dto.setRestaurantName(m.getRestaurant().getName());
        return dto;
    }
}
