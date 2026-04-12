package com.example.coreservice.service;

import com.example.coreservice.dto.ReviewRequest;
import com.example.coreservice.entity.Product;
import com.example.coreservice.entity.Review;
import com.example.coreservice.repository.ProductRepository;
import com.example.coreservice.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    public ReviewService(ReviewRepository reviewRepository, ProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
    }

    public Review addReview(ReviewRequest req, Long userId, String username) {
        if (reviewRepository.existsByProductIdAndUserId(req.getProductId(), userId))
            throw new RuntimeException("You have already reviewed this product");
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        Review review = new Review();
        review.setProduct(product);
        review.setUserId(userId);
        review.setUsername(username);
        review.setRating(req.getRating());
        review.setComment(req.getComment());
        return reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public List<Review> getProductReviews(Long productId) {
        return reviewRepository.findByProductId(productId);
    }

    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        if (!review.getUserId().equals(userId)) throw new RuntimeException("Unauthorized");
        reviewRepository.delete(review);
    }
}