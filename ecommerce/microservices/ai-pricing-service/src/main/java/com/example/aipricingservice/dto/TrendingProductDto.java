package com.example.aipricingservice.dto;
public class TrendingProductDto {
    private Long productId; private Long demandCount; private String demandLevel;
    public TrendingProductDto(Long productId, Long demandCount, String demandLevel) {
        this.productId=productId; this.demandCount=demandCount; this.demandLevel=demandLevel;
    }
    public Long getProductId() { return productId; }
    public Long getDemandCount() { return demandCount; }
    public String getDemandLevel() { return demandLevel; }
}
