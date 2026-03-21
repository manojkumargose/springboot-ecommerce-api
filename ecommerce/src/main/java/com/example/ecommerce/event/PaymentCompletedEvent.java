package com.example.ecommerce.event;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Published when a payment is successfully processed.
 * Consumed by: EmailListener (sends payment confirmation), InvoiceListener (generates invoice).
 *
 * 📁 Location: com.example.ecommerce.event
 * 📝 Action:   CREATE NEW FILE
 */
public class PaymentCompletedEvent implements Serializable {

    private Long paymentId;
    private Long orderId;
    private Long userId;
    private String username;
    private String email;
    private Double amount;
    private String paymentMethod;
    private String transactionId;
    private String status;
    private LocalDateTime timestamp;

    public PaymentCompletedEvent() {
        this.timestamp = LocalDateTime.now();
    }

    // ─── Getters & Setters ───────────────────────────────────

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}