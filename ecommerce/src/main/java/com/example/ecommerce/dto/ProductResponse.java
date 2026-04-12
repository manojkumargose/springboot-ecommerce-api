package com.example.ecommerce.dto;

public class ProductResponse {

    private Long id;
    private String name;
    private Double price;
    private String description;
    private String categoryName;
    private Integer stockQuantity;
    private Boolean inStock;
    private Double averageRating;
    private Long reviewCount;
    private String imageUrl;
    private Double basePrice;
    private Double priceChangePercent;
    private String demandLevel;
    private Integer demandScore;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public Boolean getInStock() { return inStock; }
    public void setInStock(Boolean inStock) { this.inStock = inStock; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public Long getReviewCount() { return reviewCount; }
    public void setReviewCount(Long reviewCount) { this.reviewCount = reviewCount; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Double getBasePrice() { return basePrice; }
    public void setBasePrice(Double basePrice) { this.basePrice = basePrice; }

    public Double getPriceChangePercent() { return priceChangePercent; }
    public void setPriceChangePercent(Double priceChangePercent) { this.priceChangePercent = priceChangePercent; }

    public String getDemandLevel() { return demandLevel; }
    public void setDemandLevel(String demandLevel) { this.demandLevel = demandLevel; }

    public Integer getDemandScore() { return demandScore; }
    public void setDemandScore(Integer demandScore) { this.demandScore = demandScore; }
}