package com.customer.service.client;

import com.customer.service.service.CustomerService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service")
public interface CustomerClient {
    @GetMapping("/customers/snapshot/{id}")
    CustomerService.CustomerSnapshot getCustomerSnapshot(@PathVariable Long id);
}
