package com.example.coreservice.dto;
import jakarta.validation.constraints.*;
public class ProductRequest {
    @NotBlank private String name;
    @Positive private double price;
    private String description;
    private Long categoryId;
    @Min(0) private Integer stockQuantity = 0;
    private String imageUrl;
    private String imagePublicId;
    public String getName() { return name; } public void setName(String n) { this.name=n; }
    public double getPrice() { return price; } public void setPrice(double p) { this.price=p; }
    public String getDescription() { return description; } public void setDescription(String d) { this.description=d; }
    public Long getCategoryId() { return categoryId; } public void setCategoryId(Long c) { this.categoryId=c; }
    public Integer getStockQuantity() { return stockQuantity; } public void setStockQuantity(Integer s) { this.stockQuantity=s; }
    public String getImageUrl() { return imageUrl; } public void setImageUrl(String i) { this.imageUrl=i; }
    public String getImagePublicId() { return imagePublicId; } public void setImagePublicId(String i) { this.imagePublicId=i; }
}
