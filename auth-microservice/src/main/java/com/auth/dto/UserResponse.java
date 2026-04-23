package com.auth.dto;

import com.auth.model.User;

import java.time.LocalDateTime;

public record UserResponse(
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

    public static UserResponse fromEntity(User c) {
        return new UserResponse(
                c.getId(),
                c.getUsername(),
                c.getEmail(),
                null,
                null,
                null,
                null,
                null,
                c.getRole().name(),
                c.getCreatedAt(),
                0
        );
    }
}
