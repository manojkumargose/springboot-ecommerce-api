package com.example.aipricingservice.controller;

import com.example.aipricingservice.dto.*;
import com.example.aipricingservice.service.DemandAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DemandAnalyticsService demandAnalyticsService;
    public DashboardController(DemandAnalyticsService demandAnalyticsService) {
        this.demandAnalyticsService = demandAnalyticsService;
    }

    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<DemandAnalyticsResponse>> getAnalytics() {
        return ResponseEntity.ok(ApiResponse.success("Analytics fetched", demandAnalyticsService.getAnalytics()));
    }
}
