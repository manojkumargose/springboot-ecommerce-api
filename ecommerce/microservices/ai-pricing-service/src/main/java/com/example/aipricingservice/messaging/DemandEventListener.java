package com.example.aipricingservice.messaging;

import com.example.aipricingservice.config.RabbitConfig;
import com.example.aipricingservice.entity.DemandEvent;
import com.example.aipricingservice.entity.DemandLevel;
import com.example.aipricingservice.entity.Product;
import com.example.aipricingservice.repository.DemandEventRepository;
import com.example.aipricingservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DemandEventListener {

    private final ProductRepository productRepository;
    private final DemandEventRepository demandEventRepository;

    @RabbitListener(queues = RabbitConfig.DEMAND_QUEUE)
    public void handleDemandEvent(Map<String, Object> message) {
        String eventType = String.valueOf(message.get("eventType"));

        if ("PRODUCT_SYNC".equals(eventType)) {
            handleProductSync(message);
            return;
        }

        Long productId = toLong(message.get("productId"));
        Long userId = message.get("userId") != null ? toLong(message.get("userId")) : 0L;
        int eventWeight = message.get("weight") != null ? toInt(message.get("weight")) : 1;

        log.info("📩 Event: {} for Product #{}", eventType, productId);

        // Save event for analytics/trending (no price changes here - monolith handles stock-based pricing)
        try {
            DemandEvent demandEvent = DemandEvent.builder()
                    .productId(productId)
                    .userId(userId)
                    .eventType(DemandEvent.EventType.valueOf(eventType))
                    .weight(eventWeight)
                    .createdAt(LocalDateTime.now())
                    .build();
            demandEventRepository.save(demandEvent);
            log.info("✅ Saved event: {} for product #{}", eventType, productId);
        } catch (Exception e) {
            log.error("Failed to save demand event: {}", e.getMessage());
        }
    }

    private void handleProductSync(Map<String, Object> message) {
        Long productId = toLong(message.get("productId"));
        String name = String.valueOf(message.get("name"));
        Double price = toDouble(message.get("price"));

        var existing = productRepository.findById(productId);

        if (existing.isPresent()) {
            Product product = existing.get();
            product.setName(name);
            product.setBasePrice(price);
            product.setCurrentPrice(price);
            productRepository.save(product);
            log.info("🔄 [SYNC] Updated product #{} ({}) price=${}", productId, name, price);
        } else {
            Product product = new Product();
            product.setId(productId);
            product.setName(name);
            product.setBasePrice(price);
            product.setCurrentPrice(price);
            product.setDemandLevel(DemandLevel.MEDIUM);
            product.setDemandScore(0);
            product.setLastPriceUpdate(LocalDateTime.now());
            productRepository.save(product);
            log.info("✅ [SYNC] Created product #{} ({}) price=${}", productId, name, price);
        }
    }

    private Long toLong(Object obj) {
        if (obj instanceof Number) return ((Number) obj).longValue();
        return Long.parseLong(String.valueOf(obj));
    }

    private int toInt(Object obj) {
        if (obj instanceof Number) return ((Number) obj).intValue();
        return Integer.parseInt(String.valueOf(obj));
    }

    private Double toDouble(Object obj) {
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        return Double.parseDouble(String.valueOf(obj));
    }
}