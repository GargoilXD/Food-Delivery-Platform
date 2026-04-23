package com.auth.dto;

import com.auth.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String password,
        String firstName,
        String lastName,
        String phone,
        String deliveryAddress,
        String city,
        Role role
) {}
