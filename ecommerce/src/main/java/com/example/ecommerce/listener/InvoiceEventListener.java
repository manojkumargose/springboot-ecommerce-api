package com.example.ecommerce.listener;

import com.example.ecommerce.config.RabbitMQConfig;
import com.example.ecommerce.event.OrderPlacedEvent;
import com.example.ecommerce.event.PaymentCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class InvoiceEventListener {

    private static final Logger log = LoggerFactory.getLogger(InvoiceEventListener.class);

    @RabbitListener(queues = RabbitMQConfig.INVOICE_QUEUE)
    public void handleInvoiceEvent(Object event) {

        if (event instanceof OrderPlacedEvent e) {
            log.info("INVOICE >> Generating invoice for order #{} — amount: {}",
                    e.getOrderId(), e.getFinalAmount());
            // TODO: Call InvoiceService.generateInvoice() here

        } else if (event instanceof PaymentCompletedEvent e) {
            log.info("INVOICE >> Payment confirmed for order — updating invoice status");
            // TODO: Update invoice with payment details
        }
    }
}