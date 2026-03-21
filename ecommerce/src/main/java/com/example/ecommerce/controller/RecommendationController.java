package com.example.ecommerce.controller;

import com.example.ecommerce.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/recommendations")
@Tag(name = "Recommendations", description = "AI-powered product recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    // POST /api/recommendations/product
    // Body: { "productId": 1, "userId": 123, "topN": 5 }
    @PostMapping("/product")
    @Operation(summary = "Get recommendations for a product")
    public ResponseEntity<List<Map<String, Object>>> recommendForProduct(
            @RequestBody Map<String, Object> body) {

        Long productId = Long.valueOf(body.get("productId").toString());
        Long userId    = body.containsKey("userId") ? Long.valueOf(body.get("userId").toString()) : null;
        int  topN      = body.containsKey("topN")   ? Integer.parseInt(body.get("topN").toString()) : 5;

        List<Map<String, Object>> recommendations =
                recommendationService.getRecommendations(productId, userId, topN);

        return ResponseEntity.ok(recommendations);
    }

    // GET /api/recommendations/user/{userId}?topN=5
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get personalised recommendations for a user")
    public ResponseEntity<List<Map<String, Object>>> recommendForUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "5") int topN) {

        List<Map<String, Object>> recommendations =
                recommendationService.getRecommendationsForUser(userId, topN);

        return ResponseEntity.ok(recommendations);
    }

    // GET /api/recommendations/popular?topN=5
    @GetMapping("/popular")
    @Operation(summary = "Get popular products")
    public ResponseEntity<List<Map<String, Object>>> popularProducts(
            @RequestParam(defaultValue = "5") int topN) {

        List<Map<String, Object>> recommendations =
                recommendationService.getPopularProducts(topN);

        return ResponseEntity.ok(recommendations);
    }

    // GET /api/recommendations/health
    @GetMapping("/health")
    @Operation(summary = "Check ML service health")
    public ResponseEntity<Map<String, Object>> health() {
        boolean healthy = recommendationService.isMlServiceHealthy();
        return ResponseEntity.ok(Map.of(
                "mlServiceStatus", healthy ? "UP" : "DOWN"
        ));
    }
}