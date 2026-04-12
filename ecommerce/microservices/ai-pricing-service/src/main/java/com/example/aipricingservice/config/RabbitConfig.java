package com.example.aipricingservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String DEMAND_QUEUE = "demand-queue";
    public static final String PRICE_UPDATE_QUEUE = "price.update.queue";
    public static final String PRICE_UPDATE_EXCHANGE = "price.update.exchange";
    public static final String PRICE_UPDATE_ROUTING_KEY = "price.update.routingKey";

    @Bean
    public Queue demandQueue() {
        return new Queue(DEMAND_QUEUE, true);
    }

    @Bean
    public Queue priceUpdateQueue() {
        return new Queue(PRICE_UPDATE_QUEUE, true);
    }

    @Bean
    public TopicExchange priceExchange() {
        return new TopicExchange(PRICE_UPDATE_EXCHANGE);
    }

    @Bean
    public Binding binding(Queue priceUpdateQueue, TopicExchange priceExchange) {
        return BindingBuilder.bind(priceUpdateQueue)
                .to(priceExchange)
                .with(PRICE_UPDATE_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}