package com.example.ecommerce.dto;

import com.example.ecommerce.entity.DemandEvent.EventType;

public class DemandEventRequest {

    private Long productId;
    private EventType eventType;

    public DemandEventRequest() {}

    public DemandEventRequest(Long productId, EventType eventType) {
        this.productId = productId;
        this.eventType = eventType;
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }
}