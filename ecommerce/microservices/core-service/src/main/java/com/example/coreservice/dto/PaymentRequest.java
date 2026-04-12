package com.example.coreservice.dto;
import jakarta.validation.constraints.NotBlank; import jakarta.validation.constraints.NotNull;
public class PaymentRequest {
    @NotNull private Long orderId;
    @NotBlank private String paymentMethod;
    private String transactionId;
    public Long getOrderId() { return orderId; } public void setOrderId(Long o) { this.orderId=o; }
    public String getPaymentMethod() { return paymentMethod; } public void setPaymentMethod(String p) { this.paymentMethod=p; }
    public String getTransactionId() { return transactionId; } public void setTransactionId(String t) { this.transactionId=t; }
}
