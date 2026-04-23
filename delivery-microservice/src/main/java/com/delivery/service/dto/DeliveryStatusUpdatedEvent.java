package com.delivery.service.dto;

import java.time.LocalDateTime;

public record DeliveryStatusUpdatedEvent(
        Long deliveryId,
        Long orderId,
        Long customerId,
        String status,
        LocalDateTime updatedAt
) {}
