package com.example.ecommerce.service;

import com.example.ecommerce.dto.ReviewRequest;
import com.example.ecommerce.dto.ReviewResponse;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.Review;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.ReviewRepository;
import com.example.ecommerce.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         ProductRepository productRepository,
                         UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // ─── Get logged in user ───────────────────────────────────

    private User getLoggedInUser() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // ─── Add Review ───────────────────────────────────────────

    public ReviewResponse addReview(Long productId, ReviewRequest request) {
        User user = getLoggedInUser();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found: " + productId));

        // ── Check if user already reviewed this product ───────
        if (reviewRepository.findByUserIdAndProductId(
                user.getId(), productId).isPresent()) {
            throw new RuntimeException(
                    "You have already reviewed this product");
        }

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        return mapToResponse(reviewRepository.save(review));
    }

    // ─── Update Review ────────────────────────────────────────

    public ReviewResponse updateReview(Long reviewId, ReviewRequest request) {
        User user = getLoggedInUser();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Review not found: " + reviewId));

        // ── Make sure review belongs to logged in user ────────
        if (!review.getUser().getId().equals(user.getId())) {
            throw new RuntimeException(
                    "You are not authorized to update this review");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        return mapToResponse(reviewRepository.save(review));
    }

    // ─── Delete Review ────────────────────────────────────────

    public void deleteReview(Long reviewId) {
        User user = getLoggedInUser();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Review not found: " + reviewId));

        // ── Allow user to delete own review or admin ──────────
        boolean isAdmin = user.getRole().equals("ROLE_ADMIN");
        boolean isOwner = review.getUser().getId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new RuntimeException(
                    "You are not authorized to delete this review");
        }

        reviewRepository.delete(review);
    }

    // ─── Get Reviews for Product ──────────────────────────────

    public List<ReviewResponse> getProductReviews(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException(
                    "Product not found: " + productId);
        }
        return reviewRepository.findByProductId(productId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Get Average Rating ───────────────────────────────────

    public Double getAverageRating(Long productId) {
        Double avg = reviewRepository.findAverageRatingByProductId(productId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    // ─── Get Review Count ─────────────────────────────────────

    public long getReviewCount(Long productId) {
        return reviewRepository.countByProductId(productId);
    }

    // ─── Map to Response ──────────────────────────────────────

    private ReviewResponse mapToResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setUsername(review.getUser().getUsername());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }
}