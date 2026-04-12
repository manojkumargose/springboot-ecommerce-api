package com.example.coreservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;            // Matches .id() in Service mapping
    private Long userId;        // Matches the Long type used in Controllers and Service
    private List<OrderItemResponse> items;
    private Double totalAmount;
    private Double discountAmount;
    private Double finalAmount;
    private String status;

    // 🚀 CRITICAL: This is used to pass error details when a Circuit Breaker trips
    private String message;

    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Long productId; // Matches .productId() in Service mapping
        private String productName;
        private Integer quantity;
        private Double price;
    }
}