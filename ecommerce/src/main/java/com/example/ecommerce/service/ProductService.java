package com.example.ecommerce.service;

import com.example.ecommerce.dto.ProductRequest;
import com.example.ecommerce.dto.ProductResponse;
import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.CategoryRepository;
import com.example.ecommerce.repository.OrderItemRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.ReviewRepository;
import com.example.ecommerce.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductService {

    // Max price increase = 25% when nearly sold out
    private static final double MAX_PRICE_INCREASE = 0.25;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;
    private final CloudinaryService cloudinaryService;
    private final EventPublisherService eventPublisherService;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          @Lazy ReviewService reviewService,
                          ReviewRepository reviewRepository,
                          CloudinaryService cloudinaryService,
                          EventPublisherService eventPublisherService,
                          UserRepository userRepository,
                          OrderItemRepository orderItemRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.reviewService = reviewService;
        this.reviewRepository = reviewRepository;
        this.cloudinaryService = cloudinaryService;
        this.eventPublisherService = eventPublisherService;
        this.userRepository = userRepository;
        this.orderItemRepository = orderItemRepository;
    }

    /**
     * Stock-based AI pricing formula:
     * price = basePrice * (1 + MAX_INCREASE * percentSold)
     *
     * Example: basePrice=500, initialStock=50, currentStock=10
     * percentSold = (50-10)/50 = 0.8
     * price = 500 * (1 + 0.25 * 0.8) = 500 * 1.20 = $600
     */
    private void recalculateStockBasedPrice(Product product) {
        Double basePrice = product.getBasePrice();
        Integer initialStock = product.getInitialStock();
        Integer currentStock = product.getStockQuantity();

        if (basePrice == null || initialStock == null || initialStock <= 0) return;

        double percentSold = (double)(initialStock - currentStock) / initialStock;
        percentSold = Math.max(0, Math.min(1, percentSold)); // clamp 0-1

        double newPrice = basePrice * (1 + MAX_PRICE_INCREASE * percentSold);
        newPrice = Math.round(newPrice * 100.0) / 100.0; // round to 2 decimals

        double oldPrice = product.getPrice();
        double changePercent = ((newPrice - basePrice) / basePrice) * 100;

        product.setPrice(newPrice);
        product.setCurrentPrice(newPrice);
        product.setPriceChangePercent(Math.round(changePercent * 10.0) / 10.0);
        product.setLastPriceUpdate(LocalDateTime.now());

        // Set demand level based on percent sold
        if (percentSold >= 0.7) {
            product.setDemandLevel(Product.DemandLevel.HIGH);
        } else if (percentSold >= 0.3) {
            product.setDemandLevel(Product.DemandLevel.MEDIUM);
        } else {
            product.setDemandLevel(Product.DemandLevel.LOW);
        }
        product.setDemandScore((int)(percentSold * 100));

        productRepository.save(product);
    }

    public List<ProductResponse> getRecommendations(Long productId, int limit) {
        List<Object[]> boughtTogether = orderItemRepository.findFrequentlyBoughtTogether(productId);
        List<Long> recommendedIds = boughtTogether.stream()
                .map(row -> (Long) row[0])
                .limit(limit)
                .collect(Collectors.toList());

        if (recommendedIds.size() < limit) {
            Product current = productRepository.findById(productId).orElse(null);
            if (current != null && current.getCategory() != null) {
                List<Product> sameCategory = productRepository.findByCategoryIdAndIdNot(
                        current.getCategory().getId(), productId);
                for (Product p : sameCategory) {
                    if (!recommendedIds.contains(p.getId()) && recommendedIds.size() < limit) {
                        recommendedIds.add(p.getId());
                    }
                }
            }
        }

        if (recommendedIds.isEmpty()) return List.of();

        List<Product> products = productRepository.findAllById(recommendedIds);
        products = products.stream()
                .filter(p -> p.getStockQuantity() != null && p.getStockQuantity() > 0)
                .collect(Collectors.toList());
        return mapToResponseBatch(products);
    }

    private Map<Long, double[]> loadReviewStats(List<Long> productIds) {
        Map<Long, double[]> statsMap = new HashMap<>();
        if (productIds == null || productIds.isEmpty()) return statsMap;
        List<Object[]> results = reviewRepository.findReviewStatsByProductIds(productIds);
        for (Object[] row : results) {
            Long productId = (Long) row[0];
            Double avgRating = row[1] != null ? (Double) row[1] : 0.0;
            Long count = (Long) row[2];
            statsMap.put(productId, new double[]{avgRating, count});
        }
        return statsMap;
    }

    private List<ProductResponse> mapToResponseBatch(List<Product> products) {
        List<Long> ids = products.stream().map(Product::getId).collect(Collectors.toList());
        Map<Long, double[]> reviewStats = loadReviewStats(ids);
        return products.stream().map(product -> {
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
            response.setBasePrice(product.getBasePrice());
            response.setPriceChangePercent(product.getPriceChangePercent());
            response.setDemandLevel(product.getDemandLevel() != null ? product.getDemandLevel().name() : null);
            response.setDemandScore(product.getDemandScore());
            double[] stats = reviewStats.getOrDefault(product.getId(), new double[]{0.0, 0});
            response.setAverageRating(stats[0]);
            response.setReviewCount((long) stats[1]);
            return response;
        }).collect(Collectors.toList());
    }

    @Caching(evict = { @CacheEvict(value = "products", allEntries = true) })
    public ProductResponse addProduct(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setBasePrice(request.getPrice());
        product.setCurrentPrice(request.getPrice());
        product.setDescription(request.getDescription());
        product.setCategory(category);
        product.setStockQuantity(request.getStockQuantity());
        product.setInitialStock(request.getStockQuantity());
        product.setPriceChangePercent(0.0);
        product.setDemandLevel(Product.DemandLevel.LOW);
        product.setDemandScore(0);
        Product saved = productRepository.save(product);
        try {
            eventPublisherService.publishProductSync(saved.getId(), saved.getName(), saved.getPrice());
        } catch (Exception ignored) {}
        return mapToResponse(saved);
    }

    @Cacheable(value = "products")
    public List<ProductResponse> getAllProducts() {
        return mapToResponseBatch(productRepository.findAll());
    }

    @Cacheable(value = "product", key = "#id")
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        trackProductView(id);
        return mapToResponse(product);
    }

    private void trackProductView(Long productId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()
                    && !"anonymousUser".equals(auth.getPrincipal())) {
                User user = userRepository.findByUsername(auth.getName()).orElse(null);
                Long userId = user != null ? user.getId() : 0L;
                eventPublisherService.publishProductView(productId, userId);
            }
        } catch (Exception e) {
        }
    }

    public Page<ProductResponse> searchProducts(
            String search, Long categoryId,
            Double minPrice, Double maxPrice, Pageable pageable) {
        Page<Product> productPage = productRepository
                .searchProducts(search, categoryId, minPrice, maxPrice, pageable);
        return new PageImpl<>(mapToResponseBatch(productPage.getContent()),
                pageable, productPage.getTotalElements());
    }

    public Page<ProductResponse> searchAvailableProducts(
            String search, Long categoryId,
            Double minPrice, Double maxPrice, Pageable pageable) {
        Page<Product> productPage = productRepository
                .searchAvailableProducts(search, categoryId, minPrice, maxPrice, pageable);
        return new PageImpl<>(mapToResponseBatch(productPage.getContent()),
                pageable, productPage.getTotalElements());
    }

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
        existing.setBasePrice(request.getPrice());
        existing.setCurrentPrice(request.getPrice());
        existing.setDescription(request.getDescription());
        existing.setCategory(category);
        existing.setStockQuantity(request.getStockQuantity());
        existing.setInitialStock(request.getStockQuantity());
        existing.setPriceChangePercent(0.0);
        existing.setDemandLevel(Product.DemandLevel.LOW);
        existing.setDemandScore(0);
        Product saved = productRepository.save(existing);
        try {
            eventPublisherService.publishProductSync(saved.getId(), saved.getName(), saved.getPrice());
        } catch (Exception ignored) {}
        return mapToResponse(saved);
    }

    @Caching(evict = {
            @CacheEvict(value = "product", key = "#id"),
            @CacheEvict(value = "products", allEntries = true)
    })
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        if (product.getImagePublicId() != null) {
            try { cloudinaryService.deleteImage(product.getImagePublicId()); }
            catch (IOException e) { System.err.println("Failed to delete image: " + e.getMessage()); }
        }
        productRepository.deleteById(id);
    }

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
                    + ". Available: " + product.getStockQuantity() + ", Requested: " + quantity);
        }
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);

        // Recalculate price based on stock
        recalculateStockBasedPrice(product);
    }

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

        // Recalculate price based on stock
        recalculateStockBasedPrice(product);
    }

    public List<ProductResponse> getLowStockProducts(int threshold) {
        return mapToResponseBatch(productRepository.findLowStockProducts(threshold));
    }

    public List<ProductResponse> getOutOfStockProducts() {
        return mapToResponseBatch(productRepository.findByInStockFalse());
    }

    @Caching(evict = {
            @CacheEvict(value = "product", key = "#productId"),
            @CacheEvict(value = "products", allEntries = true)
    })
    public ProductResponse uploadProductImage(Long productId, MultipartFile file) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        try {
            if (product.getImagePublicId() != null) cloudinaryService.deleteImage(product.getImagePublicId());
            Map result = cloudinaryService.uploadImage(file, "ecommerce/products");
            product.setImageUrl((String) result.get("secure_url"));
            product.setImagePublicId((String) result.get("public_id"));
            return mapToResponse(productRepository.save(product));
        } catch (IOException e) { throw new RuntimeException("Failed to upload image: " + e.getMessage()); }
    }

    @Caching(evict = {
            @CacheEvict(value = "product", key = "#productId"),
            @CacheEvict(value = "products", allEntries = true)
    })
    public ProductResponse deleteProductImage(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        try {
            if (product.getImagePublicId() != null) {
                cloudinaryService.deleteImage(product.getImagePublicId());
                product.setImageUrl(null);
                product.setImagePublicId(null);
            }
            return mapToResponse(productRepository.save(product));
        } catch (IOException e) { throw new RuntimeException("Failed to delete image: " + e.getMessage()); }
    }

    public ProductResponse mapToResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setPrice(product.getPrice());
        response.setDescription(product.getDescription());
        response.setStockQuantity(product.getStockQuantity());
        response.setInStock(product.getInStock());
        response.setImageUrl(product.getImageUrl());
        response.setCategoryName(product.getCategory() != null ? product.getCategory().getName() : null);
        response.setBasePrice(product.getBasePrice());
        response.setPriceChangePercent(product.getPriceChangePercent());
        response.setDemandLevel(product.getDemandLevel() != null ? product.getDemandLevel().name() : null);
        response.setDemandScore(product.getDemandScore());
        response.setAverageRating(reviewService.getAverageRating(product.getId()));
        response.setReviewCount(reviewService.getReviewCount(product.getId()));
        return response;
    }
}