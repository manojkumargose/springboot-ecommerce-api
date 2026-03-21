package com.example.ecommerce.dto;

import com.example.ecommerce.entity.Product.DemandLevel;
import java.time.LocalDateTime;

public class ProductPricingResponse {

    private Long productId;
    private String productName;
    private Double basePrice;
    private Double currentPrice;
    private Double priceChangePercent;
    private DemandLevel demandLevel;
    private Integer demandScore;
    private LocalDateTime lastPriceUpdate;

    public ProductPricingResponse() {}

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Double getBasePrice() { return basePrice; }
    public void setBasePrice(Double basePrice) { this.basePrice = basePrice; }
    public Double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(Double currentPrice) { this.currentPrice = currentPrice; }
    public Double getPriceChangePercent() { return priceChangePercent; }
    public void setPriceChangePercent(Double priceChangePercent) { this.priceChangePercent = priceChangePercent; }
    public DemandLevel getDemandLevel() { return demandLevel; }
    public void setDemandLevel(DemandLevel demandLevel) { this.demandLevel = demandLevel; }
    public Integer getDemandScore() { return demandScore; }
    public void setDemandScore(Integer demandScore) { this.demandScore = demandScore; }
    public LocalDateTime getLastPriceUpdate() { return lastPriceUpdate; }
    public void setLastPriceUpdate(LocalDateTime lastPriceUpdate) { this.lastPriceUpdate = lastPriceUpdate; }
}