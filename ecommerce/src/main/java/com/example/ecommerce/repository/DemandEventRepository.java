package com.example.ecommerce.repository;

import com.example.ecommerce.entity.DemandEvent;
import com.example.ecommerce.entity.DemandEvent.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 📁 Location: com.example.ecommerce.repository
 * 📝 Action:   CREATE NEW FILE (Interface)
 */
@Repository
public interface DemandEventRepository extends JpaRepository<DemandEvent, Long> {

    // Count all events for a product within a time window
    Long countByProductIdAndCreatedAtAfter(Long productId, LocalDateTime after);

    // Count specific event type for a product
    Long countByProductIdAndEventTypeAndCreatedAtAfter(
            Long productId, EventType eventType, LocalDateTime after);

    // Get demand score for ALL products in one query (used by scheduler)
    @Query("SELECT d.productId, COUNT(d) as eventCount " +
            "FROM DemandEvent d " +
            "WHERE d.createdAt > :since " +
            "GROUP BY d.productId")
    List<Object[]> getDemandScoresForAllProducts(@Param("since") LocalDateTime since);

    // Weighted demand score: PURCHASE=5, CART_ADD=3, WISHLIST_ADD=2, VIEW=1
    @Query("SELECT d.productId, " +
            "SUM(CASE " +
            "  WHEN d.eventType = 'PURCHASE' THEN 5 " +
            "  WHEN d.eventType = 'CART_ADD' THEN 3 " +
            "  WHEN d.eventType = 'WISHLIST_ADD' THEN 2 " +
            "  WHEN d.eventType = 'VIEW' THEN 1 " +
            "  ELSE 0 END) as weightedScore " +
            "FROM DemandEvent d " +
            "WHERE d.createdAt > :since " +
            "GROUP BY d.productId")
    List<Object[]> getWeightedDemandScores(@Param("since") LocalDateTime since);

    // Top trending products
    @Query("SELECT d.productId, COUNT(d) as eventCount " +
            "FROM DemandEvent d " +
            "WHERE d.createdAt > :since " +
            "GROUP BY d.productId " +
            "ORDER BY eventCount DESC")
    List<Object[]> getTopTrendingProducts(@Param("since") LocalDateTime since);

    // Cleanup old events
    void deleteByCreatedAtBefore(LocalDateTime before);
}