package com.customer.service.repository;

import com.customer.service.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUserId(Long userId);
    Optional<Customer> findByUsername(String username);
    Optional<Customer> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
