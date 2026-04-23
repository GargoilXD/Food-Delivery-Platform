package com.order.service.dto;

import com.order.service.model.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String status,
        BigDecimal totalAmount,
        BigDecimal deliveryFee,
        String deliveryAddress,
        String specialInstructions,
        LocalDateTime createdAt,
        LocalDateTime estimatedDeliveryTime,
        List<OrderItemDetail> items,
        Long customerId,
        String customerName,
        Long restaurantId,
        String restaurantName,
        String deliveryStatus,
        String driverName,
        String driverPhone
) {
    public record OrderItemDetail(
            Long id,
            String itemName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {}

    public static OrderResponse fromEntity(Order o) {
        String deliveryStatus = o.getDelivery() != null ? o.getDelivery().getStatus().name() : null;
        String driverName = o.getDelivery() != null ? o.getDelivery().getDriverName() : null;
        String driverPhone = o.getDelivery() != null ? o.getDelivery().getDriverPhone() : null;

        return new OrderResponse(
                o.getId(),
                o.getStatus().name(),
                o.getTotalAmount(),
                o.getDeliveryFee(),
                o.getDeliveryAddress(),
                o.getSpecialInstructions(),
                o.getCreatedAt(),
                o.getEstimatedDeliveryTime(),
                o.getItems().stream()
                        .map(item -> new OrderItemDetail(
                                item.getId(),
                                item.getMenuItemName(),
                                item.getQuantity(),
                                item.getUnitPrice(),
                                item.getSubtotal()
                        ))
                        .toList(),
                o.getCustomerId(),
                o.getCustomerUsername(),
                o.getRestaurantId(),
                o.getRestaurantName(),
                deliveryStatus,
                driverName,
                driverPhone
        );
    }
}
