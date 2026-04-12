package com.example.coreservice.dto;
import jakarta.validation.constraints.*;
public class ReviewRequest {
    @NotNull private Long productId;
    @Min(1) @Max(5) private Integer rating;
    @Size(max=1000) private String comment;
    public Long getProductId() { return productId; } public void setProductId(Long p) { this.productId=p; }
    public Integer getRating() { return rating; } public void setRating(Integer r) { this.rating=r; }
    public String getComment() { return comment; } public void setComment(String c) { this.comment=c; }
}
