package com.example.aipricingservice.dto;
import java.util.List;
public class DemandAnalyticsResponse {
    private List<TrendingProductDto> trendingProducts; private Long totalEventsLast24h;
    private Long totalEventsLast7d; private String topEventType;

    public List<TrendingProductDto> getTrendingProducts() { return trendingProducts; }
    public void setTrendingProducts(List<TrendingProductDto> t) { this.trendingProducts=t; }
    public Long getTotalEventsLast24h() { return totalEventsLast24h; }
    public void setTotalEventsLast24h(Long t) { this.totalEventsLast24h=t; }
    public Long getTotalEventsLast7d() { return totalEventsLast7d; }
    public void setTotalEventsLast7d(Long t) { this.totalEventsLast7d=t; }
    public String getTopEventType() { return topEventType; }
    public void setTopEventType(String t) { this.topEventType=t; }
}
