package com.example.coreservice.controller;

import com.example.coreservice.dto.ApiResponse;
import com.example.coreservice.entity.Cart;
import com.example.coreservice.security.JwtUtil;
import com.example.coreservice.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;
    private final JwtUtil jwtUtil;

    public CartController(CartService cartService, JwtUtil jwtUtil) {
        this.cartService = cartService; this.jwtUtil = jwtUtil;
    }

    private Long getUserId(HttpServletRequest req) {
        return jwtUtil.extractUserId(req.getHeader("Authorization").substring(7));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Cart>> getCart(HttpServletRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Cart fetched", cartService.getOrCreateCart(getUserId(req))));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<Cart>> addItem(@RequestParam Long productId,
                                                      @RequestParam(defaultValue = "1") Integer quantity,
                                                      HttpServletRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Item added", cartService.addToCart(getUserId(req), productId, quantity)));
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<Cart>> updateItem(@RequestParam Long productId,
                                                         @RequestParam Integer quantity,
                                                         HttpServletRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Cart updated", cartService.updateQuantity(getUserId(req), productId, quantity)));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<ApiResponse<Cart>> removeItem(@PathVariable Long productId, HttpServletRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Item removed", cartService.removeFromCart(getUserId(req), productId)));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart(HttpServletRequest req) {
        cartService.clearCart(getUserId(req));
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }
}
