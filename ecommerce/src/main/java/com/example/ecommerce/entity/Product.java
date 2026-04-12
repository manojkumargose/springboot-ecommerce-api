package com.example.ecommerce.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {

    public enum DemandLevel {
        HIGH,
        MEDIUM,
        LOW
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    private Integer initialStock;

    // ─── Dynamic Pricing Fields ──────────────────────────────

    private Double basePrice;
    private Double currentPrice;

    @Enumerated(EnumType.STRING)
    private DemandLevel demandLevel;

    private Integer demandScore;
    private LocalDateTime lastPriceUpdate;
    private Double priceChangePercent;

    @PrePersist
    protected void onCreate() {
        if (this.basePrice == null) {
            this.basePrice = this.price;
        }
        if (this.currentPrice == null) {
            this.currentPrice = this.basePrice != null ? this.basePrice : this.price;
        }
        if (this.demandLevel == null) {
            this.demandLevel = DemandLevel.MEDIUM;
        }
        if (this.demandScore == null) {
            this.demandScore = 0;
        }
        if (this.priceChangePercent == null) {
            this.priceChangePercent = 0.0;
        }
        this.lastPriceUpdate = LocalDateTime.now();
    }

    public Product() {}

    public Product(Long id, String name, double price, String description,
                   Category category, Integer stockQuantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.category = category;
        this.stockQuantity = stockQuantity;
        this.inStock = stockQuantity > 0;
        this.basePrice = price;
        this.currentPrice = price;
        this.demandLevel = DemandLevel.MEDIUM;
        this.demandScore = 0;
        this.priceChangePercent = 0.0;
    }

    // ─── Getters ─────────────────────────────────────────────

    public Long getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public Integer getStockQuantity() { return stockQuantity; }
    public Boolean getInStock() { return inStock; }
    public String getImageUrl() { return imageUrl; }
    public String getImagePublicId() { return imagePublicId; }
    public Double getBasePrice() { return basePrice; }
    public Double getCurrentPrice() { return currentPrice; }
    public DemandLevel getDemandLevel() { return demandLevel; }
    public Integer getDemandScore() { return demandScore; }
    public LocalDateTime getLastPriceUpdate() { return lastPriceUpdate; }
    public Double getPriceChangePercent() { return priceChangePercent; }

    // ─── Setters ─────────────────────────────────────────────

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPrice(double price) { this.price = price; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(Category category) { this.category = category; }
    public void setInStock(Boolean inStock) { this.inStock = inStock; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setImagePublicId(String imagePublicId) { this.imagePublicId = imagePublicId; }
    public void setBasePrice(Double basePrice) { this.basePrice = basePrice; }
    public void setCurrentPrice(Double currentPrice) { this.currentPrice = currentPrice; }
    public void setDemandLevel(DemandLevel demandLevel) { this.demandLevel = demandLevel; }
    public void setDemandScore(Integer demandScore) { this.demandScore = demandScore; }
    public void setLastPriceUpdate(LocalDateTime lastPriceUpdate) { this.lastPriceUpdate = lastPriceUpdate; }
    public void setPriceChangePercent(Double priceChangePercent) { this.priceChangePercent = priceChangePercent; }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
        this.inStock = stockQuantity > 0;
    }
    public Integer getInitialStock() { return initialStock; }
    public void setInitialStock(Integer initialStock) { this.initialStock = initialStock; }
}