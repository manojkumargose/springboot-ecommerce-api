package com.example.aipricingservice.controller;

import com.example.aipricingservice.dto.ApiResponse;
import com.example.aipricingservice.entity.PriceHistory;
import com.example.aipricingservice.service.DynamicPricingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class DynamicPricingController {

    private final DynamicPricingService dynamicPricingService;

    @GetMapping("/calculate/{productId}")
    @Operation(summary = "Calculate AI price based on real-time metrics")
    public ResponseEntity<ApiResponse<Double>> calculatePrice(
            @PathVariable Long productId,
            @RequestParam(required = false, defaultValue = "100.0") Double basePrice) {
        Double price = dynamicPricingService.getCurrentPrice(productId, basePrice);
        return ResponseEntity.ok(ApiResponse.success("AI Price calculation successful", price));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<Double>> getProductPrice(
            @PathVariable Long productId,
            @RequestParam(required = false, defaultValue = "100.0") Double basePrice) {
        Double currentPrice = dynamicPricingService.getCurrentPrice(productId, basePrice);
        return ResponseEntity.ok(ApiResponse.success("Current price fetched", currentPrice));
    }

    @PostMapping("/recalculate/{productId}")
    public ResponseEntity<ApiResponse<Void>> recalculate(@PathVariable Long productId,
                                                         @RequestParam(required=false) Long categoryId) {
        dynamicPricingService.recalculatePrice(productId, categoryId);
        return ResponseEntity.ok(ApiResponse.success("Repricing triggered and logged", null));
    }

    @PostMapping("/viral-surge/{productId}")
    @Operation(summary = "Simulate a massive spike in demand")
    public ResponseEntity<ApiResponse<Double>> triggerViralSurge(@PathVariable Long productId) {
        Double newPrice = dynamicPricingService.updatePriceWithHistory(productId, 1.0, "Viral TikTok Trend - High Demand!");
        return ResponseEntity.ok(ApiResponse.success("Viral Surge Activated! Prices are skyrocketing 🚀", newPrice));
    }

    @GetMapping("/history/{productId}")
    public ResponseEntity<ApiResponse<List<PriceHistory>>> getPriceHistory(@PathVariable Long productId) {
        List<PriceHistory> history = dynamicPricingService.getHistory(productId);
        return ResponseEntity.ok(ApiResponse.success("Price history retrieved", history));
    }
}