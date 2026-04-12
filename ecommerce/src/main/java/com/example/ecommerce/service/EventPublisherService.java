package com.example.ecommerce.service;

import com.example.ecommerce.config.RabbitMQConfig;
import com.example.ecommerce.event.OrderCancelledEvent;
import com.example.ecommerce.event.OrderPlacedEvent;
import com.example.ecommerce.event.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisherService {

    private final RabbitTemplate rabbitTemplate;

    public void publishOrderPlaced(OrderPlacedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ORDER_PLACED_KEY,
                    event
            );
            log.info("Published OrderPlacedEvent for order #{}", event.getOrderId());
            sendDemandEventsToAI(event);
        } catch (Exception e) {
            log.error("Failed to publish OrderPlacedEvent: {}", e.getMessage());
        }
    }

    public void publishOrderCancelled(OrderCancelledEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ORDER_CANCELLED_KEY,
                    event
            );
            log.info("Published OrderCancelledEvent for order #{}", event.getOrderId());
            for (OrderPlacedEvent.OrderItemInfo item : event.getItems()) {
                sendToAIDemandQueue(item.getProductId(), event.getUserId(), "CANCELLATION", 1);
            }
        } catch (Exception e) {
            log.error("Failed to publish OrderCancelledEvent: {}", e.getMessage());
        }
    }

    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.PAYMENT_COMPLETED_KEY,
                    event
            );
            log.info("Published PaymentCompletedEvent for order #{}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to publish PaymentCompletedEvent: {}", e.getMessage());
        }
    }

    private void sendDemandEventsToAI(OrderPlacedEvent event) {
        for (OrderPlacedEvent.OrderItemInfo item : event.getItems()) {
            sendToAIDemandQueue(item.getProductId(), event.getUserId(), "PURCHASE", item.getQuantity());
        }
    }

    public void publishProductView(Long productId, Long userId) {
        sendToAIDemandQueue(productId, userId, "VIEW", 1);
    }

    public void publishAddToCart(Long productId, Long userId) {
        sendToAIDemandQueue(productId, userId, "CART_ADD", 1);
    }

    public void publishWishlistAdd(Long productId, Long userId) {
        sendToAIDemandQueue(productId, userId, "WISHLIST_ADD", 1);
    }

    // NEW: Auto-sync product to AI pricing service
    public void publishProductSync(Long productId, String name, Double price) {
        try {
            Map<String, Object> syncEvent = new HashMap<>();
            syncEvent.put("productId", productId);
            syncEvent.put("name", name);
            syncEvent.put("price", price);
            syncEvent.put("eventType", "PRODUCT_SYNC");

            rabbitTemplate.convertAndSend(RabbitMQConfig.AI_DEMAND_QUEUE, syncEvent);
            log.info("Sent PRODUCT_SYNC to AI for product #{} ({})", productId, name);
        } catch (Exception e) {
            log.error("Failed to send PRODUCT_SYNC for product #{}: {}", productId, e.getMessage());
        }
    }

    private void sendToAIDemandQueue(Long productId, Long userId, String eventType, int quantity) {
        try {
            Map<String, Object> demandEvent = new HashMap<>();
            demandEvent.put("productId", productId);
            demandEvent.put("userId", userId);
            demandEvent.put("eventType", eventType);
            demandEvent.put("weight", quantity);

            rabbitTemplate.convertAndSend(RabbitMQConfig.AI_DEMAND_QUEUE, demandEvent);
            log.info("Sent {} demand event to AI for product #{}", eventType, productId);
        } catch (Exception e) {
            log.error("Failed to send {} demand event for product #{}: {}",
                    eventType, productId, e.getMessage());
        }
    }
}