package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Get all reviews for a product
    List<Review> findByProductId(Long productId);

    // Check if user already reviewed a product
    Optional<Review> findByUserIdAndProductId(Long userId, Long productId);

    // Get average rating for a single product
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double findAverageRatingByProductId(@Param("productId") Long productId);

    // Count reviews for a single product
    long countByProductId(Long productId);

    // ✅ FIX: Batch query — get avg rating AND count for ALL products in ONE query
    // Returns Object[] rows: [productId, avgRating, reviewCount]
    @Query("SELECT r.product.id, AVG(r.rating), COUNT(r.id) " +
            "FROM Review r GROUP BY r.product.id")
    List<Object[]> findAllReviewStats();

    // ✅ FIX: Batch query — get stats for a specific set of product IDs
    @Query("SELECT r.product.id, AVG(r.rating), COUNT(r.id) " +
            "FROM Review r WHERE r.product.id IN :productIds GROUP BY r.product.id")
    List<Object[]> findReviewStatsByProductIds(@Param("productIds") List<Long> productIds);
}