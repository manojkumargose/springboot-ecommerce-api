package com.example.ecommerce.listener;

import com.example.ecommerce.config.RabbitMQConfig;
import com.example.ecommerce.event.PaymentCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_QUEUE)
    public void handlePaymentEvent(Object event) throws InterruptedException {

        // The ghost "listener." prefix is gone, and pattern matching handles the cast
        if (event instanceof PaymentCompletedEvent e) {

            log.info("Payment confirmed and processing for order #{}", e.getOrderId());

            // TODO: Add your logic here to update the database or trigger shipping

        } else {
            log.warn("Received unknown event in payment queue");
        }
    }
}