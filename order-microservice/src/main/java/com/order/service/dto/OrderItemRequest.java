package com.order.service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class OrderItemRequest {
    @NotNull
    private Long menuItemId;
    @Positive
    private int quantity;
    private String specialInstructions;

    public Long getMenuItemId() { return menuItemId; }
    public int getQuantity() { return quantity; }
    public String getSpecialInstructions() { return specialInstructions; }
    public void setMenuItemId(Long menuItemId) { this.menuItemId = menuItemId; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
}
