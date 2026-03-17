package com.example.ecommerce.service;

import com.example.ecommerce.dto.ProductRequest;
import com.example.ecommerce.dto.ProductResponse;
import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.CategoryRepository;
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewService reviewService;
    private final CloudinaryService cloudinaryService;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          @Lazy ReviewService reviewService,
                          CloudinaryService cloudinaryService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.reviewService = reviewService;
        this.cloudinaryService = cloudinaryService;
    }

    // ─── Add Product ──────────────────────────────────────────
    // Evicts all products cache when new product is added

    @Caching(evict = {
            @CacheEvict(value = "products", allEntries = true)
    })
    public ProductResponse addProduct(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());
        product.setCategory(category);
        product.setStockQuantity(request.getStockQuantity());

        return mapToResponse(productRepository.save(product));
    }

    // ─── Get All Products ─────────────────────────────────────
    // Cached — DB is only hit once, then served from memory

    @Cacheable(value = "products")
    public List<ProductResponse> getAllProducts() {
        System.out.println("Fetching all products from DB...");
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Get Product By ID ────────────────────────────────────
    // Cached per product ID

    @Cacheable(value = "product", key = "#id")
    public ProductResponse getProductById(Long id) {
        System.out.println("Fetching product from DB: " + id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        return mapToResponse(product);
    }

    // ─── Search Products ──────────────────────────────────────
    // Not cached because results vary by many params

    public Page<ProductResponse> searchProducts(
            String search, Long categoryId,
            Double minPrice, Double maxPrice,
            Pageable pageable) {
        return productRepository
                .searchProducts(search, categoryId, minPrice, maxPrice, pageable)
                .map(this::mapToResponse);
    }

    public Page<ProductResponse> searchAvailableProducts(
            String search, Long categoryId,
            Double minPrice, Double maxPrice,
            Pageable pageable) {
        return productRepository
                .searchAvailableProducts(search, categoryId, minPrice, maxPrice, pageable)
                .map(this::mapToResponse);
    }

    // ─── Update Product ───────────────────────────────────────
    // Evicts both product by ID and all products list cache

    @Caching(evict = {
            @CacheEvict(value = "product", key = "#id"),
            @CacheEvict(value = "products", allEntries = true)
    })
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        existing.setName(request.getName());
        existing.setPrice(request.getPrice());
        existing.setDescription(request.getDescription());
        existing.setCategory(category);
        existing.setStockQuantity(request.getStockQuantity());

        return mapToResponse(productRepository.save(existing));
    }

    // ─── Delete Product ───────────────────────────────────────
    // Evicts both caches on delete

    @Caching(evict = {
            @CacheEvict(value = "product", key = "#id"),
            @CacheEvict(value = "products", allEntries = true)
    })
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

        if (product.getImagePublicId() != null) {
            try {
                cloudinaryService.deleteImage(product.getImagePublicId());
            } catch (IOException e) {
                System.err.println("Failed to delete image: " + e.getMessage());
            }
        }

        productRepository.deleteById(id);
    }

    // ─── Reduce Stock ─────────────────────────────────────────
    // Evicts product cache since stock changed

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "product", key = "#productId"),
            @CacheEvict(value = "products", allEntries = true)
    })
    public void reduceStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock for product: " + product.getName()
                    + ". Available: " + product.getStockQuantity()
                    + ", Requested: " + quantity);
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);
    }

    // ─── Restore Stock ────────────────────────────────────────
    // Evicts product cache since stock changed

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "product", key = "#productId"),
            @CacheEvict(value = "products", allEntries = true)
    })
    public void restoreStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        product.setStockQuantity(product.getStockQuantity() + quantity);
        productRepository.save(product);
    }

    // ─── Low Stock Products ───────────────────────────────────
    // Not cached — stock changes frequently

    public List<ProductResponse> getLowStockProducts(int threshold) {
        return productRepository.findLowStockProducts(threshold)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Out of Stock Products ────────────────────────────────
    // Not cached — stock changes frequently

    public List<ProductResponse> getOutOfStockProducts() {
        return productRepository.findByInStockFalse()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Upload Product Image ─────────────────────────────────
    // Evicts product cache since image changed

    @Caching(evict = {
            @CacheEvict(value = "product", key = "#productId"),
            @CacheEvict(value = "products", allEntries = true)
    })
    public ProductResponse uploadProductImage(Long productId, MultipartFile file) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found: " + productId));

        try {
            if (product.getImagePublicId() != null) {
                cloudinaryService.deleteImage(product.getImagePublicId());
            }

            Map result = cloudinaryService.uploadImage(file, "ecommerce/products");
            product.setImageUrl((String) result.get("secure_url"));
            product.setImagePublicId((String) result.get("public_id"));

            return mapToResponse(productRepository.save(product));
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image: " + e.getMessage());
        }
    }

    // ─── Delete Product Image ─────────────────────────────────
    // Evicts product cache since image changed

    @Caching(evict = {
            @CacheEvict(value = "product", key = "#productId"),
            @CacheEvict(value = "products", allEntries = true)
    })
    public ProductResponse deleteProductImage(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found: " + productId));

        try {
            if (product.getImagePublicId() != null) {
                cloudinaryService.deleteImage(product.getImagePublicId());
                product.setImageUrl(null);
                product.setImagePublicId(null);
            }
            return mapToResponse(productRepository.save(product));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete image: " + e.getMessage());
        }
    }

    // ─── Map to Response ──────────────────────────────────────

    public ProductResponse mapToResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setPrice(product.getPrice());
        response.setDescription(product.getDescription());
        response.setStockQuantity(product.getStockQuantity());
        response.setInStock(product.getInStock());
        response.setImageUrl(product.getImageUrl());
        response.setCategoryName(
                product.getCategory() != null ? product.getCategory().getName() : null);
        response.setAverageRating(reviewService.getAverageRating(product.getId()));
        response.setReviewCount(reviewService.getReviewCount(product.getId()));
        return response;
    }
}