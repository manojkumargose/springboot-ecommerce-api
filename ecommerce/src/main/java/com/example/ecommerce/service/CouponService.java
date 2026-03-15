package com.example.ecommerce.service;

import com.example.ecommerce.dto.CouponRequest;
import com.example.ecommerce.dto.CouponResponse;
import com.example.ecommerce.entity.Coupon;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.CouponRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    // ─── Create Coupon (Admin) ────────────────────────────────

    public CouponResponse createCoupon(CouponRequest request) {
        if (couponRepository.existsByCode(request.getCode().toUpperCase())) {
            throw new RuntimeException("Coupon code already exists: " + request.getCode());
        }

        if (!request.getDiscountType().equals("PERCENTAGE")
                && !request.getDiscountType().equals("FLAT")) {
            throw new RuntimeException("Discount type must be PERCENTAGE or FLAT");
        }

        if (request.getDiscountType().equals("PERCENTAGE")
                && request.getDiscountValue() > 100) {
            throw new RuntimeException("Percentage discount cannot exceed 100%");
        }

        Coupon coupon = new Coupon();
        coupon.setCode(request.getCode().toUpperCase());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMinimumOrderAmount(request.getMinimumOrderAmount());
        coupon.setUsageLimit(request.getUsageLimit());
        coupon.setExpiryDate(request.getExpiryDate());
        coupon.setActive(true);
        coupon.setUsedCount(0);

        return mapToResponse(couponRepository.save(coupon));
    }

    // ─── Get All Coupons (Admin) ──────────────────────────────

    public List<CouponResponse> getAllCoupons() {
        return couponRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Validate & Apply Coupon ──────────────────────────────

    public Coupon validateCoupon(String code, double orderAmount) {
        Coupon coupon = couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new RuntimeException(
                        "Invalid coupon code: " + code));

        if (!coupon.getActive()) {
            throw new RuntimeException("Coupon is inactive: " + code);
        }

        if (coupon.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Coupon has expired: " + code);
        }

        if (coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new RuntimeException("Coupon usage limit reached: " + code);
        }

        if (orderAmount < coupon.getMinimumOrderAmount()) {
            throw new RuntimeException(
                    "Order amount must be at least ₹"
                            + coupon.getMinimumOrderAmount()
                            + " to use this coupon");
        }

        return coupon;
    }

    // ─── Calculate Discount ───────────────────────────────────

    public double calculateDiscount(Coupon coupon, double orderAmount) {
        if (coupon.getDiscountType().equals("PERCENTAGE")) {
            return Math.round((orderAmount * coupon.getDiscountValue() / 100) * 100.0) / 100.0;
        } else {
            return Math.min(coupon.getDiscountValue(), orderAmount);
        }
    }

    // ─── Increment Usage Count ────────────────────────────────

    public void incrementUsage(Coupon coupon) {
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);
    }

    // ─── Deactivate Coupon (Admin) ────────────────────────────

    public CouponResponse deactivateCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
        coupon.setActive(false);
        return mapToResponse(couponRepository.save(coupon));
    }

    // ─── Delete Coupon (Admin) ────────────────────────────────

    public void deleteCoupon(Long id) {
        if (!couponRepository.existsById(id)) {
            throw new ResourceNotFoundException("Coupon not found");
        }
        couponRepository.deleteById(id);
    }

    // ─── Map to Response ──────────────────────────────────────

    private CouponResponse mapToResponse(Coupon coupon) {
        CouponResponse response = new CouponResponse();
        response.setId(coupon.getId());
        response.setCode(coupon.getCode());
        response.setDiscountType(coupon.getDiscountType());
        response.setDiscountValue(coupon.getDiscountValue());
        response.setMinimumOrderAmount(coupon.getMinimumOrderAmount());
        response.setUsageLimit(coupon.getUsageLimit());
        response.setUsedCount(coupon.getUsedCount());
        response.setExpiryDate(coupon.getExpiryDate());
        response.setActive(coupon.getActive());
        return response;
    }
}