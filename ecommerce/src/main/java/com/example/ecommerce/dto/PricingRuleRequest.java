package com.example.ecommerce.dto;

public class PricingRuleRequest {

    private Integer highDemandThreshold;
    private Integer mediumDemandThreshold;
    private Double highDemandMultiplier;
    private Double mediumDemandMultiplier;
    private Double lowDemandMultiplier;
    private Double maxPriceIncreasePercent;
    private Double maxPriceDecreasePercent;
    private Integer demandWindowHours;
    private Long categoryId;

    public PricingRuleRequest() {}

    public Integer getHighDemandThreshold() { return highDemandThreshold; }
    public void setHighDemandThreshold(Integer highDemandThreshold) { this.highDemandThreshold = highDemandThreshold; }
    public Integer getMediumDemandThreshold() { return mediumDemandThreshold; }
    public void setMediumDemandThreshold(Integer mediumDemandThreshold) { this.mediumDemandThreshold = mediumDemandThreshold; }
    public Double getHighDemandMultiplier() { return highDemandMultiplier; }
    public void setHighDemandMultiplier(Double highDemandMultiplier) { this.highDemandMultiplier = highDemandMultiplier; }
    public Double getMediumDemandMultiplier() { return mediumDemandMultiplier; }
    public void setMediumDemandMultiplier(Double mediumDemandMultiplier) { this.mediumDemandMultiplier = mediumDemandMultiplier; }
    public Double getLowDemandMultiplier() { return lowDemandMultiplier; }
    public void setLowDemandMultiplier(Double lowDemandMultiplier) { this.lowDemandMultiplier = lowDemandMultiplier; }
    public Double getMaxPriceIncreasePercent() { return maxPriceIncreasePercent; }
    public void setMaxPriceIncreasePercent(Double maxPriceIncreasePercent) { this.maxPriceIncreasePercent = maxPriceIncreasePercent; }
    public Double getMaxPriceDecreasePercent() { return maxPriceDecreasePercent; }
    public void setMaxPriceDecreasePercent(Double maxPriceDecreasePercent) { this.maxPriceDecreasePercent = maxPriceDecreasePercent; }
    public Integer getDemandWindowHours() { return demandWindowHours; }
    public void setDemandWindowHours(Integer demandWindowHours) { this.demandWindowHours = demandWindowHours; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}