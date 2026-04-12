package com.example.aipricingservice.controller;

import com.example.aipricingservice.dto.*;
import com.example.aipricingservice.service.DemandAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private final DemandAnalyticsService demandAnalyticsService;
    public RecommendationController(DemandAnalyticsService demandAnalyticsService) {
        this.demandAnalyticsService = demandAnalyticsService;
    }

    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<List<TrendingProductDto>>> getTrending(
            @RequestParam(defaultValue = "24") int hours) {
        return ResponseEntity.ok(ApiResponse.success("Trending products", demandAnalyticsService.getTrendingProducts(hours)));
    }
}
