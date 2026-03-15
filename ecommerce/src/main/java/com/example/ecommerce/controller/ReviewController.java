package com.example.ecommerce.controller;

import com.example.ecommerce.dto.ApiResponse;
import com.example.ecommerce.dto.ReviewRequest;
import com.example.ecommerce.dto.ReviewResponse;
import com.example.ecommerce.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products/{productId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // ─── Add Review (Logged in User) ──────────────────────────

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReviewResponse>> addReview(
            @PathVariable Long productId,
            @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Review added successfully",
                reviewService.addReview(productId, request)));
    }

    // ─── Update Review (Owner Only) ───────────────────────────

    @PutMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable Long productId,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Review updated successfully",
                reviewService.updateReview(reviewId, request)));
    }

    // ─── Delete Review (Owner or Admin) ───────────────────────

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long productId,
            @PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok(ApiResponse.success(
                "Review deleted successfully", null));
    }

    // ─── Get All Reviews for Product (Public) ─────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getProductReviews(
            @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Reviews fetched",
                reviewService.getProductReviews(productId)));
    }

    // ─── Get Average Rating (Public) ──────────────────────────

    @GetMapping("/rating")
    public ResponseEntity<ApiResponse<Double>> getAverageRating(
            @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Average rating fetched",
                reviewService.getAverageRating(productId)));
    }
}