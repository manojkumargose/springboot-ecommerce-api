package com.example.ecommerce.listener;

import com.example.ecommerce.dto.PriceUpdateMessage;
import com.example.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PriceUpdateListener {

    private final ProductRepository productRepository;

    @RabbitListener(queues = "price.update.queue")
    @Transactional
    public void onPriceUpdate(PriceUpdateMessage message) {
        log.info("📢 [MONOLITH] AI suggested new price for Product ID {}: ${}",
                message.getProductId(), message.getNewPrice());

        productRepository.findById(message.getProductId()).ifPresentOrElse(product -> {
            product.setPrice(message.getNewPrice());
            productRepository.save(product);

            // Evict Redis cache so next fetch shows new price
            evictProductCache(message.getProductId());

            log.info("✅ [MONOLITH] Product '{}' price updated successfully!", product.getName());
        }, () -> {
            log.error("❌ [MONOLITH] Price update failed: Product ID {} not found", message.getProductId());
        });
    }

    @Caching(evict = {
            @CacheEvict(value = "product", key = "#productId"),
            @CacheEvict(value = "products", allEntries = true)
    })
    public void evictProductCache(Long productId) {
        log.debug("Cache evicted for product {}", productId);
    }
}