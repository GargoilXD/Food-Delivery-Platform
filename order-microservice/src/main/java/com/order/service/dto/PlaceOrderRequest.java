package com.order.service.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class PlaceOrderRequest {
    @NotNull
    private Long restaurantId;
    @NotEmpty
    private List<OrderItemRequest> items;
    private String deliveryAddress;
    private String specialInstructions;
}
