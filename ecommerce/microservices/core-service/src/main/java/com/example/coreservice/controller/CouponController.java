package com.example.coreservice.controller;

import com.example.coreservice.dto.*;
import com.example.coreservice.entity.Coupon;
import com.example.coreservice.service.CouponService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {
    private final CouponService couponService;
    public CouponController(CouponService couponService) { this.couponService = couponService; }

    @PostMapping
    public ResponseEntity<ApiResponse<Coupon>> create(@Valid @RequestBody CouponRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Coupon created", couponService.createCoupon(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Coupon>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Coupons fetched", couponService.getAllCoupons()));
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Coupon>> validate(@RequestParam String code, @RequestParam Double amount) {
        return ResponseEntity.ok(ApiResponse.success("Coupon valid", couponService.validateCoupon(code, amount)));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        couponService.deactivateCoupon(id);
        return ResponseEntity.ok(ApiResponse.success("Coupon deactivated", null));
    }
}
