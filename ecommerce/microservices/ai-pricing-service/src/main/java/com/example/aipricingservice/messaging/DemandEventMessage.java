package com.example.aipricingservice.messaging;

import java.io.Serializable;

public class DemandEventMessage implements Serializable {
    private Long productId; private String eventType; private Long userId; private Long categoryId;
    public DemandEventMessage() {}
    public Long getProductId() { return productId; } public void setProductId(Long p) { this.productId=p; }
    public String getEventType() { return eventType; } public void setEventType(String e) { this.eventType=e; }
    public Long getUserId() { return userId; } public void setUserId(Long u) { this.userId=u; }
    public Long getCategoryId() { return categoryId; } public void setCategoryId(Long c) { this.categoryId=c; }
}
