package com.customer.service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateCustomerRequest {
    @NotNull
    private Long userId;
    @NotBlank
    private String username;
    @NotBlank
    @Email
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String deliveryAddress;
    private String city;
}
