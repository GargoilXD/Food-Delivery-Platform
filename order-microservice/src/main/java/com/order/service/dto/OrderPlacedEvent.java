package com.order.service.dto;

import java.math.BigDecimal;

public record OrderPlacedEvent(Long orderId, Long customerId, Long restaurantId, BigDecimal total) {}
