package com.restaurant.service.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restaurants")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String cuisineType;
    private String address;
    private String city;
    private String phone;
    private boolean active;
    @Column(nullable = false)
    private double rating;
    private int estimatedDeliveryMinutes;

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuItem> menuItems = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (rating == 0) rating = 0.0;
        active = true;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCuisineType() { return cuisineType; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getPhone() { return phone; }
    public boolean isActive() { return active; }
    public double getRating() { return rating; }
    public int getEstimatedDeliveryMinutes() { return estimatedDeliveryMinutes; }
    public Long getOwnerId() { return ownerId; }
    public List<MenuItem> getMenuItems() { return menuItems; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setCuisineType(String cuisineType) { this.cuisineType = cuisineType; }
    public void setAddress(String address) { this.address = address; }
    public void setCity(String city) { this.city = city; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEstimatedDeliveryMinutes(int estimatedDeliveryMinutes) { this.estimatedDeliveryMinutes = estimatedDeliveryMinutes; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
}