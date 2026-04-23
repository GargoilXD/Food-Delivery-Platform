package com.customer.service.dto;

import com.customer.service.model.Customer;

import java.time.LocalDateTime;

public record CustomerResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        String phone,
        String deliveryAddress,
        String city,
        String role,
        LocalDateTime createdAt,
        int orderCount
) {

    public static CustomerResponse fromEntity(Customer c) {
        return new CustomerResponse(
                c.getId(),
                c.getUsername(),
                c.getEmail(),
                c.getFirstName(),
                c.getLastName(),
                c.getPhone(),
                c.getDeliveryAddress(),
                c.getCity(),
                "CUSTOMER",
                c.getCreatedAt(),
                0
        );
    }
}