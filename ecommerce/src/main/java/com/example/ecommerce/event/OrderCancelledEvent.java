package com.example.ecommerce.event;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Published when a customer or admin cancels an order.
 * Consumed by: EmailListener (sends cancellation email), StockListener (restores stock).
 *
 * 📁 Location: com.example.ecommerce.event
 * 📝 Action:   CREATE NEW FILE
 */
public class OrderCancelledEvent implements Serializable {

    private Long orderId;
    private Long userId;
    private String username;
    private String email;
    private List<com.example.ecommerce.event.OrderPlacedEvent.OrderItemInfo> items;
    private String reason;
    private LocalDateTime timestamp;

    public OrderCancelledEvent() {
        this.timestamp = LocalDateTime.now();
    }

    // ─── Getters & Setters ───────────────────────────────────

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public List<com.example.ecommerce.event.OrderPlacedEvent.OrderItemInfo> getItems() { return items; }
    public void setItems(List<com.example.ecommerce.event.OrderPlacedEvent.OrderItemInfo> items) { this.items = items; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public class OrderItemInfo {
        public Long getProductId() {
            return 0L;
        }
    }
}