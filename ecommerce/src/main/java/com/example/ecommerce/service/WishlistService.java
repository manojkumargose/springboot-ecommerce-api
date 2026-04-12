package com.example.ecommerce.service;

import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.entity.Wishlist;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.repository.WishlistRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final EventPublisherService eventPublisherService; // NEW

    public WishlistService(WishlistRepository wishlistRepository,
                           ProductRepository productRepository,
                           UserRepository userRepository,
                           EventPublisherService eventPublisherService) { // NEW
        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.eventPublisherService = eventPublisherService; // NEW
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Wishlist addToWishlist(Long productId) {
        User user = getCurrentUser();
        if (wishlistRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new RuntimeException("Product already in wishlist");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setProduct(product);
        Wishlist saved = wishlistRepository.save(wishlist);

        // NEW: Track wishlist demand for AI pricing
        try {
            eventPublisherService.publishWishlistAdd(productId, user.getId());
        } catch (Exception ignored) {}

        return saved;
    }

    public void removeFromWishlist(Long productId) {
        User user = getCurrentUser();
        Wishlist wishlist = wishlistRepository
                .findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Product not in wishlist"));
        wishlistRepository.delete(wishlist);
    }

    public List<Wishlist> getMyWishlist() {
        User user = getCurrentUser();
        return wishlistRepository.findByUserId(user.getId());
    }
}