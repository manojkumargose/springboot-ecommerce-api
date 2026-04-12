package com.example.coreservice.service;

import com.example.coreservice.client.PricingClient;
import com.example.coreservice.entity.Product;
import com.example.coreservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final PricingClient pricingClient;

    /**
     * 🧠 Gets a single product and updates its price using the AI service
     */
    public Product getProduct(Long id) {
        // 1. Get the basic product from Core MySQL
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        try {
            // 🎯 FIXED: Now calling getDynamicPrice and passing BOTH the ID and the DB Price
            var response = pricingClient.getDynamicPrice(id, product.getBasePrice());

            if (response != null && response.isSuccess()) {
                // 3. Update the price with the AI value
                product.setPrice(response.getData());
                log.info("🚀 AI Price applied for product {}: {}", id, response.getData());
            }
        } catch (Exception e) {
            // 🛡️ Fallback: If AI service is down, use the database price
            log.warn("⚠️ AI Pricing Service is offline. Using base price for product: {}", id);
        }

        return product;
    }

    /**
     * 📦 Gets all products from the database
     */
    public List<Product> getAllProducts() {
        log.info("Fetching all products from database");
        return productRepository.findAll();
    }

    /**
     * 🔍 SEARCH: Finds products by name
     */
    public List<Product> searchProducts(String query) {
        log.info("Searching products for: {}", query);
        return productRepository.findByNameContainingIgnoreCase(query);
    }

    /**
     * ✨ CREATE: Saves a new product
     */
    public Product createProduct(Product product) {
        log.info("Creating new product: {}", product.getName());
        return productRepository.save(product);
    }

    /**
     * 📝 UPDATE: Edits an existing product
     */
    public Product updateProduct(Long id, Product productDetails) {
        log.info("Updating product with id: {}", id);
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        existingProduct.setName(productDetails.getName());
        existingProduct.setPrice(productDetails.getPrice());
        // Add other fields here if your Product entity has them (e.g., description, stock)

        return productRepository.save(existingProduct);
    }

    /**
     * 🗑️ DELETE: Removes a product
     */
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);
        productRepository.deleteById(id);
    }
}