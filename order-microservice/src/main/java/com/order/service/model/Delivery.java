package com.order.service.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    private String driverName;
    private String driverPhone;
    private String pickupAddress;
    private String deliveryAddress;
    private LocalDateTime assignedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "delivery")
    private Order order;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = DeliveryStatus.PENDING;
    }

    public enum DeliveryStatus {
        PENDING, ASSIGNED, PICKED_UP, IN_TRANSIT, DELIVERED, FAILED
    }

    public Long getId() { return id; }
    public DeliveryStatus getStatus() { return status; }
    public String getDriverName() { return driverName; }
    public String getDriverPhone() { return driverPhone; }
    public String getPickupAddress() { return pickupAddress; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public LocalDateTime getPickedUpAt() { return pickedUpAt; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Order getOrder() { return order; }
    public void setStatus(DeliveryStatus status) { this.status = status; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public void setDriverPhone(String driverPhone) { this.driverPhone = driverPhone; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
    public void setPickedUpAt(LocalDateTime pickedUpAt) { this.pickedUpAt = pickedUpAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
    public void setOrder(Order order) { this.order = order; }
}