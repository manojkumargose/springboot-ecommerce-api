package com.example.aipricingservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "demand_events")
public class DemandEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    private int weight;

    private LocalDateTime createdAt;

    // Inner enum — referenced as DemandEvent.EventType throughout the project
    public enum EventType {
        PURCHASE,
        CART_ADD,
        WISHLIST_ADD,
        VIEW,
        CANCELLATION
    }
}