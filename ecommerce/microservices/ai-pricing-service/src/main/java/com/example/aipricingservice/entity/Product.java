package com.example.aipricingservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private double basePrice;

    private double currentPrice;

    @Enumerated(EnumType.STRING)
    private DemandLevel demandLevel = DemandLevel.LOW;

    private int demandScore;

    private LocalDateTime lastPriceUpdate;

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public DemandLevel getDemandLevel() {
        return demandLevel;
    }

    public void setDemandLevel(DemandLevel demandLevel) {
        this.demandLevel = demandLevel;
    }

    public int getDemandScore() {
        return demandScore;
    }

    public void setDemandScore(int demandScore) {
        this.demandScore = demandScore;
    }

    public LocalDateTime getLastPriceUpdate() {
        return lastPriceUpdate;
    }

    public void setLastPriceUpdate(LocalDateTime lastPriceUpdate) {
        this.lastPriceUpdate = lastPriceUpdate;
    }
}