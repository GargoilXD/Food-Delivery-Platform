package com.customer.service.controller;

import com.customer.service.dto.CreateCustomerRequest;
import com.customer.service.dto.CustomerResponse;
import com.customer.service.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CreateCustomerRequest request) {
        return ResponseEntity.ok(customerService.createCustomer(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getById(id));
    }

    @GetMapping("/by-user")
    public ResponseEntity<CustomerResponse> getByUserId(@RequestParam Long userId) {
        return ResponseEntity.ok(customerService.getByUserId(userId));
    }

    @GetMapping("/snapshot/{id}")
    public ResponseEntity<CustomerService.CustomerSnapshot> getSnapshot(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getSnapshotById(id));
    }
}
