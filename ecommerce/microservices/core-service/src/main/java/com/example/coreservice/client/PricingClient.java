package com.example.coreservice.client;

import com.example.coreservice.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "AI-PRICING-SERVICE")
public interface PricingClient {

    // 🎯 ONE method that passes BOTH the ID and the original Price
    @GetMapping("/api/pricing/calculate/{productId}")
    ApiResponse<Double> getDynamicPrice(
            @PathVariable("productId") Long productId, Double basePrice);
}