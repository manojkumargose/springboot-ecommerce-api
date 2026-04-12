package com.example.apigateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/auth")
    public ResponseEntity<Map<String, String>> authFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "status", "error",
                "message", "Auth Service is currently unavailable. Please try again later.",
                "service", "auth-service"
            ));
    }

    @RequestMapping("/core")
    public ResponseEntity<Map<String, String>> coreFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "status", "error",
                "message", "Core Service is currently unavailable. Please try again later.",
                "service", "core-service"
            ));
    }

    @RequestMapping("/pricing")
    public ResponseEntity<Map<String, String>> pricingFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "status", "error",
                "message", "AI Pricing Service is currently unavailable. Showing base prices.",
                "service", "ai-pricing-service"
            ));
    }
}
