package com.example.ecommerce.service;

import com.example.ecommerce.dto.*;
import com.example.ecommerce.entity.PricingRule;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.Product.DemandLevel;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.DemandEventRepository;
import com.example.ecommerce.repository.PricingRuleRepository;
import com.example.ecommerce.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DynamicPricingService {

    private static final Logger log = LoggerFactory.getLogger(DynamicPricingService.class);

    private final ProductRepository productRepository;
    private final PricingRuleRepository pricingRuleRepository;
    private final DemandTrackingService demandTrackingService;
    private final DemandEventRepository demandEventRepository;

    public DynamicPricingService(ProductRepository productRepository,
                                 PricingRuleRepository pricingRuleRepository,
                                 DemandTrackingService demandTrackingService,
                                 DemandEventRepository demandEventRepository) {
        this.productRepository = productRepository;
        this.pricingRuleRepository = pricingRuleRepository;
        this.demandTrackingService = demandTrackingService;
        this.demandEventRepository = demandEventRepository;
    }

    // ═══════════════════════════════════════════════════════════════
    //  FIX #4: Increase interval to 5 minutes + early exit if no
    //  recent demand events — stops hammering DB every 60 seconds
    //  when nothing has changed.
    // ═══════════════════════════════════════════════════════════════

    @Scheduled(fixedRate = 300000) // every 5 minutes
    public void scheduledPriceRecalculation() {
        log.info("⏰ Scheduled price recalculation triggered...");
        try {
            // FIX #4: Early exit — skip the whole recalculation if there
            // are no demand events in the last 24 hours.
            PricingRule defaultRule = getDefaultRule();
            LocalDateTime windowStart = LocalDateTime.now(ZoneOffset.UTC)
                    .minusHours(defaultRule.getDemandWindowHours());
            long recentEvents = demandEventRepository
                    .countByCreatedAtAfter(windowStart);

            if (recentEvents == 0) {
                log.info("No recent demand events — skipping recalculation.");
                return;
            }

            int adjusted = recalculateAllPrices();
            if (adjusted > 0) {
                log.info("✅ Auto-recalculation complete: {} products adjusted.", adjusted);
            }
        } catch (Exception e) {
            log.error("❌ Scheduled recalculation failed: {}", e.getMessage(), e);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  CORE: Recalculate prices for ALL products
    //
    //  FIX #1: Use findAllWithCategory() (JOIN FETCH) instead of
    //  findAll() — eliminates N lazy SELECT per product for category.
    //
    //  FIX #2: Pre-load ALL pricing rules into a Map before the loop
    //  instead of calling findByCategoryIdAndIsActiveTrue() per
    //  product — eliminates the duplicate per-product rule queries.
    //
    //  FIX #3: Only call saveAll() when at least one price changed.
    // ═══════════════════════════════════════════════════════════════

    @Transactional
    public int recalculateAllPrices() {
        log.info("Starting dynamic price recalculation for all products...");

        PricingRule defaultRule = getDefaultRule();
        int windowHours = defaultRule.getDemandWindowHours();

        // FIX #2: Load all active rules once, keyed by categoryId.
        // null key = the global default rule (no category).
        Map<Long, PricingRule> rulesByCategoryId = pricingRuleRepository
                .findByIsActiveTrue()
                .stream()
                .filter(r -> r.getCategory() != null)
                .collect(Collectors.toMap(
                        r -> r.getCategory().getId(),
                        r -> r,
                        (existing, replacement) -> existing
                ));

        Map<Long, Long> demandScores = demandTrackingService
                .getWeightedDemandScores(windowHours);

        // FIX #1: JOIN FETCH category — no lazy loads inside the loop.
        List<Product> products = productRepository.findAllWithCategory();
        int adjustedCount = 0;

        for (Product product : products) {
            // FIX #2: Rule lookup from pre-loaded map — zero extra queries.
            PricingRule rule = (product.getCategory() != null)
                    ? rulesByCategoryId.getOrDefault(
                    product.getCategory().getId(), defaultRule)
                    : defaultRule;

            long score = demandScores.getOrDefault(product.getId(), 0L);
            boolean changed = applyDynamicPrice(product, rule, score);
            if (changed) adjustedCount++;
        }

        // FIX #3: Skip the UPDATE statements entirely if nothing changed.
        if (adjustedCount > 0) {
            productRepository.saveAll(products);
        }

        log.info("Price recalculation complete. {} out of {} products adjusted.",
                adjustedCount, products.size());
        return adjustedCount;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Calculate and apply price for a SINGLE product
    // ═══════════════════════════════════════════════════════════════

    private boolean applyDynamicPrice(Product product, PricingRule rule, long demandScore) {
        Double basePrice = product.getBasePrice();
        if (basePrice == null || basePrice <= 0) return false;

        Double oldPrice = product.getCurrentPrice();

        DemandLevel level = determineDemandLevel(demandScore, rule);

        double multiplier;
        switch (level) {
            case HIGH:   multiplier = rule.getHighDemandMultiplier();   break;
            case MEDIUM: multiplier = rule.getMediumDemandMultiplier(); break;
            case LOW:    multiplier = rule.getLowDemandMultiplier();    break;
            default:     multiplier = 1.0;
        }

        double newPrice = basePrice * multiplier;

        double maxPrice = basePrice * (1 + rule.getMaxPriceIncreasePercent() / 100.0);
        double minPrice = basePrice * (1 - rule.getMaxPriceDecreasePercent() / 100.0);
        newPrice = Math.max(minPrice, Math.min(maxPrice, newPrice));
        newPrice = Math.round(newPrice * 100.0) / 100.0;

        double changePercent = ((newPrice - basePrice) / basePrice) * 100.0;
        changePercent = Math.round(changePercent * 100.0) / 100.0;

        product.setCurrentPrice(newPrice);
        product.setDemandLevel(level);
        product.setDemandScore((int) demandScore);
        product.setPriceChangePercent(changePercent);
        product.setLastPriceUpdate(LocalDateTime.now(ZoneOffset.UTC));

        boolean priceChanged = oldPrice == null || Math.abs(oldPrice - newPrice) > 0.01;

        if (priceChanged) {
            log.debug("Product [{}] '{}': base={}, new={} ({}%), demand={} (score={})",
                    product.getId(), product.getName(), basePrice, newPrice,
                    changePercent > 0 ? "+" + changePercent : changePercent,
                    level, demandScore);
        }

        return priceChanged;
    }

    private DemandLevel determineDemandLevel(long score, PricingRule rule) {
        if (score >= rule.getHighDemandThreshold())   return DemandLevel.HIGH;
        if (score >= rule.getMediumDemandThreshold()) return DemandLevel.MEDIUM;
        return DemandLevel.LOW;
    }

    // ═══════════════════════════════════════════════════════════════
    //  PRICING RULES management
    // ═══════════════════════════════════════════════════════════════

    @Transactional
    public PricingRule createOrUpdateRule(PricingRuleRequest request) {
        PricingRule rule;
        if (request.getCategoryId() != null) {
            rule = pricingRuleRepository
                    .findByCategoryIdAndIsActiveTrue(request.getCategoryId())
                    .orElse(new PricingRule());
        } else {
            rule = pricingRuleRepository
                    .findByCategoryIsNullAndIsActiveTrue()
                    .orElse(new PricingRule());
        }

        rule.setHighDemandThreshold(request.getHighDemandThreshold());
        rule.setMediumDemandThreshold(request.getMediumDemandThreshold());
        rule.setHighDemandMultiplier(request.getHighDemandMultiplier());
        rule.setMediumDemandMultiplier(request.getMediumDemandMultiplier());
        rule.setLowDemandMultiplier(request.getLowDemandMultiplier());
        rule.setMaxPriceIncreasePercent(request.getMaxPriceIncreasePercent());
        rule.setMaxPriceDecreasePercent(request.getMaxPriceDecreasePercent());
        rule.setDemandWindowHours(request.getDemandWindowHours());
        rule.setIsActive(true);
        return pricingRuleRepository.save(rule);
    }

    public PricingRule getDefaultRule() {
        return pricingRuleRepository
                .findByCategoryIsNullAndIsActiveTrue()
                .orElseGet(this::createDefaultRule);
    }

    @Transactional
    public PricingRule createDefaultRule() {
        PricingRule rule = new PricingRule();
        rule.setHighDemandThreshold(100);
        rule.setMediumDemandThreshold(30);
        rule.setHighDemandMultiplier(1.15);
        rule.setMediumDemandMultiplier(1.00);
        rule.setLowDemandMultiplier(0.90);
        rule.setMaxPriceIncreasePercent(30.0);
        rule.setMaxPriceDecreasePercent(25.0);
        rule.setDemandWindowHours(24);
        rule.setIsActive(true);
        return pricingRuleRepository.save(rule);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Query methods (used by controller)
    // ═══════════════════════════════════════════════════════════════

    public ProductPricingResponse getProductPricing(Long productId) {
        // FIX #1: Use findByIdWithCategory to avoid lazy load
        Product product = productRepository.findByIdWithCategory(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        return toProductPricingResponse(product);
    }

    public List<ProductPricingResponse> getAllProductPricing() {
        // FIX #1: JOIN FETCH here too
        return productRepository.findAllWithCategory().stream()
                .map(this::toProductPricingResponse)
                .collect(Collectors.toList());
    }

    private ProductPricingResponse toProductPricingResponse(Product product) {
        ProductPricingResponse response = new ProductPricingResponse();
        response.setProductId(product.getId());
        response.setProductName(product.getName());
        response.setBasePrice(product.getBasePrice());
        response.setCurrentPrice(product.getCurrentPrice());
        response.setPriceChangePercent(product.getPriceChangePercent());
        response.setDemandLevel(product.getDemandLevel());
        response.setDemandScore(product.getDemandScore());
        response.setLastPriceUpdate(product.getLastPriceUpdate());
        return response;
    }

    // ═══════════════════════════════════════════════════════════════
    //  FIX #5: getDemandAnalytics was calling productRepository
    //  .findById(pid) inside a loop — one SELECT per trending product.
    //  Fixed by loading all products into a Map once.
    // ═══════════════════════════════════════════════════════════════

    public DemandAnalyticsResponse getDemandAnalytics() {
        PricingRule rule = getDefaultRule();
        int windowHours = rule.getDemandWindowHours();

        // FIX #1: JOIN FETCH — no lazy loads
        List<Product> products = productRepository.findAllWithCategory();

        // FIX #5: Pre-load into Map so we don't hit DB inside the loop
        Map<Long, Product> productById = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<Object[]> trending = demandTrackingService.getTopTrending(windowHours, 10);

        long highCount = products.stream()
                .filter(p -> p.getDemandLevel() == DemandLevel.HIGH).count();
        long lowCount = products.stream()
                .filter(p -> p.getDemandLevel() == DemandLevel.LOW).count();
        long adjustedCount = products.stream()
                .filter(p -> p.getPriceChangePercent() != null && p.getPriceChangePercent() != 0.0)
                .count();

        List<TrendingProductDto> trendingDtos = trending.stream()
                .map(row -> {
                    Long pid = (Long) row[0];
                    Long count = (Long) row[1];
                    // FIX #5: map lookup — zero extra DB queries
                    Product p = productById.get(pid);
                    if (p == null) return null;

                    TrendingProductDto dto = new TrendingProductDto();
                    dto.setProductId(pid);
                    dto.setProductName(p.getName());
                    dto.setEventCount(count);
                    dto.setDemandLevel(p.getDemandLevel());
                    dto.setCurrentPrice(p.getCurrentPrice());
                    dto.setPriceChangePercent(p.getPriceChangePercent());
                    return dto;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        DemandAnalyticsResponse analytics = new DemandAnalyticsResponse();
        analytics.setTopTrendingProducts(trendingDtos);
        analytics.setProductsWithHighDemand((int) highCount);
        analytics.setProductsWithLowDemand((int) lowCount);
        analytics.setTotalPriceAdjustments((int) adjustedCount);
        return analytics;
    }
}