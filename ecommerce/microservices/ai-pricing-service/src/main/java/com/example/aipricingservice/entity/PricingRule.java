package com.example.aipricingservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "pricing_rules")
public class PricingRule {

    @Id
    private Long id;

    private Long categoryId;

    private int demandWindowHours;

    private int highDemandThreshold;

    private double highDemandMultiplier;

    private int mediumDemandThreshold;

    private double mediumDemandMultiplier;

    private boolean active = true;

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public int getDemandWindowHours() {
        return demandWindowHours;
    }

    public void setDemandWindowHours(int demandWindowHours) {
        this.demandWindowHours = demandWindowHours;
    }

    public int getHighDemandThreshold() {
        return highDemandThreshold;
    }

    public void setHighDemandThreshold(int highDemandThreshold) {
        this.highDemandThreshold = highDemandThreshold;
    }

    public double getHighDemandMultiplier() {
        return highDemandMultiplier;
    }

    public void setHighDemandMultiplier(double highDemandMultiplier) {
        this.highDemandMultiplier = highDemandMultiplier;
    }

    public int getMediumDemandThreshold() {
        return mediumDemandThreshold;
    }

    public void setMediumDemandThreshold(int mediumDemandThreshold) {
        this.mediumDemandThreshold = mediumDemandThreshold;
    }

    public double getMediumDemandMultiplier() {
        return mediumDemandMultiplier;
    }

    public void setMediumDemandMultiplier(double mediumDemandMultiplier) {
        this.mediumDemandMultiplier = mediumDemandMultiplier;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}