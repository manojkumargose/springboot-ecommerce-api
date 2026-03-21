package com.example.ecommerce.controller;

import com.example.ecommerce.dto.*;
import com.example.ecommerce.entity.PricingRule;
import com.example.ecommerce.service.DemandTrackingService;
import com.example.ecommerce.service.DynamicPricingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pricing")
@Tag(name = "Dynamic Pricing", description = "Demand-based automatic price adjustment")
public class DynamicPricingController {

    private final DynamicPricingService dynamicPricingService;
    private final DemandTrackingService demandTrackingService;

    public DynamicPricingController(DynamicPricingService dynamicPricingService,
                                    DemandTrackingService demandTrackingService) {
        this.dynamicPricingService = dynamicPricingService;
        this.demandTrackingService = demandTrackingService;
    }

    // ─── PUBLIC ENDPOINTS ────────────────────────────────────

    @PostMapping("/track")
    @Operation(summary = "Track a demand event (VIEW, CART_ADD, WISHLIST_ADD, PURCHASE)")
    public ResponseEntity<ApiResponse> trackEvent(
            @RequestBody DemandEventRequest request) {

        demandTrackingService.trackEvent(request, null);

        return ResponseEntity.ok(ApiResponse.success("Event tracked successfully", null));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get current pricing details for a product")
    public ResponseEntity<ApiResponse<ProductPricingResponse>> getProductPricing(
            @PathVariable Long productId) {
        ProductPricingResponse pricing = dynamicPricingService.getProductPricing(productId);
        return ResponseEntity.ok(ApiResponse.success("Product pricing fetched", pricing));
    }

    @GetMapping("/products")
    @Operation(summary = "Get pricing details for all products")
    public ResponseEntity<ApiResponse<List<ProductPricingResponse>>> getAllProductPricing() {
        List<ProductPricingResponse> pricing = dynamicPricingService.getAllProductPricing();
        return ResponseEntity.ok(ApiResponse.success("All product pricing fetched", pricing));
    }

    // ─── ADMIN ENDPOINTS ─────────────────────────────────────

    @PostMapping("/rules")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create or update a pricing rule (ADMIN)")
    public ResponseEntity<ApiResponse<PricingRule>> createOrUpdateRule(
            @RequestBody PricingRuleRequest request) {
        PricingRule rule = dynamicPricingService.createOrUpdateRule(request);
        return ResponseEntity.ok(ApiResponse.success("Pricing rule saved successfully", rule));
    }

    @GetMapping("/rules/default")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get the default pricing rule")
    public ResponseEntity<ApiResponse<PricingRule>> getDefaultRule() {
        PricingRule rule = dynamicPricingService.getDefaultRule();
        return ResponseEntity.ok(ApiResponse.success("Default pricing rule fetched", rule));
    }

    @PostMapping("/recalculate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Manually trigger price recalculation (ADMIN)")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> triggerRecalculation() {
        int adjustedCount = dynamicPricingService.recalculateAllPrices();
        return ResponseEntity.ok(ApiResponse.success(
                adjustedCount + " products had their prices adjusted",
                Map.of("adjustedProducts", adjustedCount)));
    }

    @GetMapping("/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get demand analytics dashboard (ADMIN)")
    public ResponseEntity<ApiResponse<DemandAnalyticsResponse>> getDemandAnalytics() {
        DemandAnalyticsResponse analytics = dynamicPricingService.getDemandAnalytics();
        return ResponseEntity.ok(ApiResponse.success("Demand analytics fetched", analytics));
    }
}