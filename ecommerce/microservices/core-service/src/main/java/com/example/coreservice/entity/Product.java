package com.example.coreservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {

    public enum DemandLevel { HIGH, MEDIUM, LOW }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double price;

    @Column(length = 1000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private Integer stockQuantity = 0;

    @Column(nullable = false)
    private Boolean inStock = false;

    private String imageUrl;
    private String imagePublicId;

    private Double basePrice;
    private Double currentPrice;

    @Enumerated(EnumType.STRING)
    private DemandLevel demandLevel;

    private Integer demandScore;
    private LocalDateTime lastPriceUpdate;
    private Double priceChangePercent;

    @PrePersist
    protected void onCreate() {
        if (this.basePrice == null) this.basePrice = this.price;
        if (this.currentPrice == null) this.currentPrice = this.basePrice != null ? this.basePrice : this.price;
        if (this.demandLevel == null) this.demandLevel = DemandLevel.MEDIUM;
        if (this.demandScore == null) this.demandScore = 0;
        if (this.priceChangePercent == null) this.priceChangePercent = 0.0;
        this.lastPriceUpdate = LocalDateTime.now();
    }

    public Product() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer qty) { this.stockQuantity = qty; this.inStock = qty > 0; }
    public Boolean getInStock() { return inStock; }
    public void setInStock(Boolean inStock) { this.inStock = inStock; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getImagePublicId() { return imagePublicId; }
    public void setImagePublicId(String imagePublicId) { this.imagePublicId = imagePublicId; }
    public Double getBasePrice() { return basePrice; }
    public void setBasePrice(Double basePrice) { this.basePrice = basePrice; }
    public Double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(Double currentPrice) { this.currentPrice = currentPrice; }
    public DemandLevel getDemandLevel() { return demandLevel; }
    public void setDemandLevel(DemandLevel demandLevel) { this.demandLevel = demandLevel; }
    public Integer getDemandScore() { return demandScore; }
    public void setDemandScore(Integer demandScore) { this.demandScore = demandScore; }
    public LocalDateTime getLastPriceUpdate() { return lastPriceUpdate; }
    public void setLastPriceUpdate(LocalDateTime lastPriceUpdate) { this.lastPriceUpdate = lastPriceUpdate; }
    public Double getPriceChangePercent() { return priceChangePercent; }
    public void setPriceChangePercent(Double priceChangePercent) { this.priceChangePercent = priceChangePercent; }
}
