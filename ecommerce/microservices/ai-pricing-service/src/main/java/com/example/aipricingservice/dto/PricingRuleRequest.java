package com.example.aipricingservice.dto;

import jakarta.validation.constraints.*;

public class PricingRuleRequest {

    // 1. Rule Identification
    @NotBlank(message = "Rule name is required")
    private String name;

    @NotNull(message = "Target increase is required")
    @Positive
    private Double targetIncrease;

    private Long categoryId;

    // 2. Demand Thresholds
    @NotNull @Min(1)
    private Integer highDemandThreshold;

    @NotNull @Min(1)
    private Integer mediumDemandThreshold;

    // 3. Multipliers (The "AI" Logic Drivers)
    @NotNull @Positive private Double highDemandMultiplier;
    @NotNull @Positive private Double mediumDemandMultiplier;
    @NotNull @Positive private Double lowDemandMultiplier;

    // 4. Safety Guardrails
    @NotNull @Positive private Double maxPriceIncreasePercent;
    @NotNull @Positive private Double maxPriceDecreasePercent;

    // 5. Analysis Settings
    @NotNull @Min(1) private Integer demandWindowHours;

    // --- GETTERS AND SETTERS ---

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getTargetIncrease() { return targetIncrease; }
    public void setTargetIncrease(Double targetIncrease) { this.targetIncrease = targetIncrease; }

    public Integer getHighDemandThreshold() { return highDemandThreshold; }
    public void setHighDemandThreshold(Integer h) { this.highDemandThreshold = h; }

    public Integer getMediumDemandThreshold() { return mediumDemandThreshold; }
    public void setMediumDemandThreshold(Integer m) { this.mediumDemandThreshold = m; }

    public Double getHighDemandMultiplier() { return highDemandMultiplier; }
    public void setHighDemandMultiplier(Double h) { this.highDemandMultiplier = h; }

    public Double getMediumDemandMultiplier() { return mediumDemandMultiplier; }
    public void setMediumDemandMultiplier(Double m) { this.mediumDemandMultiplier = m; }

    public Double getLowDemandMultiplier() { return lowDemandMultiplier; }
    public void setLowDemandMultiplier(Double l) { this.lowDemandMultiplier = l; }

    public Double getMaxPriceIncreasePercent() { return maxPriceIncreasePercent; }
    public void setMaxPriceIncreasePercent(Double m) { this.maxPriceIncreasePercent = m; }

    public Double getMaxPriceDecreasePercent() { return maxPriceDecreasePercent; }
    public void setMaxPriceDecreasePercent(Double m) { this.maxPriceDecreasePercent = m; }

    public Integer getDemandWindowHours() { return demandWindowHours; }
    public void setDemandWindowHours(Integer d) { this.demandWindowHours = d; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long c) { this.categoryId = c; }
}