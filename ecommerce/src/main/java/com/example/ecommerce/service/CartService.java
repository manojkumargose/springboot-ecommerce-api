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

    public CartService(CartRepository cartRepository,
                       ProductRepository productRepository,
                       CartItemRepository cartItemRepository,
                       UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
    }

    // ─── Get Logged In User ID from JWT ───────────────────────

    private Long getLoggedInUserId() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }

    // ─── Get or Create Cart ───────────────────────────────────

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

    // ─── Add Item to Cart ─────────────────────────────────────

    @Transactional
    public Cart addToCart(Long productId, int quantity) {
        Long userId = getLoggedInUserId();

        // ── Validate product exists ───────────────────────────
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found: " + productId));

        // ── Check product is in stock ─────────────────────────
        if (!product.getInStock()) {
            throw new RuntimeException(
                    "Product is out of stock: " + product.getName());
        }

        // ── Check requested quantity is available ─────────────
        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException(
                    "Insufficient stock for: " + product.getName()
                            + ". Available: " + product.getStockQuantity()
                            + ", Requested: " + quantity);
        }

        Cart cart = getMyCart();

        // ── If product already in cart, increase quantity ──────
        for (CartItem existingItem : cart.getItems()) {
            if (existingItem.getProductId().equals(productId)) {
                int newQuantity = existingItem.getQuantity() + quantity;

                // Check combined quantity doesn't exceed stock
                if (product.getStockQuantity() < newQuantity) {
                    throw new RuntimeException(
                            "Cannot add more. Available stock: "
                                    + product.getStockQuantity()
                                    + ", Already in cart: " + existingItem.getQuantity());
                }

                existingItem.setQuantity(newQuantity);
                return cartRepository.save(cart);
            }
        }

        // ── Otherwise add as new cart item ────────────────────
        CartItem item = new CartItem();
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setCart(cart);
        cart.getItems().add(item);

        return cartRepository.save(cart);
    }

    // ─── Remove Item from Cart ────────────────────────────────

    @Transactional
    public Cart removeFromCart(Long productId) {
        Long userId = getLoggedInUserId();
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            throw new ResourceNotFoundException("Cart not found");
        }
        cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        return cartRepository.save(cart);
    }

    // ─── Update Item Quantity ─────────────────────────────────

    @Transactional
    public Cart updateQuantity(Long productId, int quantity) {
        Long userId = getLoggedInUserId();

        if (quantity <= 0) {
            return removeFromCart(productId);
        }

        // ── Check stock for new quantity ───────────────────────
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found: " + productId));

        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException(
                    "Insufficient stock. Available: " + product.getStockQuantity());
        }

        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            throw new ResourceNotFoundException("Cart not found");
        }

        for (CartItem item : cart.getItems()) {
            if (item.getProductId().equals(productId)) {
                item.setQuantity(quantity);
                return cartRepository.save(cart);
            }
        }

        throw new ResourceNotFoundException("Item not found in cart");
    }

    // ─── Clear Cart ───────────────────────────────────────────

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