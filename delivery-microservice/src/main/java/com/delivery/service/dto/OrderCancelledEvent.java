package com.delivery.service.dto;

public record OrderCancelledEvent(Long orderId, Long customerId, Long restaurantId) {}
