package com.example.ecommerce.controller;

import com.example.ecommerce.dto.ApiResponse;
import com.example.ecommerce.dto.ProductRequest;
import com.example.ecommerce.dto.ProductResponse;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.DemandTrackingService;
import com.example.ecommerce.service.EventPublisherService;
import com.example.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;
    private final DemandTrackingService demandTrackingService;
    private final EventPublisherService eventPublisherService;
    private final UserRepository userRepository;

    public ProductController(ProductService productService,
                             DemandTrackingService demandTrackingService,
                             EventPublisherService eventPublisherService,
                             UserRepository userRepository) {
        this.productService = productService;
        this.demandTrackingService = demandTrackingService;
        this.eventPublisherService = eventPublisherService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> addProduct(
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Product added successfully",
                productService.addProduct(request)));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("Products fetched",
                productService.searchProducts(search, categoryId, minPrice, maxPrice, pageable)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAvailableProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("Products fetched",
                productService.searchAvailableProducts(search, categoryId, minPrice, maxPrice, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable Long id) {

        // Track locally only (no AI price change on views)
        try {
            demandTrackingService.trackProductView(id, null);
        } catch (Exception ignored) {}

        return ResponseEntity.ok(
                ApiResponse.success("Product fetched", productService.getProductById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully",
                productService.updateProduct(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getLowStockProducts(
            @RequestParam(defaultValue = "5") int threshold) {
        return ResponseEntity.ok(ApiResponse.success("Low stock products fetched",
                productService.getLowStockProducts(threshold)));
    }

    @GetMapping("/out-of-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getOutOfStockProducts() {
        return ResponseEntity.ok(ApiResponse.success("Out of stock products fetched",
                productService.getOutOfStockProducts()));
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateStock(
            @PathVariable Long id, @RequestParam int quantity) {
        productService.restoreStock(id, quantity);
        return ResponseEntity.ok(ApiResponse.success("Stock updated successfully", null));
    }

    @PostMapping("/{id}/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> uploadImage(
            @PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success("Image uploaded successfully",
                productService.uploadProductImage(id, file)));
    }

    @GetMapping("/{id}/recommendations")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getRecommendations(
            @PathVariable Long id,
            @RequestParam(defaultValue = "4") int limit) {
        return ResponseEntity.ok(ApiResponse.success(
                "Recommendations fetched", productService.getRecommendations(id, limit)));
    }

    @DeleteMapping("/{id}/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> deleteImage(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Image deleted successfully",
                productService.deleteProductImage(id)));
    }

    public ProductResponse getProductById(long l) {
        return null;
    }
}