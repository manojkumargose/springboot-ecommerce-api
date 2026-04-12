package com.example.coreservice.controller;

import com.example.coreservice.dto.*;
import com.example.coreservice.entity.Review;
import com.example.coreservice.security.JwtUtil;
import com.example.coreservice.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    private final JwtUtil jwtUtil;

    public ReviewController(ReviewService reviewService, JwtUtil jwtUtil) {
        this.reviewService = reviewService; this.jwtUtil = jwtUtil;
    }

    private Long getUserId(HttpServletRequest req) {
        return jwtUtil.extractUserId(req.getHeader("Authorization").substring(7));
    }
    private String getUsername(HttpServletRequest req) {
        return jwtUtil.extractUsername(req.getHeader("Authorization").substring(7));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Review>> addReview(@Valid @RequestBody ReviewRequest request,
                                                          HttpServletRequest req) {
        Review review = reviewService.addReview(request, getUserId(req), getUsername(req));
        return ResponseEntity.ok(ApiResponse.success("Review added", review));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<Review>>> getProductReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success("Reviews fetched", reviewService.getProductReviews(productId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id, HttpServletRequest req) {
        reviewService.deleteReview(id, getUserId(req));
        return ResponseEntity.ok(ApiResponse.success("Review deleted", null));
    }
}
