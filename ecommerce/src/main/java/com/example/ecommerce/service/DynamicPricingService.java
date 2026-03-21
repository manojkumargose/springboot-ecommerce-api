package com.example.ecommerce.service;

import com.example.ecommerce.dto.*;
import com.example.ecommerce.entity.PricingRule;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.Product.DemandLevel;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.PricingRuleRepository;
import com.example.ecommerce.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DynamicPricingService {

    private static final Logger log = LoggerFactory.getLogger(DynamicPricingService.class);

    private final ProductRepository productRepository;
    private final PricingRuleRepository pricingRuleRepository;
    private final DemandTrackingService demandTrackingService;

    public DynamicPricingService(ProductRepository productRepository,
                                 PricingRuleRepository pricingRuleRepository,
                                 DemandTrackingService demandTrackingService) {
        this.productRepository = productRepository;
        this.pricingRuleRepository = pricingRuleRepository;
        this.demandTrackingService = demandTrackingService;
    }

    // ═══════════════════════════════════════════════════════════════
    //  CORE: Recalculate prices for ALL products
    // ═══════════════════════════════════════════════════════════════

    @Transactional
    public int recalculateAllPrices() {
        log.info("Starting dynamic price recalculation for all products...");

        PricingRule defaultRule = getDefaultRule();
        int windowHours = defaultRule.getDemandWindowHours();

        Map<Long, Long> demandScores = demandTrackingService.getWeightedDemandScores(windowHours);

        List<Product> products = productRepository.findAll();
        int adjustedCount = 0;

        for (Product product : products) {
            PricingRule rule = findRuleForProduct(product, defaultRule);
            long score = demandScores.getOrDefault(product.getId(), 0L);
            boolean changed = applyDynamicPrice(product, rule, score);
            if (changed) adjustedCount++;
        }

        productRepository.saveAll(products);

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
            case HIGH:   multiplier = rule.getHighDemandMultiplier(); break;
            case MEDIUM: multiplier = rule.getMediumDemandMultiplier(); break;
            case LOW:    multiplier = rule.getLowDemandMultiplier(); break;
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
        product.setLastPriceUpdate(LocalDateTime.now());

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
        if (score >= rule.getHighDemandThreshold()) {
            return DemandLevel.HIGH;
        } else if (score >= rule.getMediumDemandThreshold()) {
            return DemandLevel.MEDIUM;
        } else {
            return DemandLevel.LOW;
        }
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
        return pricingRuleRepository.findByCategoryIsNullAndIsActiveTrue()
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

    private PricingRule findRuleForProduct(Product product, PricingRule defaultRule) {
        if (product.getCategory() != null) {
            return pricingRuleRepository
                    .findByCategoryIdAndIsActiveTrue(product.getCategory().getId())
                    .orElse(defaultRule);
        }
        return defaultRule;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Query methods (used by controller)
    // ═══════════════════════════════════════════════════════════════

    public ProductPricingResponse getProductPricing(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

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

    public List<ProductPricingResponse> getAllProductPricing() {
        return productRepository.findAll().stream()
                .map(product -> {
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
                })
                .collect(Collectors.toList());
    }

    public DemandAnalyticsResponse getDemandAnalytics() {
        PricingRule rule = getDefaultRule();
        int windowHours = rule.getDemandWindowHours();

        List<Product> products = productRepository.findAll();
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
                    Product p = productRepository.findById(pid).orElse(null);
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