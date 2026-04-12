package com.example.coreservice.controller;

import com.example.coreservice.dto.ApiResponse;
import com.example.coreservice.entity.Wishlist;
import com.example.coreservice.security.JwtUtil;
import com.example.coreservice.service.WishlistService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {
    private final WishlistService wishlistService;
    private final JwtUtil jwtUtil;

    public WishlistController(WishlistService wishlistService, JwtUtil jwtUtil) {
        this.wishlistService = wishlistService; this.jwtUtil = jwtUtil;
    }

    private Long getUserId(HttpServletRequest req) {
        return jwtUtil.extractUserId(req.getHeader("Authorization").substring(7));
    }

    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse<Wishlist>> add(@PathVariable Long productId, HttpServletRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Added to wishlist", wishlistService.addToWishlist(getUserId(req), productId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Wishlist>>> get(HttpServletRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Wishlist fetched", wishlistService.getWishlist(getUserId(req))));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> remove(@PathVariable Long productId, HttpServletRequest req) {
        wishlistService.removeFromWishlist(getUserId(req), productId);
        return ResponseEntity.ok(ApiResponse.success("Removed from wishlist", null));
    }
}
