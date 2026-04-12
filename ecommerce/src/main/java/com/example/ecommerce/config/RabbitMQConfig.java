package com.example.ecommerce.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ─── Existing E-commerce Exchange ────────────────────────
    public static final String EXCHANGE = "ecommerce.exchange";

    // ─── AI Pricing Exchange ──────────────────────────────────
    public static final String PRICE_UPDATE_QUEUE = "price.update.queue";
    public static final String PRICE_UPDATE_EXCHANGE = "price.update.exchange";
    public static final String PRICE_UPDATE_ROUTING_KEY = "price.update.routingKey";

    // ─── AI Demand Queue (Monolith → AI Service) ─────────────
    public static final String AI_DEMAND_QUEUE = "demand-queue";

    // ─── Existing Queue Names ─────────────────────────────────
    public static final String ORDER_QUEUE = "ecommerce.order.queue";
    public static final String EMAIL_QUEUE = "ecommerce.email.queue";
    public static final String PAYMENT_QUEUE = "ecommerce.payment.queue";
    public static final String DEMAND_QUEUE = "ecommerce.demand.queue";
    public static final String INVOICE_QUEUE = "ecommerce.invoice.queue";

    // ─── Routing Keys ─────────────────────────────────────────
    public static final String ORDER_PLACED_KEY = "order.placed";
    public static final String ORDER_CANCELLED_KEY = "order.cancelled";
    public static final String PAYMENT_COMPLETED_KEY = "payment.completed";

    // ─── Exchange Beans ───────────────────────────────────────

    @Bean
    public TopicExchange ecommerceExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public TopicExchange priceExchange() {
        return new TopicExchange(PRICE_UPDATE_EXCHANGE);
    }

    // ─── Queue Beans ─────────────────────────────────────────

    @Bean
    public Queue priceUpdateQueue() {
        return QueueBuilder.durable(PRICE_UPDATE_QUEUE).build();
    }

    @Bean
    public Queue aiDemandQueue() {
        return QueueBuilder.durable(AI_DEMAND_QUEUE).build();
    }

    @Bean
    public Queue orderQueue() { return QueueBuilder.durable(ORDER_QUEUE).build(); }

    @Bean
    public Queue emailQueue() { return QueueBuilder.durable(EMAIL_QUEUE).build(); }

    @Bean
    public Queue paymentQueue() { return QueueBuilder.durable(PAYMENT_QUEUE).build(); }

    @Bean
    public Queue demandQueue() { return QueueBuilder.durable(DEMAND_QUEUE).build(); }

    @Bean
    public Queue invoiceQueue() { return QueueBuilder.durable(INVOICE_QUEUE).build(); }

    // ─── AI Pricing Binding ──────────────────────────────────

    @Bean
    public Binding priceUpdateBinding() {
        return BindingBuilder.bind(priceUpdateQueue())
                .to(priceExchange())
                .with(PRICE_UPDATE_ROUTING_KEY);
    }

    // ─── Existing Bindings ───────────────────────────────────

    @Bean
    public Binding orderPlacedToOrderQueue() {
        return BindingBuilder.bind(orderQueue()).to(ecommerceExchange()).with(ORDER_PLACED_KEY);
    }

    @Bean
    public Binding orderPlacedToEmailQueue() {
        return BindingBuilder.bind(emailQueue()).to(ecommerceExchange()).with(ORDER_PLACED_KEY);
    }

    @Bean
    public Binding orderPlacedToDemandQueue() {
        return BindingBuilder.bind(demandQueue()).to(ecommerceExchange()).with(ORDER_PLACED_KEY);
    }

    @Bean
    public Binding orderPlacedToInvoiceQueue() {
        return BindingBuilder.bind(invoiceQueue()).to(ecommerceExchange()).with(ORDER_PLACED_KEY);
    }

    @Bean
    public Binding orderCancelledToOrderQueue() {
        return BindingBuilder.bind(orderQueue()).to(ecommerceExchange()).with(ORDER_CANCELLED_KEY);
    }

    @Bean
    public Binding orderCancelledToEmailQueue() {
        return BindingBuilder.bind(emailQueue()).to(ecommerceExchange()).with(ORDER_CANCELLED_KEY);
    }

    @Bean
    public Binding paymentCompletedToPaymentQueue() {
        return BindingBuilder.bind(paymentQueue()).to(ecommerceExchange()).with(PAYMENT_COMPLETED_KEY);
    }

    @Bean
    public Binding paymentCompletedToEmailQueue() {
        return BindingBuilder.bind(emailQueue()).to(ecommerceExchange()).with(PAYMENT_COMPLETED_KEY);
    }

    @Bean
    public Binding paymentCompletedToInvoiceQueue() {
        return BindingBuilder.bind(invoiceQueue()).to(ecommerceExchange()).with(PAYMENT_COMPLETED_KEY);
    }

    // ─── Infrastructure ──────────────────────────────────────

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}