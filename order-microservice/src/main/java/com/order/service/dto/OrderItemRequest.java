package com.order.service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderItemRequest {
    @NotNull
    private Long menuItemId;
    @Positive
    private int quantity;
    private String specialInstructions;
}
