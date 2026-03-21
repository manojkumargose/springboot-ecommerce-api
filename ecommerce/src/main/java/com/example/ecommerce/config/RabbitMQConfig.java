package com.example.ecommerce.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration — defines exchange, queues, and routing.
 *
 * Architecture:
 *   Producer → TopicExchange → routing key → Queue → Consumer
 *
 * Exchange: ecommerce.exchange (topic type)
 *
 * Routing keys:
 *   order.placed       → order queue, email queue, demand queue
 *   order.cancelled    → order queue, email queue
 *   payment.completed  → payment queue, email queue
 *
 * 📁 Location: com.example.ecommerce.config
 * 📝 Action:   CREATE NEW FILE (or replace existing RabbitMQ config)
 */
@Configuration
public class RabbitMQConfig {

    // ─── Exchange ────────────────────────────────────────────

    public static final String EXCHANGE = "ecommerce.exchange";

    // ─── Queue Names ─────────────────────────────────────────

    public static final String ORDER_QUEUE = "ecommerce.order.queue";
    public static final String EMAIL_QUEUE = "ecommerce.email.queue";
    public static final String PAYMENT_QUEUE = "ecommerce.payment.queue";
    public static final String DEMAND_QUEUE = "ecommerce.demand.queue";
    public static final String INVOICE_QUEUE = "ecommerce.invoice.queue";

    // ─── Routing Keys ────────────────────────────────────────

    public static final String ORDER_PLACED_KEY = "order.placed";
    public static final String ORDER_CANCELLED_KEY = "order.cancelled";
    public static final String PAYMENT_COMPLETED_KEY = "payment.completed";

    // ─── Exchange Bean ───────────────────────────────────────

    @Bean
    public TopicExchange ecommerceExchange() {
        return new TopicExchange(EXCHANGE);
    }

    // ─── Queue Beans ─────────────────────────────────────────

    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable(ORDER_QUEUE).build();
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(EMAIL_QUEUE).build();
    }

    @Bean
    public Queue paymentQueue() {
        return QueueBuilder.durable(PAYMENT_QUEUE).build();
    }

    @Bean
    public Queue demandQueue() {
        return QueueBuilder.durable(DEMAND_QUEUE).build();
    }

    @Bean
    public Queue invoiceQueue() {
        return QueueBuilder.durable(INVOICE_QUEUE).build();
    }

    // ─── Bindings: order.placed → multiple queues ────────────

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

    // ─── Bindings: order.cancelled → queues ──────────────────

    @Bean
    public Binding orderCancelledToOrderQueue() {
        return BindingBuilder.bind(orderQueue()).to(ecommerceExchange()).with(ORDER_CANCELLED_KEY);
    }

    @Bean
    public Binding orderCancelledToEmailQueue() {
        return BindingBuilder.bind(emailQueue()).to(ecommerceExchange()).with(ORDER_CANCELLED_KEY);
    }

    // ─── Bindings: payment.completed → queues ────────────────

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

    // ─── JSON Message Converter (events sent as JSON) ────────

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