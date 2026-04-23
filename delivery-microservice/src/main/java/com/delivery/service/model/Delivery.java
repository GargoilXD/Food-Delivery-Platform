package com.delivery.service.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private Long orderId;
    @Column(nullable = false)
    private Long customerId;
    @Column(nullable = false)
    private Long restaurantId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;
    private String driverName;
    private String driverPhone;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = DeliveryStatus.ASSIGNED;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum DeliveryStatus {ASSIGNED, PICKED_UP, IN_TRANSIT, DELIVERED, FAILED}
    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public Long getCustomerId() { return customerId; }
    public Long getRestaurantId() { return restaurantId; }
    public DeliveryStatus getStatus() { return status; }
    public String getDriverName() { return driverName; }
    public String getDriverPhone() { return driverPhone; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }
    public void setStatus(DeliveryStatus status) { this.status = status; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public void setDriverPhone(String driverPhone) { this.driverPhone = driverPhone; }
}
