package com.example.coreservice.service;

import com.example.coreservice.entity.Product;
import com.example.coreservice.entity.Wishlist;
import com.example.coreservice.messaging.DemandEventPublisher;
import com.example.coreservice.repository.ProductRepository;
import com.example.coreservice.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class WishlistService {
    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final DemandEventPublisher demandEventPublisher;

    public WishlistService(WishlistRepository wishlistRepository, ProductRepository productRepository,
                           DemandEventPublisher demandEventPublisher) {
        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
        this.demandEventPublisher = demandEventPublisher;
    }

    public Wishlist addToWishlist(Long userId, Long productId) {
        if (wishlistRepository.existsByUserIdAndProductId(userId, productId))
            throw new RuntimeException("Product already in wishlist");
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        Wishlist wishlist = new Wishlist();
        wishlist.setUserId(userId);
        wishlist.setProduct(product);
        Long categoryId = product.getCategory() != null ? product.getCategory().getId() : null;
        demandEventPublisher.publishDemandEvent(productId, "WISHLIST_ADD", userId, categoryId);
        return wishlistRepository.save(wishlist);
    }

    @Transactional(readOnly = true)
    public List<Wishlist> getWishlist(Long userId) {
        return wishlistRepository.findByUserId(userId);
    }

    public void removeFromWishlist(Long userId, Long productId) {
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
    }
}