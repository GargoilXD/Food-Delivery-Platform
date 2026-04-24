package com.order.service.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private BigDecimal totalAmount;
    private BigDecimal deliveryFee;

    @Column(nullable = false)
    private String customerUsername;
    @Column(nullable = false)
    private String restaurantName;
    @Column(nullable = false)
    private String deliveryAddress;

    private String specialInstructions;
    private LocalDateTime estimatedDeliveryTime;

    @Column(nullable = false)
    private Long customerId;
    @Column(nullable = false)
    private Long restaurantId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = OrderStatus.PLACED;
        if (deliveryFee == null) deliveryFee = new BigDecimal("2.99");
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum OrderStatus {
        PLACED, CONFIRMED, PREPARING, READY_FOR_PICKUP,
        OUT_FOR_DELIVERY, DELIVERED, CANCELLED
    }

    public Long getId() { return id; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public String getCustomerUsername() { return customerUsername; }
    public String getRestaurantName() { return restaurantName; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public String getSpecialInstructions() { return specialInstructions; }
    public LocalDateTime getEstimatedDeliveryTime() { return estimatedDeliveryTime; }
    public Long getCustomerId() { return customerId; }
    public Long getRestaurantId() { return restaurantId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<OrderItem> getItems() { return items; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }
    public void setCustomerUsername(String customerUsername) { this.customerUsername = customerUsername; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
    public void setEstimatedDeliveryTime(LocalDateTime estimatedDeliveryTime) { this.estimatedDeliveryTime = estimatedDeliveryTime; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }
}