package com.example.ecommerce.listener;

import com.example.ecommerce.config.RabbitMQConfig;
import com.example.ecommerce.event.OrderCancelledEvent;
import com.example.ecommerce.event.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listens to order queue for order lifecycle events.
 *
 * 📁 Location: com.example.ecommerce.listener
 * 📝 Action:   CREATE NEW FILE
 */
@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
    public void handleOrderEvent(Object event) {
        if (event instanceof OrderPlacedEvent) {
            OrderPlacedEvent e = (OrderPlacedEvent) event;
            log.info("ORDER >> New order #{} placed by {} — {} items, total: {}",
                    e.getOrderId(), e.getUsername(), e.getItems().size(), e.getFinalAmount());
        } else if (event instanceof OrderCancelledEvent) {
            OrderCancelledEvent e = (OrderCancelledEvent) event;
            log.info("ORDER >> Order #{} cancelled by {} — reason: {}",
                    e.getOrderId(), e.getUsername(), e.getReason());
        }
    }
}