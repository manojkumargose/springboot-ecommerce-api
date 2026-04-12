package com.example.aipricingservice.dto;
public class ProductPricingResponse {
    private Long productId; private Double basePrice; private Double currentPrice;
    private Double recommendedPrice; private String demandLevel; private Integer demandScore;
    private Double priceChangePercent; private String appliedRule;

    public Long getProductId() { return productId; } public void setProductId(Long p) { this.productId=p; }
    public Double getBasePrice() { return basePrice; } public void setBasePrice(Double b) { this.basePrice=b; }
    public Double getCurrentPrice() { return currentPrice; } public void setCurrentPrice(Double c) { this.currentPrice=c; }
    public Double getRecommendedPrice() { return recommendedPrice; } public void setRecommendedPrice(Double r) { this.recommendedPrice=r; }
    public String getDemandLevel() { return demandLevel; } public void setDemandLevel(String d) { this.demandLevel=d; }
    public Integer getDemandScore() { return demandScore; } public void setDemandScore(Integer d) { this.demandScore=d; }
    public Double getPriceChangePercent() { return priceChangePercent; } public void setPriceChangePercent(Double p) { this.priceChangePercent=p; }
    public String getAppliedRule() { return appliedRule; } public void setAppliedRule(String a) { this.appliedRule=a; }
}
