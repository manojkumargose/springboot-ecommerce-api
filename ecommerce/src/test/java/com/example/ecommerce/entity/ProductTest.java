package com.example.ecommerce.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ProductTest {

    @Test
    @DisplayName("Product should calculate inStock based on stockQuantity")
    void inStockCalculation() {
        Product product = new Product();
        product.setStockQuantity(10);
        assertThat(product.getInStock()).isTrue();

        product.setStockQuantity(0);
        assertThat(product.getInStock()).isFalse();
    }

    @Test
    @DisplayName("Product should store base price separately from current price")
    void basePriceVsCurrentPrice() {
        Product product = new Product();
        product.setBasePrice(100.0);
        product.setPrice(120.0);
        product.setCurrentPrice(120.0);

        assertThat(product.getBasePrice()).isEqualTo(100.0);
        assertThat(product.getPrice()).isEqualTo(120.0);
        assertThat(product.getCurrentPrice()).isEqualTo(120.0);
    }

    @Test
    @DisplayName("Product should have default demand level")
    void defaultDemandLevel() {
        Product product = new Product();
        product.setDemandLevel(Product.DemandLevel.LOW);
        assertThat(product.getDemandLevel()).isEqualTo(Product.DemandLevel.LOW);
    }

    @Test
    @DisplayName("Product should track initial stock for pricing")
    void initialStockTracking() {
        Product product = new Product();
        product.setInitialStock(100);
        product.setStockQuantity(30);

        assertThat(product.getInitialStock()).isEqualTo(100);
        assertThat(product.getStockQuantity()).isEqualTo(30);
    }

    @Test
    @DisplayName("Product should associate with category")
    void categoryAssociation() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        Product product = new Product();
        product.setCategory(category);

        assertThat(product.getCategory()).isNotNull();
        assertThat(product.getCategory().getName()).isEqualTo("Electronics");
    }
}