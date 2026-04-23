package com.restaurant.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service")
public interface CustomerClient {
    @GetMapping("/customers/snapshot/{id}")
    CustomerSnapshot getCustomerSnapshot(@PathVariable Long id);

    record CustomerSnapshot(Long id, String username, String deliveryAddress, String phone) {}
}
