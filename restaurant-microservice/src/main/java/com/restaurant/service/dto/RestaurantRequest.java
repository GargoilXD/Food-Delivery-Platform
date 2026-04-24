package com.restaurant.service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RestaurantRequest {
    @NotBlank private String name;
    private String description;
    @NotBlank private String cuisineType;
    @NotBlank private String address;
    @NotBlank private String city;
    private String phone;
    private int estimatedDeliveryMinutes;
}
