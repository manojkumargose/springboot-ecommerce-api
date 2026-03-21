package com.example.ecommerce.dto;

import com.example.ecommerce.entity.Product.DemandLevel;

public class TrendingProductDto {

    private Long productId;
    private String productName;
    private Long eventCount;
    private DemandLevel demandLevel;
    private Double currentPrice;
    private Double priceChangePercent;

    public TrendingProductDto() {}

    public TrendingProductDto(Long productId, String productName, Long eventCount,
                              DemandLevel demandLevel, Double currentPrice,
                              Double priceChangePercent) {
        this.productId = productId;
        this.productName = productName;
        this.eventCount = eventCount;
        this.demandLevel = demandLevel;
        this.currentPrice = currentPrice;
        this.priceChangePercent = priceChangePercent;
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Long getEventCount() { return eventCount; }
    public void setEventCount(Long eventCount) { this.eventCount = eventCount; }
    public DemandLevel getDemandLevel() { return demandLevel; }
    public void setDemandLevel(DemandLevel demandLevel) { this.demandLevel = demandLevel; }
    public Double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(Double currentPrice) { this.currentPrice = currentPrice; }
    public Double getPriceChangePercent() { return priceChangePercent; }
    public void setPriceChangePercent(Double priceChangePercent) { this.priceChangePercent = priceChangePercent; }
}