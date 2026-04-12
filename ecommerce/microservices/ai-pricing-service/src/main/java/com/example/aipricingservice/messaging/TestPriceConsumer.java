package com.example.aipricingservice.messaging;

import com.example.aipricingservice.dto.PriceUpdateMessage; // 👈 Ensure this matches the DTO's package
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class TestPriceConsumer {

    @RabbitListener(queues = "price.update.queue") // 👈 Double check this matches RabbitMQConfig
    public void handleMessage(PriceUpdateMessage message) {
        if (message != null) {
            System.out.println("📬 [CONSUMER-TEST] I caught a message from RabbitMQ!");
            System.out.println("📦 Product ID: " + message.getProductId());
            System.out.println("💰 New Price: $" + message.getNewPrice());
            System.out.println("📝 Reason: " + message.getChangeReason());
        }
    }
}