package com.example.coreservice.service;

import com.example.coreservice.client.PricingClient;
import com.example.coreservice.entity.*;
import com.example.coreservice.messaging.DemandEventPublisher;
import com.example.coreservice.repository.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // ✅ Essential for Lazy Loading

import java.util.ArrayList;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final DemandEventPublisher demandEventPublisher;
    private final PricingClient pricingClient;

    public CartService(CartRepository cartRepository,
                       ProductRepository productRepository,
                       DemandEventPublisher demandEventPublisher,
                       PricingClient pricingClient) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.demandEventPublisher = demandEventPublisher;
        this.pricingClient = pricingClient;
    }

    @Transactional // ✅ Keeps the session open to load items
    public Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setItems(new ArrayList<>()); // ✅ Initialize list to prevent NullPointer
            return cartRepository.save(cart);
        });
    }

    @Transactional // ✅ Critical: Keeps the DB session open for the entire add process
    @CircuitBreaker(name = "pricingService", fallbackMethod = "pricingFallback")
    public Cart addToCart(Long userId, Long productId, Integer quantity) {
        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Call AI Service - If this fails, pricingFallback is triggered
        Double dynamicPrice = pricingClient.getDynamicPrice(productId, product.getBasePrice()).getData();

        return processAddToCart(cart, product, quantity, userId, dynamicPrice);
    }

    // ✅ Fixed Fallback: Uses the default product price if AI is down
    @Transactional
    public Cart pricingFallback(Long userId, Long productId, Integer quantity, Throwable t) {
        System.err.println("⚠️ AI Pricing Down. Using default price. Reason: " + t.getMessage());

        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Use the hardcoded/base price from the product entity
        return processAddToCart(cart, product, quantity, userId, product.getPrice());
    }

    private Cart processAddToCart(Cart cart, Product product, Integer quantity, Long userId, Double priceToApply) {
        // ✅ Safety: Ensure items list is not null
        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }

        cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .findFirst()
                .ifPresentOrElse(
                        item -> item.setQuantity(item.getQuantity() + quantity),
                        () -> {
                            CartItem item = new CartItem();
                            item.setCart(cart);
                            item.setProduct(product);
                            item.setQuantity(quantity);
                            // item.setPriceAtTimeOfAdding(priceToApply); // Best practice for AI pricing
                            cart.getItems().add(item);
                        }
                );

        Long categoryId = (product.getCategory() != null) ? product.getCategory().getId() : null;
        demandEventPublisher.publishDemandEvent(product.getId(), "CART_ADD", userId, categoryId);

        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeFromCart(Long userId, Long productId) {
        Cart cart = getOrCreateCart(userId);
        if (cart.getItems() != null) {
            cart.getItems().removeIf(i -> i.getProduct().getId().equals(productId));
        }
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart updateQuantity(Long userId, Long productId, Integer quantity) {
        Cart cart = getOrCreateCart(userId);
        if (quantity <= 0) return removeFromCart(userId, productId);

        cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresent(i -> i.setQuantity(quantity));
        return cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        if (cart.getItems() != null) {
            cart.getItems().clear();
        }
        cartRepository.save(cart);
    }
}