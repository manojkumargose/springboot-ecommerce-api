package com.example.ecommerce.service;

import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.CartItemRepository;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final EventPublisherService eventPublisherService; // NEW

    public CartService(CartRepository cartRepository,
                       ProductRepository productRepository,
                       CartItemRepository cartItemRepository,
                       UserRepository userRepository,
                       EventPublisherService eventPublisherService) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.eventPublisherService = eventPublisherService;
    }

    private Long getLoggedInUserId() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }

    public Cart getMyCart() {
        Long userId = getLoggedInUserId();
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            cart = new Cart();
            cart.setUserId(userId);
            cart.setItems(new ArrayList<>());
            cartRepository.save(cart);
        }
        return cart;
    }

    @Transactional
    public Cart addToCart(Long productId, int quantity) {
        Long userId = getLoggedInUserId();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found: " + productId));

        if (!product.getInStock()) {
            throw new RuntimeException(
                    "Product is out of stock: " + product.getName());
        }

        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException(
                    "Insufficient stock for: " + product.getName()
                            + ". Available: " + product.getStockQuantity()
                            + ", Requested: " + quantity);
        }

        Cart cart = getMyCart();

        for (CartItem existingItem : cart.getItems()) {
            if (existingItem.getProductId().equals(productId)) {
                int newQuantity = existingItem.getQuantity() + quantity;
                if (product.getStockQuantity() < newQuantity) {
                    throw new RuntimeException(
                            "Cannot add more. Available stock: "
                                    + product.getStockQuantity()
                                    + ", Already in cart: " + existingItem.getQuantity());
                }
                existingItem.setQuantity(newQuantity);
                Cart saved = cartRepository.save(cart);

                // NEW: Track add-to-cart demand for AI pricing
                eventPublisherService.publishAddToCart(productId, userId);

                return saved;
            }
        }

        CartItem item = new CartItem();
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setCart(cart);
        cart.getItems().add(item);
        Cart saved = cartRepository.save(cart);

        // NEW: Track add-to-cart demand for AI pricing
        eventPublisherService.publishAddToCart(productId, userId);

        return saved;
    }

    @Transactional
    public Cart removeFromCart(Long productId) {
        Long userId = getLoggedInUserId();
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) throw new ResourceNotFoundException("Cart not found");
        cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart updateQuantity(Long productId, int quantity) {
        Long userId = getLoggedInUserId();
        if (quantity <= 0) return removeFromCart(productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found: " + productId));
        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException(
                    "Insufficient stock. Available: " + product.getStockQuantity());
        }

        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) throw new ResourceNotFoundException("Cart not found");

        for (CartItem item : cart.getItems()) {
            if (item.getProductId().equals(productId)) {
                item.setQuantity(quantity);
                return cartRepository.save(cart);
            }
        }
        throw new ResourceNotFoundException("Item not found in cart");
    }

    @Transactional
    public void clearCart() {
        Long userId = getLoggedInUserId();
        Cart cart = cartRepository.findByUserId(userId);
        if (cart != null) {
            cart.getItems().clear();
            cartRepository.save(cart);
        }
    }
}