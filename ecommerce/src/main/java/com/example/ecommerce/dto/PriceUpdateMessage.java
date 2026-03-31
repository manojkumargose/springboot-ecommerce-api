package com.example.ecommerce.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceUpdateMessage {
    private Long productId;
    private Double newPrice;
    private Double demandScore;
    private String changeReason;
}