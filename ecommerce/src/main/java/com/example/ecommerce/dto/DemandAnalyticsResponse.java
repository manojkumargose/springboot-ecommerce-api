package com.example.ecommerce.dto;

import java.util.List;

public class DemandAnalyticsResponse {

    private Long totalEvents;
    private Long totalViews;
    private Long totalCartAdds;
    private Long totalPurchases;
    private List<TrendingProductDto> topTrendingProducts;
    private Integer productsWithHighDemand;
    private Integer productsWithLowDemand;
    private Integer totalPriceAdjustments;

    public DemandAnalyticsResponse() {}

    public Long getTotalEvents() { return totalEvents; }
    public void setTotalEvents(Long totalEvents) { this.totalEvents = totalEvents; }
    public Long getTotalViews() { return totalViews; }
    public void setTotalViews(Long totalViews) { this.totalViews = totalViews; }
    public Long getTotalCartAdds() { return totalCartAdds; }
    public void setTotalCartAdds(Long totalCartAdds) { this.totalCartAdds = totalCartAdds; }
    public Long getTotalPurchases() { return totalPurchases; }
    public void setTotalPurchases(Long totalPurchases) { this.totalPurchases = totalPurchases; }
    public List<TrendingProductDto> getTopTrendingProducts() { return topTrendingProducts; }
    public void setTopTrendingProducts(List<TrendingProductDto> topTrendingProducts) { this.topTrendingProducts = topTrendingProducts; }
    public Integer getProductsWithHighDemand() { return productsWithHighDemand; }
    public void setProductsWithHighDemand(Integer productsWithHighDemand) { this.productsWithHighDemand = productsWithHighDemand; }
    public Integer getProductsWithLowDemand() { return productsWithLowDemand; }
    public void setProductsWithLowDemand(Integer productsWithLowDemand) { this.productsWithLowDemand = productsWithLowDemand; }
    public Integer getTotalPriceAdjustments() { return totalPriceAdjustments; }
    public void setTotalPriceAdjustments(Integer totalPriceAdjustments) { this.totalPriceAdjustments = totalPriceAdjustments; }
}