package com.example.coreservice.dto;
public class ProductResponse {
    private Long id; private String name; private double price; private String description;
    private String categoryName; private Integer stockQuantity; private Boolean inStock;
    private String imageUrl; private Double basePrice; private Double currentPrice;
    private String demandLevel; private Integer demandScore; private Double priceChangePercent;

    public Long getId() { return id; } public void setId(Long id) { this.id=id; }
    public String getName() { return name; } public void setName(String n) { this.name=n; }
    public double getPrice() { return price; } public void setPrice(double p) { this.price=p; }
    public String getDescription() { return description; } public void setDescription(String d) { this.description=d; }
    public String getCategoryName() { return categoryName; } public void setCategoryName(String c) { this.categoryName=c; }
    public Integer getStockQuantity() { return stockQuantity; } public void setStockQuantity(Integer s) { this.stockQuantity=s; }
    public Boolean getInStock() { return inStock; } public void setInStock(Boolean i) { this.inStock=i; }
    public String getImageUrl() { return imageUrl; } public void setImageUrl(String i) { this.imageUrl=i; }
    public Double getBasePrice() { return basePrice; } public void setBasePrice(Double b) { this.basePrice=b; }
    public Double getCurrentPrice() { return currentPrice; } public void setCurrentPrice(Double c) { this.currentPrice=c; }
    public String getDemandLevel() { return demandLevel; } public void setDemandLevel(String d) { this.demandLevel=d; }
    public Integer getDemandScore() { return demandScore; } public void setDemandScore(Integer d) { this.demandScore=d; }
    public Double getPriceChangePercent() { return priceChangePercent; } public void setPriceChangePercent(Double p) { this.priceChangePercent=p; }
}
