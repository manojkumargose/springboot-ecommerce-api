package com.example.aipricingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PriceUpdateMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long productId;
    private Double newPrice;

    // ── AI Analytics Fields ──
    private Double demandScore;
    private String changeReason;

    // ── Optional metadata to match Core Service ──
    private String demandLevel;
    private Double priceChangePercent;
}