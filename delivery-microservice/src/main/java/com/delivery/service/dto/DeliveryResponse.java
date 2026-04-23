package com.delivery.service.dto;

import com.delivery.service.model.Delivery;

import java.time.LocalDateTime;

public record DeliveryResponse(
        Long id,
        Long orderId,
        Long customerId,
        Long restaurantId,
        String status,
        String driverName,
        String driverPhone,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static DeliveryResponse fromEntity(Delivery delivery) {
        return new DeliveryResponse(
                delivery.getId(),
                delivery.getOrderId(),
                delivery.getCustomerId(),
                delivery.getRestaurantId(),
                delivery.getStatus().name(),
                delivery.getDriverName(),
                delivery.getDriverPhone(),
                delivery.getCreatedAt(),
                delivery.getUpdatedAt()
        );
    }
}
