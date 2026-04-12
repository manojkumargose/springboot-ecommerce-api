package com.example.aipricingservice.repository;

import com.example.aipricingservice.entity.DemandEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DemandEventRepository extends JpaRepository<DemandEvent, Long> {

    // Used by DynamicPricingService
    @Query("SELECT SUM(d.weight) FROM DemandEvent d WHERE d.productId = :productId AND d.createdAt >= :windowStart")
    Long calculateWeightedScore(@Param("productId") Long productId,
                                @Param("windowStart") LocalDateTime windowStart);

    // Used by DemandAnalyticsService (lines 34 & 35)
    long countByCreatedAtAfter(LocalDateTime timestamp);

    // Used by DemandAnalyticsService (line 54) — returns top products ranked by demand count
    @Query("SELECT d.productId, COUNT(d) AS cnt FROM DemandEvent d WHERE d.createdAt >= :since GROUP BY d.productId ORDER BY cnt DESC")
    List<Object[]> findTopProductsByDemand(@Param("since") LocalDateTime since);
}