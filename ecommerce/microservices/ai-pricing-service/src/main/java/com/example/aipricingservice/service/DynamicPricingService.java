package com.example.aipricingservice.service;

import com.example.aipricingservice.entity.Product;
import com.example.aipricingservice.entity.PriceHistory;
import com.example.aipricingservice.entity.PricingRule;
import com.example.aipricingservice.entity.DemandLevel;
import com.example.aipricingservice.repository.ProductRepository;
import com.example.aipricingservice.repository.PricingRuleRepository;
import com.example.aipricingservice.repository.DemandEventRepository;
import com.example.aipricingservice.repository.PriceHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
public class DynamicPricingService {

    private static final Logger log = LoggerFactory.getLogger(DynamicPricingService.class);

    private final PricingRuleRepository pricingRuleRepository;
    private final ProductRepository productRepository;
    private final DemandEventRepository demandEventRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final PriceUpdatePublisher priceUpdatePublisher; // NEW

    public DynamicPricingService(PricingRuleRepository pricingRuleRepository,
                                 ProductRepository productRepository,
                                 DemandEventRepository demandEventRepository,
                                 PriceHistoryRepository priceHistoryRepository,
                                 PriceUpdatePublisher priceUpdatePublisher) {
        this.pricingRuleRepository = pricingRuleRepository;
        this.productRepository = productRepository;
        this.demandEventRepository = demandEventRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.priceUpdatePublisher = priceUpdatePublisher;
    }

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void recalculatePrices() {
        log.info("⏰ AI Brain Heartbeat: Starting price recalculation...");

        PricingRule rule = pricingRuleRepository.findById(1L)
                .orElseGet(this::createDefaultRule);

        LocalDateTime windowStart = LocalDateTime.now(ZoneOffset.UTC)
                .minusHours(rule.getDemandWindowHours());

        List<Product> products = productRepository.findAll();
        int adjustedCount = 0;

        for (Product product : products) {
            Long score = demandEventRepository.calculateWeightedScore(product.getId(), windowStart);
            score = (score == null || score < 0) ? 0L : score;

            log.info("🔍 Checking Product: {} | Score: {} | High Threshold: {} | Medium Threshold: {}",
                    product.getName(), score, rule.getHighDemandThreshold(), rule.getMediumDemandThreshold());

            double oldPrice = product.getCurrentPrice();
            double newPrice;

            if (score >= rule.getHighDemandThreshold()) {
                newPrice = product.getBasePrice() * rule.getHighDemandMultiplier();
                product.setDemandLevel(DemandLevel.HIGH);
                log.warn("🚀 SURGE DETECTED for {}", product.getName());
            } else if (score >= rule.getMediumDemandThreshold()) {
                newPrice = product.getBasePrice() * rule.getMediumDemandMultiplier();
                product.setDemandLevel(DemandLevel.MEDIUM);
            } else {
                newPrice = product.getBasePrice();
                product.setDemandLevel(DemandLevel.LOW);
            }

            if (Math.abs(newPrice - oldPrice) > 0.01) {
                product.setCurrentPrice(newPrice);
                product.setDemandScore(score.intValue());
                product.setLastPriceUpdate(LocalDateTime.now(ZoneOffset.UTC));
                productRepository.save(product);
                adjustedCount++;

                // NEW: Notify monolith about the price change
                priceUpdatePublisher.publishPriceUpdate(
                        product.getId(), newPrice,
                        "AI recalculation: demand=" + product.getDemandLevel()
                );
            }
        }
        log.info("✅ Recalculation complete. {} out of {} products adjusted.", adjustedCount, products.size());
    }

    public Double getCurrentPrice(Long productId, Double basePrice) {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            return product.getCurrentPrice() > 0 ? product.getCurrentPrice() : basePrice;
        }
        log.warn("⚠️ Product {} not found, returning base price.", productId);
        return basePrice;
    }

    @Transactional
    public void recalculatePrice(Long productId, Long categoryId) {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isEmpty()) {
            log.warn("⚠️ Product {} not found. Skipping recalculation.", productId);
            return;
        }

        Product product = optionalProduct.get();

        Optional<PricingRule> ruleOpt = (categoryId != null)
                ? pricingRuleRepository.findByCategoryIdAndActiveTrue(categoryId)
                : Optional.empty();
        if (ruleOpt.isEmpty()) {
            ruleOpt = pricingRuleRepository.findByCategoryIdIsNullAndActiveTrue();
        }

        PricingRule rule = ruleOpt.orElseGet(this::createDefaultRule);

        LocalDateTime windowStart = LocalDateTime.now(ZoneOffset.UTC)
                .minusHours(rule.getDemandWindowHours());

        Long score = demandEventRepository.calculateWeightedScore(productId, windowStart);
        score = (score == null || score < 0) ? 0L : score;

        double oldPrice = product.getCurrentPrice();
        double newPrice;

        if (score >= rule.getHighDemandThreshold()) {
            newPrice = product.getBasePrice() * rule.getHighDemandMultiplier();
            product.setDemandLevel(DemandLevel.HIGH);
        } else if (score >= rule.getMediumDemandThreshold()) {
            newPrice = product.getBasePrice() * rule.getMediumDemandMultiplier();
            product.setDemandLevel(DemandLevel.MEDIUM);
        } else {
            newPrice = product.getBasePrice();
            product.setDemandLevel(DemandLevel.LOW);
        }

        product.setCurrentPrice(newPrice);
        product.setDemandScore(score.intValue());
        product.setLastPriceUpdate(LocalDateTime.now(ZoneOffset.UTC));
        productRepository.save(product);

        priceHistoryRepository.save(new PriceHistory(productId, oldPrice, newPrice, "Manual recalculation triggered"));
        log.info("🔄 Recalculated price for Product {} | {} → {}", productId, oldPrice, newPrice);

        // NEW: Notify monolith
        priceUpdatePublisher.publishPriceUpdate(productId, newPrice, "Manual recalculation");
    }

    @Transactional
    public Double updatePriceWithHistory(Long productId, double weightMultiplier, String reason) {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isEmpty()) {
            log.warn("⚠️ Product not found for ID: {}. Skipping price update.", productId);
            return null;
        }

        Product product = optionalProduct.get();
        double oldPrice = product.getCurrentPrice();
        double adjustment = oldPrice * weightMultiplier;
        double newPrice = oldPrice + adjustment;

        if (newPrice < product.getBasePrice()) {
            newPrice = product.getBasePrice();
        }

        product.setCurrentPrice(newPrice);
        product.setLastPriceUpdate(LocalDateTime.now(ZoneOffset.UTC));
        productRepository.save(product);

        priceHistoryRepository.save(new PriceHistory(productId, oldPrice, newPrice, reason));
        log.info("💰 Price updated for {} | {} → {} | Reason: {}", product.getName(), oldPrice, newPrice, reason);

        // NEW: Notify monolith
        priceUpdatePublisher.publishPriceUpdate(productId, newPrice, reason);

        return newPrice;
    }

    public List<PriceHistory> getHistory(Long productId) {
        return priceHistoryRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    private PricingRule createDefaultRule() {
        log.info("📢 No rule found in Cloud DB. Creating Default Surge Rule...");
        PricingRule rule = new PricingRule();
        rule.setId(1L);
        rule.setDemandWindowHours(24);
        rule.setHighDemandThreshold(10);
        rule.setHighDemandMultiplier(1.5);
        rule.setMediumDemandThreshold(3);
        rule.setMediumDemandMultiplier(1.2);
        rule.setActive(true);
        return pricingRuleRepository.save(rule);
    }
}