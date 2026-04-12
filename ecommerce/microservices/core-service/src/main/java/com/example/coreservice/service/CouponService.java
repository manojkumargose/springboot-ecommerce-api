package com.example.coreservice.service;

import com.example.coreservice.dto.CouponRequest;
import com.example.coreservice.entity.Coupon;
import com.example.coreservice.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CouponService {
    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    public Coupon createCoupon(CouponRequest req) {
        if (couponRepository.findByCode(req.getCode()).isPresent())
            throw new RuntimeException("Coupon code already exists");
        Coupon coupon = new Coupon();
        coupon.setCode(req.getCode());
        coupon.setDiscountPercent(req.getDiscountPercent());
        coupon.setMaxDiscountAmount(req.getMaxDiscountAmount());
        coupon.setMinOrderAmount(req.getMinOrderAmount());
        coupon.setExpiryDate(req.getExpiryDate());
        coupon.setUsageLimit(req.getUsageLimit());
        return couponRepository.save(coupon);
    }

    @Transactional(readOnly = true)
    public Coupon validateCoupon(String code, Double orderAmount) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
        if (!coupon.getIsActive()) throw new RuntimeException("Coupon is inactive");
        if (orderAmount < coupon.getMinOrderAmount())
            throw new RuntimeException("Minimum order amount not met");
        return coupon;
    }

    @Transactional(readOnly = true)
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    public void deactivateCoupon(Long id) {
        Coupon c = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
        c.setIsActive(false);
        couponRepository.save(c);
    }
}