package com.example.aipricingservice.service;

import com.example.aipricingservice.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceUpdatePublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishPriceUpdate(Long productId, Double newPrice, String reason) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("productId", productId);
            message.put("newPrice", newPrice);
            message.put("changeReason", reason);

            rabbitTemplate.convertAndSend(
                    RabbitConfig.PRICE_UPDATE_EXCHANGE,
                    RabbitConfig.PRICE_UPDATE_ROUTING_KEY,
                    message
            );
            log.info("Published price update: product={}, newPrice={}", productId, newPrice);
        } catch (Exception e) {
            log.error("Failed to publish price update for product {}: {}", productId, e.getMessage());
        }
    }
}