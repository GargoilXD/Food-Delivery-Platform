package com.order.service.dto;

import com.order.service.model.Delivery;

import java.time.LocalDateTime;

public record DeliveryResponse(
        Long id,
        String status,
        String driverName,
        String driverPhone,
        String pickupAddress,
        String deliveryAddress,
        LocalDateTime assignedAt,
        LocalDateTime pickedUpAt,
        LocalDateTime deliveredAt,
        LocalDateTime createdAt,
        Long orderId,
        String orderStatus,
        Long customerId,
        String customerName,
        String restaurantName
) {

    public static DeliveryResponse fromEntity(Delivery d) {
        Long orderId = d.getOrder() != null ? d.getOrder().getId() : null;
        String orderStatus = d.getOrder() != null ? d.getOrder().getStatus().name() : null;
        Long customerId = d.getOrder() != null ? d.getOrder().getCustomerId() : null;
        String customerName = d.getOrder() != null ? d.getOrder().getCustomerUsername() : null;
        String restaurantName = d.getOrder() != null ? d.getOrder().getRestaurantName() : null;
        return new DeliveryResponse(
                d.getId(),
                d.getStatus().name(),
                d.getDriverName(),
                d.getDriverPhone(),
                d.getPickupAddress(),
                d.getDeliveryAddress(),
                d.getAssignedAt(),
                d.getPickedUpAt(),
                d.getDeliveredAt(),
                d.getCreatedAt(),
                orderId,
                orderStatus,
                customerId,
                customerName,
                restaurantName
        );
    }
}
