package com.example.ecommerce.controller;

import com.example.ecommerce.dto.ApiResponse;
import com.example.ecommerce.dto.ProductResponse;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/recommendations")
@Tag(name = "Recommendations", description = "AI-powered product recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get AI recommendations for a product",
            description = "Uses hybrid ML model: TF-IDF content similarity + collaborative filtering")
    public ResponseEntity<ApiResponse> getRecommendations(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "5") int limit) {

        List<Product> recommendations = recommendationService.getRecommendations(productId, limit);
        List<ProductResponse> response = recommendations.stream()
                .map(this::toProductResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse(true, "AI recommendations generated", response));
    }

    @GetMapping("/{productId}/similar")
    @Operation(summary = "Get similar products",
            description = "Content-based filtering using TF-IDF and cosine similarity")
    public ResponseEntity<ApiResponse> getSimilarProducts(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "5") int limit) {

        List<Product> similar = recommendationService.getSimilarProducts(productId, limit);
        List<ProductResponse> response = similar.stream()
                .map(this::toProductResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse(true, "Similar products found", response));
    }

    @GetMapping("/{productId}/bought-together")
    @Operation(summary = "Get frequently bought together products",
            description = "Collaborative filtering based on co-purchase analysis")
    public ResponseEntity<ApiResponse> getBoughtTogether(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "5") int limit) {

        List<Product> boughtTogether = recommendationService.getBoughtTogether(productId, limit);
        List<ProductResponse> response = boughtTogether.stream()
                .map(this::toProductResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse(true, "Frequently bought together", response));
    }

    private ProductResponse toProductResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setPrice(product.getPrice());
        response.setDescription(product.getDescription());
        response.setCategoryName(product.getCategory() != null ? product.getCategory().getName() : null);
        response.setImageUrl(product.getImageUrl());
        response.setInStock(product.getInStock());
        response.setStockQuantity(product.getStockQuantity());
        return response;
    }
}