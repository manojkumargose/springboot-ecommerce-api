package com.example.aipricingservice.service;

import com.example.aipricingservice.dto.DemandAnalyticsResponse;
import com.example.aipricingservice.dto.TrendingProductDto;
import com.example.aipricingservice.repository.DemandEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor // Automatically generates the constructor for final fields
public class DemandAnalyticsService {

    private final DemandEventRepository demandEventRepository;

    @Transactional(readOnly = true)
    public DemandAnalyticsResponse getAnalytics() {
        log.info("Generating demand analytics report...");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last24h = now.minusHours(24);
        LocalDateTime last7d = now.minusDays(7);

        // 1. Fetch and map trending products
        List<TrendingProductDto> trending = getTrendingProducts(24);

        // 2. Aggregate counts efficiently
        long total24h = demandEventRepository.countByCreatedAtAfter(last24h);
        long total7d = demandEventRepository.countByCreatedAtAfter(last7d);

        log.debug("Analytics results: 24h count: {}, 7d count: {}", total24h, total7d);

        // 3. Build Response
        DemandAnalyticsResponse resp = new DemandAnalyticsResponse();
        resp.setTrendingProducts(trending);
        resp.setTotalEventsLast24h(total24h);
        resp.setTotalEventsLast7d(total7d);
        resp.setTopEventType("PURCHASE");

        return resp;
    }

    @Transactional(readOnly = true)
    public List<TrendingProductDto> getTrendingProducts(int hours) {
        log.info("Fetching trending products for the last {} hours", hours);

        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        List<Object[]> rawResults = demandEventRepository.findTopProductsByDemand(since);

        if (rawResults.isEmpty()) {
            log.warn("No demand data found in the last {} hours. Returning empty list.", hours);
        }

        return rawResults.stream()
                .limit(10)
                .map(this::mapToTrendingDto)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to map raw Object arrays from the Repository to DTOs.
     * Prevents code duplication.
     */
    private TrendingProductDto mapToTrendingDto(Object[] row) {
        try {
            // Safe conversion using Number to handle different DB types (Long/Integer)
            Long productId = ((Number) row[0]).longValue();
            Long count = ((Number) row[1]).longValue();

            // Dynamic Demand Level Logic
            String level = "LOW";
            if (count >= 50) {
                level = "HIGH";
            } else if (count >= 20) {
                level = "MEDIUM";
            }

            return new TrendingProductDto(productId, count, level);
        } catch (Exception e) {
            log.error("Error mapping repository row to TrendingProductDto: {}", e.getMessage());
            return null; // Or throw a custom exception
        }
    }
}