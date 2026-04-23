package com.order.service.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private BigDecimal unitPrice;
    @Column(nullable = false)
    private BigDecimal subtotal;

    private String specialInstructions;

    @Column(nullable = false)
    private String menuItemName;
    private Long menuItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    public Long getId() { return id; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getSubtotal() { return subtotal; }
    public String getSpecialInstructions() { return specialInstructions; }
    public String getMenuItemName() { return menuItemName; }
    public Long getMenuItemId() { return menuItemId; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
    public void setMenuItemName(String menuItemName) { this.menuItemName = menuItemName; }
    public void setMenuItemId(Long menuItemId) { this.menuItemId = menuItemId; }
}