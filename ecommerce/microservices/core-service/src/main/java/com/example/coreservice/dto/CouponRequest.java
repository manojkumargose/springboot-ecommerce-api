package com.example.coreservice.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
public class CouponRequest {
    @NotBlank private String code;
    @Positive private Double discountPercent;
    @Positive private Double maxDiscountAmount;
    @Positive private Double minOrderAmount;
    private LocalDateTime expiryDate;
    private Integer usageLimit;
    public String getCode() { return code; } public void setCode(String c) { this.code=c; }
    public Double getDiscountPercent() { return discountPercent; } public void setDiscountPercent(Double d) { this.discountPercent=d; }
    public Double getMaxDiscountAmount() { return maxDiscountAmount; } public void setMaxDiscountAmount(Double m) { this.maxDiscountAmount=m; }
    public Double getMinOrderAmount() { return minOrderAmount; } public void setMinOrderAmount(Double m) { this.minOrderAmount=m; }
    public LocalDateTime getExpiryDate() { return expiryDate; } public void setExpiryDate(LocalDateTime e) { this.expiryDate=e; }
    public Integer getUsageLimit() { return usageLimit; } public void setUsageLimit(Integer u) { this.usageLimit=u; }
}
