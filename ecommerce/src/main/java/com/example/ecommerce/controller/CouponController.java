package com.example.ecommerce.controller;

import com.example.ecommerce.dto.ApiResponse;
import com.example.ecommerce.dto.CouponRequest;
import com.example.ecommerce.dto.CouponResponse;
import com.example.ecommerce.service.CouponService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    // ─── Create Coupon (Admin) ────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(
            @Valid @RequestBody CouponRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Coupon created successfully",
                couponService.createCoupon(request)));
    }

    // ─── Get All Coupons (Admin) ──────────────────────────────

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getAllCoupons() {
        return ResponseEntity.ok(ApiResponse.success(
                "Coupons fetched",
                couponService.getAllCoupons()));
    }

    // ─── Deactivate Coupon (Admin) ────────────────────────────

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CouponResponse>> deactivateCoupon(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Coupon deactivated",
                couponService.deactivateCoupon(id)));
    }

    // ─── Delete Coupon (Admin) ────────────────────────────────

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(
            @PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Coupon deleted", null));
    }
}