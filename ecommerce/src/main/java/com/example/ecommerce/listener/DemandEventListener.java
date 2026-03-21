package com.example.ecommerce.listener;

import com.example.ecommerce.config.RabbitMQConfig;
import com.example.ecommerce.event.OrderPlacedEvent;
import com.example.ecommerce.service.DemandTrackingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listens to demand queue — tracks PURCHASE events for dynamic pricing.
 * This replaces the direct call in OrderService for purchase tracking.
 *
 * 📁 Location: com.example.ecommerce.listener
 * 📝 Action:   CREATE NEW FILE
 */
@Component
public class DemandEventListener {

    private static final Logger log = LoggerFactory.getLogger(DemandEventListener.class);

    private final DemandTrackingService demandTrackingService;

    public DemandEventListener(DemandTrackingService demandTrackingService) {
        this.demandTrackingService = demandTrackingService;
    }

    @RabbitListener(queues = RabbitMQConfig.DEMAND_QUEUE)
    public void handleDemandEvent(Object event) {
        if (event instanceof OrderPlacedEvent) {
            OrderPlacedEvent e = (OrderPlacedEvent) event;
            log.info("DEMAND >> Tracking purchases from order #{}", e.getOrderId());

            for (OrderPlacedEvent.OrderItemInfo item : e.getItems()) {
                try {
                    demandTrackingService.trackPurchase(item.getProductId(), e.getUserId());
                    log.debug("DEMAND >> Tracked purchase for product #{}", item.getProductId());
                } catch (Exception ex) {
                    log.error("DEMAND >> Failed to track purchase for product #{}: {}",
                            item.getProductId(), ex.getMessage());
                }
            }
        }
    }
}