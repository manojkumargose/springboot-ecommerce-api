package com.example.ecommerce.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedEvent implements Serializable {

    private Long orderId;
    private Long userId;
    private String username;
    private String email;
    private List<OrderItemInfo> items;
    private Double totalAmount;
    private Double finalAmount;
    private String couponCode;

    // Initializes automatically when the event is created
    private LocalDateTime timestamp = LocalDateTime.now();

    // ─── Inner class for order items ─────────────────────────

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemInfo implements Serializable {
        private Long productId;
        private String productName;
        private Integer quantity;
        private Double price;
    }
}