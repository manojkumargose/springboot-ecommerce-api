package com.example.ecommerce.controller;

import com.example.ecommerce.dto.ApiResponse;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.service.CartService;
import com.example.ecommerce.service.DemandTrackingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;
    private final DemandTrackingService demandTrackingService;  // ← NEW

    public CartController(CartService cartService,
                          DemandTrackingService demandTrackingService) {  // ← CHANGED
        this.cartService = cartService;
        this.demandTrackingService = demandTrackingService;  // ← NEW
    }

    // ─── Get My Cart ──────────────────────────────────────────

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Cart>> getMyCart() {
        return ResponseEntity.ok(
                ApiResponse.success("Cart fetched", cartService.getMyCart()));
    }

    // ─── Add Item to Cart ─────────────────────────────────────

    @PostMapping("/add")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Cart>> addToCart(
            @RequestParam Long productId,
            @RequestParam int quantity) {

        Cart cart = cartService.addToCart(productId, quantity);

        // ─── NEW: Track cart-add event for demand-based pricing ───
        try {
            demandTrackingService.trackCartAdd(productId, null);
        } catch (Exception e) {
            // Don't let tracking failure break cart operation
        }
        // ─── END NEW ─────────────────────────────────────────────

        return ResponseEntity.ok(
                ApiResponse.success("Item added to cart", cart));
    }

    // ─── Remove Item from Cart ────────────────────────────────

    @DeleteMapping("/remove")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Cart>> removeFromCart(
            @RequestParam Long productId) {
        return ResponseEntity.ok(
                ApiResponse.success("Item removed from cart",
                        cartService.removeFromCart(productId)));
    }

    // ─── Update Item Quantity ─────────────────────────────────

    @PutMapping("/update")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Cart>> updateQuantity(
            @RequestParam Long productId,
            @RequestParam int quantity) {
        return ResponseEntity.ok(
                ApiResponse.success("Cart updated",
                        cartService.updateQuantity(productId, quantity)));
    }

    // ─── Clear My Cart ────────────────────────────────────────

    @DeleteMapping("/clear")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        cartService.clearCart();
        return ResponseEntity.ok(
                ApiResponse.success("Cart cleared", null));
    }
}