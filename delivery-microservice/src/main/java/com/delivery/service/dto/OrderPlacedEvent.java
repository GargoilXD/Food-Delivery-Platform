package com.delivery.service.dto;

import java.math.BigDecimal;

public record OrderPlacedEvent(Long orderId, Long customerId, Long restaurantId, BigDecimal totalAmount) {}
