package com.order.service.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class PlaceOrderRequest {
    @NotNull
    private Long restaurantId;
    @NotEmpty
    private List<OrderItemRequest> items;
    private String deliveryAddress;  // optional override of customer's default address
    private String specialInstructions;

    public Long getRestaurantId() { return restaurantId; }
    public List<OrderItemRequest> getItems() { return items; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public String getSpecialInstructions() { return specialInstructions; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
}
