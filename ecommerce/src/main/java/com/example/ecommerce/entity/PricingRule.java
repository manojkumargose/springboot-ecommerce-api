package com.example.ecommerce.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pricing_rules")
public class PricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer highDemandThreshold;

    @Column(nullable = false)
    private Integer mediumDemandThreshold;

    @Column(nullable = false)
    private Double highDemandMultiplier;

    @Column(nullable = false)
    private Double mediumDemandMultiplier;

    @Column(nullable = false)
    private Double lowDemandMultiplier;

    @Column(nullable = false)
    private Double maxPriceIncreasePercent;

    @Column(nullable = false)
    private Double maxPriceDecreasePercent;

    @Column(nullable = false)
    private Integer demandWindowHours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private Boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isActive == null) this.isActive = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ─── Constructors ────────────────────────────────────────

    public PricingRule() {}

    // ─── Getters ─────────────────────────────────────────────

    public Long getId() { return id; }
    public Integer getHighDemandThreshold() { return highDemandThreshold; }
    public Integer getMediumDemandThreshold() { return mediumDemandThreshold; }
    public Double getHighDemandMultiplier() { return highDemandMultiplier; }
    public Double getMediumDemandMultiplier() { return mediumDemandMultiplier; }
    public Double getLowDemandMultiplier() { return lowDemandMultiplier; }
    public Double getMaxPriceIncreasePercent() { return maxPriceIncreasePercent; }
    public Double getMaxPriceDecreasePercent() { return maxPriceDecreasePercent; }
    public Integer getDemandWindowHours() { return demandWindowHours; }
    public Category getCategory() { return category; }
    public Boolean getIsActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // ─── Setters ─────────────────────────────────────────────

    public void setId(Long id) { this.id = id; }
    public void setHighDemandThreshold(Integer highDemandThreshold) { this.highDemandThreshold = highDemandThreshold; }
    public void setMediumDemandThreshold(Integer mediumDemandThreshold) { this.mediumDemandThreshold = mediumDemandThreshold; }
    public void setHighDemandMultiplier(Double highDemandMultiplier) { this.highDemandMultiplier = highDemandMultiplier; }
    public void setMediumDemandMultiplier(Double mediumDemandMultiplier) { this.mediumDemandMultiplier = mediumDemandMultiplier; }
    public void setLowDemandMultiplier(Double lowDemandMultiplier) { this.lowDemandMultiplier = lowDemandMultiplier; }
    public void setMaxPriceIncreasePercent(Double maxPriceIncreasePercent) { this.maxPriceIncreasePercent = maxPriceIncreasePercent; }
    public void setMaxPriceDecreasePercent(Double maxPriceDecreasePercent) { this.maxPriceDecreasePercent = maxPriceDecreasePercent; }
    public void setDemandWindowHours(Integer demandWindowHours) { this.demandWindowHours = demandWindowHours; }
    public void setCategory(Category category) { this.category = category; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}