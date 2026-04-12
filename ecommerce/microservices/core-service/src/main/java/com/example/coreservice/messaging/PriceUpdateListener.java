package com.example.coreservice.messaging;

import com.example.coreservice.config.RabbitMQConfig;
import com.example.coreservice.dto.PriceUpdateMessage; // FIXED: Use local DTO, not the AI service one
import com.example.coreservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PriceUpdateListener {

    private final ProductRepository productRepository;

    @RabbitListener(queues = RabbitMQConfig.PRICE_UPDATE_QUEUE)
    public void handlePriceUpdate(PriceUpdateMessage message) {
        log.info("Received price update for product {}: new price = {}, reason = {}",
                message.getProductId(), message.getNewPrice(), message.getChangeReason());

        productRepository.findById(message.getProductId()).ifPresentOrElse(
                product -> {
                    product.setPrice(message.getNewPrice());
                    productRepository.save(product);
                    log.info("Updated product {} price to {}", message.getProductId(), message.getNewPrice());
                },
                () -> log.warn("Product not found with id: {}", message.getProductId())
        );
    }
}