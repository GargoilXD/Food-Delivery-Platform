package com.customer.service.dto;

public record CustomerCreatedEvent(Long customerId, Long userId, String username, String email) {}
