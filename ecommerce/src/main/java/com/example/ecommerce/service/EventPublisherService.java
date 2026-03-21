package com.example.ecommerce.service;

import com.example.ecommerce.config.RabbitMQConfig;
import com.example.ecommerce.event.OrderCancelledEvent;
import com.example.ecommerce.event.OrderPlacedEvent;
import com.example.ecommerce.event.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisherService {

    private final RabbitTemplate rabbitTemplate;

    public void publishOrderPlaced(OrderPlacedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    "order_placed_routing_key", // Fallback if RabbitMQConfig.ORDER_PLACED_KEY is missing
                    event
            );
            // Notice this is now .getOrderId()
            log.info("Published OrderPlacedEvent for order #{}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to publish OrderPlacedEvent: {}", e.getMessage());
        }
    }

    public void publishOrderCancelled(OrderCancelledEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    "order_cancelled_routing_key", // Fallback
                    event
            );
            // Notice this is now .getOrderId()
            log.info("Published OrderCancelledEvent for order #{}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to publish OrderCancelledEvent: {}", e.getMessage());
        }
    }

    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    "payment_completed_routing_key", // Fallback
                    event
            );
            // Notice this is now .getOrderId()
            log.info("Published PaymentCompletedEvent for order #{}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to publish PaymentCompletedEvent: {}", e.getMessage());
        }
    }
}