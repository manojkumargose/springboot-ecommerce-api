package com.example.ecommerce.service;

import com.example.ecommerce.dto.DemandEventRequest;
import com.example.ecommerce.entity.DemandEvent;
import com.example.ecommerce.entity.DemandEvent.EventType;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.DemandEventRepository;
import com.example.ecommerce.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DemandTrackingService {

    private static final Logger log = LoggerFactory.getLogger(DemandTrackingService.class);

    private final DemandEventRepository demandEventRepository;
    private final ProductRepository productRepository;

    public DemandTrackingService(DemandEventRepository demandEventRepository,
                                 ProductRepository productRepository) {
        this.demandEventRepository = demandEventRepository;
        this.productRepository = productRepository;
    }

    /**
     * Record a demand event from the API endpoint POST /api/pricing/track
     */
    @Transactional
    public void trackEvent(DemandEventRequest request, Long userId) {
        if (!productRepository.existsById(request.getProductId())) {
            throw new ResourceNotFoundException("Product not found: " + request.getProductId());
        }

        DemandEvent event = new DemandEvent();
        event.setProductId(request.getProductId());
        event.setEventType(request.getEventType());
        event.setUserId(userId);

        demandEventRepository.save(event);
        log.debug("Tracked {} event for product {} by user {}",
                request.getEventType(), request.getProductId(), userId);
    }

    /**
     * Call this from ProductController when a user views a product.
     */
    @Transactional
    public void trackProductView(Long productId, Long userId) {
        DemandEvent event = new DemandEvent();
        event.setProductId(productId);
        event.setEventType(EventType.VIEW);
        event.setUserId(userId);
        demandEventRepository.save(event);
    }

    /**
     * Call this from CartController when a user adds to cart.
     */
    @Transactional
    public void trackCartAdd(Long productId, Long userId) {
        DemandEvent event = new DemandEvent();
        event.setProductId(productId);
        event.setEventType(EventType.CART_ADD);
        event.setUserId(userId);
        demandEventRepository.save(event);
    }

    /**
     * Call this from OrderService when a user purchases a product.
     */
    @Transactional
    public void trackPurchase(Long productId, Long userId) {
        DemandEvent event = new DemandEvent();
        event.setProductId(productId);
        event.setEventType(EventType.PURCHASE);
        event.setUserId(userId);
        demandEventRepository.save(event);
    }

    /**
     * Get weighted demand scores for ALL products in one efficient query.
     */
    public Map<Long, Long> getWeightedDemandScores(int windowHours) {
        LocalDateTime since = LocalDateTime.now().minusHours(windowHours);
        List<Object[]> results = demandEventRepository.getWeightedDemandScores(since);

        return results.stream().collect(Collectors.toMap(
                row -> (Long) row[0],
                row -> (Long) row[1]
        ));
    }

    /**
     * Get raw event count for a single product.
     */
    public Long getEventCount(Long productId, int windowHours) {
        LocalDateTime since = LocalDateTime.now().minusHours(windowHours);
        return demandEventRepository.countByProductIdAndCreatedAtAfter(productId, since);
    }

    /**
     * Get top trending products by event count.
     */
    public List<Object[]> getTopTrending(int windowHours, int limit) {
        LocalDateTime since = LocalDateTime.now().minusHours(windowHours);
        List<Object[]> all = demandEventRepository.getTopTrendingProducts(since);
        return all.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * Delete events older than retentionDays.
     */
    @Transactional
    public void cleanupOldEvents(int retentionDays) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        demandEventRepository.deleteByCreatedAtBefore(cutoff);
        log.info("Cleaned up demand events older than {} days", retentionDays);
    }
}