package com.example.coreservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ── 1. PRICE UPDATES (AI -> CORE) ──
    public static final String PRICE_UPDATE_QUEUE = "price.update.queue";
    public static final String PRICE_UPDATE_EXCHANGE = "price.update.exchange";
    public static final String PRICE_UPDATE_ROUTING_KEY = "price.update.routingKey";

    // ── 2. DEMAND TRACKING (CORE -> AI) ──
    public static final String DEMAND_EXCHANGE = "demand.exchange";
    public static final String DEMAND_TRACKING_QUEUE = "demand.tracking.queue";
    public static final String DEMAND_ROUTING_KEY = "demand.tracking.routingKey";

    // --- Beans for Price Updates (The "Ear") ---
    @Bean
    public Queue priceUpdateQueue() {
        return new Queue(PRICE_UPDATE_QUEUE, true); // true = durable (survives restart)
    }

    @Bean
    public TopicExchange priceExchange() {
        return new TopicExchange(PRICE_UPDATE_EXCHANGE);
    }

    @Bean
    public Binding priceUpdateBinding(Queue priceUpdateQueue, TopicExchange priceExchange) {
        return BindingBuilder.bind(priceUpdateQueue)
                .to(priceExchange)
                .with(PRICE_UPDATE_ROUTING_KEY);
    }

    // --- Beans for Demand Tracking (The "Voice") ---
    @Bean
    public Queue demandTrackingQueue() {
        return new Queue(DEMAND_TRACKING_QUEUE, true);
    }

    @Bean
    public TopicExchange demandExchange() {
        return new TopicExchange(DEMAND_EXCHANGE);
    }

    @Bean
    public Binding demandTrackingBinding(Queue demandTrackingQueue, TopicExchange demandExchange) {
        return BindingBuilder.bind(demandTrackingQueue)
                .to(demandExchange)
                .with(DEMAND_ROUTING_KEY);
    }

    // --- Global Configuration (The "Translator") ---

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        // 🛡️ CRITICAL: This allows the Core Service to read AI Service classes
        // even if they are in different packages.
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}