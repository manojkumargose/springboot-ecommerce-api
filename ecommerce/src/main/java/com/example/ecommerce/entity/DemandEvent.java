package com.example.ecommerce.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "demand_events", indexes = {
        @Index(name = "idx_demand_product_id", columnList = "product_id"),
        @Index(name = "idx_demand_event_type", columnList = "eventType"),
        @Index(name = "idx_demand_created_at", columnList = "createdAt")
})
public class DemandEvent {

    public enum EventType {
        VIEW,
        CART_ADD,
        WISHLIST_ADD,
        PURCHASE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ─── Constructors ────────────────────────────────────────

    public DemandEvent() {}

    public DemandEvent(Long productId, EventType eventType, Long userId) {
        this.productId = productId;
        this.eventType = eventType;
        this.userId = userId;
    }

    // ─── Getters ─────────────────────────────────────────────

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public EventType getEventType() { return eventType; }
    public Long getUserId() { return userId; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // ─── Setters ─────────────────────────────────────────────

    public void setId(Long id) { this.id = id; }
    public void setProductId(Long productId) { this.productId = productId; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}