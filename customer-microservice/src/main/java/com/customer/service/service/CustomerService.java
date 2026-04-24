package com.customer.service.service;

import com.customer.service.client.AuthClient;
import com.customer.service.dto.CreateCustomerRequest;
import com.customer.service.dto.CustomerCreatedEvent;
import com.customer.service.dto.CustomerResponse;
import com.customer.service.model.Customer;
import com.customer.service.repository.CustomerRepository;
import com.shared.definitions.exception.DuplicateResourceException;
import com.shared.definitions.exception.ResourceNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final AuthClient authClient;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    @CircuitBreaker(name = "authService", fallbackMethod = "createCustomerFallback")
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        log.info("Creating customer: userId={}, username={}", request.getUserId(), request.getUsername());
        if (!authClient.userExists(request.getUserId())) {
            throw new ResourceNotFoundException("User", "id", request.getUserId());
        }

        if (customerRepository.existsByUsername(request.getUsername())) {
            log.warn("Customer creation failed - username already taken: {}", request.getUsername());
            throw new DuplicateResourceException("Username already taken");
        }

        Customer customer = new Customer();
        customer.setUserId(request.getUserId());
        customer.setUsername(request.getUsername());
        customer.setEmail(request.getEmail());
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setPhone(request.getPhone());
        customer.setDeliveryAddress(request.getDeliveryAddress());
        customer.setCity(request.getCity());

        Customer saved = customerRepository.save(customer);

        rabbitTemplate.convertAndSend("customer.events", "customer.created", new CustomerCreatedEvent(
                saved.getId(), saved.getUserId(), saved.getUsername(), saved.getEmail()
        ));

        log.info("Customer created successfully: id={}, userId={}", saved.getId(), saved.getUserId());
        return CustomerResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getByUserId(Long userId) {
        return customerRepository.findByUserId(userId)
                .map(CustomerResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "userId", userId));
    }

    @Transactional(readOnly = true)
    public CustomerResponse getById(Long id) {
        return customerRepository.findById(id)
                .map(CustomerResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
    }

    @Transactional(readOnly = true)
    public CustomerSnapshot getSnapshotById(Long id) {
        log.debug("Fetching customer snapshot: id={}", id);
        Customer c = customerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        return new CustomerSnapshot(c.getId(), c.getUsername(), c.getDeliveryAddress(), c.getPhone());
    }

    private CustomerResponse createCustomerFallback(CreateCustomerRequest request, Throwable throwable) {
        log.warn("Auth service circuit breaker triggered for customer creation: userId={}, cause={}", request.getUserId(), throwable.getMessage());
        throw new IllegalStateException("Auth service is unavailable. Please try again later.", throwable);
    }

    public record CustomerSnapshot(Long id, String username, String deliveryAddress, String phone) {}
}
