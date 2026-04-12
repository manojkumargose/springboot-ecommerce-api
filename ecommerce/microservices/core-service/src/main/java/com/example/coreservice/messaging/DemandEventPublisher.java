package com.example.coreservice.messaging;

import com.example.coreservice.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DemandEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishDemandEvent(Long productId, String eventType, Long quantity, Long referenceId) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", eventType);
            event.put("productId", productId);
            event.put("quantity", quantity);
            event.put("referenceId", referenceId); // userId or orderId
            event.put("timestamp", LocalDateTime.now().toString());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.DEMAND_EXCHANGE,
                    RabbitMQConfig.DEMAND_ROUTING_KEY,
                    event
            );
            log.info("📢 Published {} event for product: {}", eventType, productId);
        } catch (Exception e) {
            log.warn("❌ RabbitMQ Demand publishing failed: {}", e.getMessage());
        }
    }
}