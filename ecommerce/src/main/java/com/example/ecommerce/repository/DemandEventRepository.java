package com.example.ecommerce.repository;

import com.example.ecommerce.entity.DemandEvent;
import com.example.ecommerce.entity.DemandEvent.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DemandEventRepository extends JpaRepository<DemandEvent, Long> {

    // ─── FIX: Added for scheduler early-exit check ────────────────────────────
    // DynamicPricingService calls this before running any pricing logic.
    // If the result is 0, the entire recalculation is skipped — no product
    // SELECTs, no rule SELECTs, no UPDATE statements fire at idle.

    long countByCreatedAtAfter(LocalDateTime after);

    // ─── Existing queries (no changes needed) ─────────────────────────────────

    Long countByProductIdAndCreatedAtAfter(Long productId, LocalDateTime after);

    Long countByProductIdAndEventTypeAndCreatedAtAfter(
            Long productId, EventType eventType, LocalDateTime after);

    @Query("SELECT d.productId, COUNT(d) as eventCount " +
            "FROM DemandEvent d " +
            "WHERE d.createdAt > :since " +
            "GROUP BY d.productId")
    List<Object[]> getDemandScoresForAllProducts(@Param("since") LocalDateTime since);

    @Query("SELECT d.productId, " +
            "SUM(CASE " +
            "  WHEN d.eventType = 'PURCHASE'     THEN 5 " +
            "  WHEN d.eventType = 'CART_ADD'     THEN 3 " +
            "  WHEN d.eventType = 'WISHLIST_ADD' THEN 2 " +
            "  WHEN d.eventType = 'VIEW'         THEN 1 " +
            "  ELSE 0 END) as weightedScore " +
            "FROM DemandEvent d " +
            "WHERE d.createdAt > :since " +
            "GROUP BY d.productId")
    List<Object[]> getWeightedDemandScores(@Param("since") LocalDateTime since);

    @Query("SELECT d.productId, COUNT(d) as eventCount " +
            "FROM DemandEvent d " +
            "WHERE d.createdAt > :since " +
            "GROUP BY d.productId " +
            "ORDER BY eventCount DESC")
    List<Object[]> getTopTrendingProducts(@Param("since") LocalDateTime since);

    void deleteByCreatedAtBefore(LocalDateTime before);
}