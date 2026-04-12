package com.example.coreservice.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data // ✅ Generates Getters, Setters, toString, and equals/hashCode
@AllArgsConstructor // ✅ Generates the constructor with all 4 fields
@NoArgsConstructor // ✅ Generates the empty constructor required for JSON
public class DemandEventMessage implements Serializable {

    private Long productId;
    private String eventType; // VIEW, CART_ADD, WISHLIST_ADD, PURCHASE
    private Long userId;
    private Long categoryId; // 🎯 This matches the 4th parameter in your OrderService
}