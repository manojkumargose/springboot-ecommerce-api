package com.example.coreservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // 🚀 CRITICAL: Prevents mapping errors if packages differ
public class PriceUpdateMessage implements Serializable {

    private Long productId;
    private Double newPrice;

    // 🛡️ These must match the Double and String types from the AI Service
    private Double demandScore;
    private String changeReason;

    // Optional: Keep these if you plan to use them later,
    // but the AI service needs to send them for them to be non-null.
    private String demandLevel;
    private Double priceChangePercent;
}