package com.example.ecommerce.listener;

import com.example.ecommerce.event.OrderPlacedEvent;
import com.example.ecommerce.event.OrderCancelledEvent;
import com.example.ecommerce.event.PaymentCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailEventListener {

    public void handleEmailEvent(Object event) {
        if (event instanceof OrderPlacedEvent e) {
            handleOrderPlaced(e);
        } else if (event instanceof OrderCancelledEvent e) {
            handleOrderCancelled(e);
        } else if (event instanceof PaymentCompletedEvent e) {
            handlePaymentCompleted(e);
        } else {
            log.warn("Unknown event type received in email queue: {}", event.getClass().getName());
        }
    }

    private void handleOrderPlaced(OrderPlacedEvent event) {
        // Your logic to send order placed email
        log.info("Processing order placed event");
    }

    private void handleOrderCancelled(OrderCancelledEvent event) {
        // Your logic to send order cancelled email
        log.info("Processing order cancelled event");
    }

    private void handlePaymentCompleted(PaymentCompletedEvent event) {
        // Your logic to send payment completed email
        log.info("Processing payment completed event");
    }
}