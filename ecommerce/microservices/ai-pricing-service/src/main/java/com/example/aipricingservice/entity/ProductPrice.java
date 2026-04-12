package com.example.aipricingservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_prices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductPrice {
    @Id
    private Long productId; // This matches the ID from Core-Service
    private Double basePrice;
    private Double demandScore; // 1.0 = Normal, 1.2 = High, 0.8 = Low
    private Integer stockLevel;
}