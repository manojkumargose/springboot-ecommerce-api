package com.example.coreservice.dto;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
public class OrderRequest {
    private List<OrderItemRequest> items;
    private String couponCode;
    @NotBlank private String shippingStreet;
    @NotBlank private String shippingCity;
    @NotBlank private String shippingState;
    @NotBlank private String shippingPincode;
    public List<OrderItemRequest> getItems() { return items; } public void setItems(List<OrderItemRequest> i) { this.items=i; }
    public String getCouponCode() { return couponCode; } public void setCouponCode(String c) { this.couponCode=c; }
    public String getShippingStreet() { return shippingStreet; } public void setShippingStreet(String s) { this.shippingStreet=s; }
    public String getShippingCity() { return shippingCity; } public void setShippingCity(String c) { this.shippingCity=c; }
    public String getShippingState() { return shippingState; } public void setShippingState(String s) { this.shippingState=s; }
    public String getShippingPincode() { return shippingPincode; } public void setShippingPincode(String p) { this.shippingPincode=p; }

    public static class OrderItemRequest {
        private Long productId; private Integer quantity;
        public Long getProductId() { return productId; } public void setProductId(Long p) { this.productId=p; }
        public Integer getQuantity() { return quantity; } public void setQuantity(Integer q) { this.quantity=q; }
    }
}
