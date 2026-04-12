package com.example.aipricingservice.messaging;

import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Demand Event Constants (For the Publisher)
    public static final String DEMAND_EXCHANGE = "demand.exchange";
    public static final String DEMAND_QUEUE = "demand.queue";

    // ✅ Price Update Constants (For the Listener)
    // This must match the name in your RabbitMQ screenshot!
    public static final String PRICE_UPDATE_QUEUE = "price.updates.queue";
}