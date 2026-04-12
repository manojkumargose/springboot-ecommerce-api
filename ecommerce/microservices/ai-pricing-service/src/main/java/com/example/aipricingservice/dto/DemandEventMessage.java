package com.example.aipricingservice.dto;

import java.io.Serializable;

public class DemandEventMessage implements Serializable {
    private Long productId;
    private String eventType; // e.g., "VIEW", "PURCHASE"

    public DemandEventMessage() {}
    public DemandEventMessage(Long productId, String eventType) {
        this.productId = productId;
        this.eventType = eventType;
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Object getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
}