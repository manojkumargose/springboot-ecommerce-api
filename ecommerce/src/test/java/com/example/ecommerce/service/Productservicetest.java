package com.example.ecommerce.service;

import com.example.ecommerce.dto.ProductRequest;
import com.example.ecommerce.dto.ProductResponse;
import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.*;
import com.example.ecommerce.service.CloudinaryService;
import com.example.ecommerce.service.EventPublisherService;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ReviewService reviewService;
    @Mock private ReviewRepository reviewRepository;
    @Mock private CloudinaryService cloudinaryService;
    @Mock private EventPublisherService eventPublisherService;
    @Mock private UserRepository userRepository;
    @Mock private OrderItemRepository orderItemRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Electronics");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Laptop");
        testProduct.setPrice(1000.0);
        testProduct.setBasePrice(1000.0);
        testProduct.setCurrentPrice(1000.0);
        testProduct.setDescription("A powerful laptop");
        testProduct.setCategory(testCategory);
        testProduct.setStockQuantity(50);
        testProduct.setInitialStock(50);
        testProduct.setDemandScore(0);
        testProduct.setPriceChangePercent(0.0);
    }

    // ========== CRUD Tests ==========

    @Nested
    @DisplayName("Add Product")
    class AddProductTests {

        @Test
        @DisplayName("Should add product successfully")
        void addProduct_Success() {
            ProductRequest request = new ProductRequest();
            request.setName("Laptop");
            request.setPrice(1000.0);
            request.setDescription("A powerful laptop");
            request.setCategoryId(1L);
            request.setStockQuantity(50);

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);
            when(reviewService.getAverageRating(1L)).thenReturn(0.0);
            when(reviewService.getReviewCount(1L)).thenReturn(0L);

            ProductResponse response = productService.addProduct(request);

            assertThat(response).isNotNull();
            assertThat(response.getName()).isEqualTo("Laptop");
            assertThat(response.getPrice()).isEqualTo(1000.0);
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Should throw exception when category not found")
        void addProduct_CategoryNotFound() {
            ProductRequest request = new ProductRequest();
            request.setCategoryId(999L);

            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.addProduct(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Category not found");
        }
    }

    @Nested
    @DisplayName("Get Product")
    class GetProductTests {

        @Test
        @DisplayName("Should get product by ID")
        void getProductById_Success() {
            when(productRepository.findByIdWithCategory(1L)).thenReturn(Optional.of(testProduct));
            when(reviewService.getAverageRating(1L)).thenReturn(4.5);
            when(reviewService.getReviewCount(1L)).thenReturn(10L);

            ProductResponse response = productService.getProductById(1L);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("Laptop");
            assertThat(response.getAverageRating()).isEqualTo(4.5);
            assertThat(response.getReviewCount()).isEqualTo(10L);
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void getProductById_NotFound() {
            when(productRepository.findByIdWithCategory(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getProductById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product not found");
        }

        @Test
        @DisplayName("Should get all products")
        void getAllProducts_Success() {
            when(productRepository.findAll()).thenReturn(List.of(testProduct));
            when(reviewRepository.findReviewStatsByProductIds(anyList())).thenReturn(List.of());

            List<ProductResponse> products = productService.getAllProducts();

            assertThat(products).hasSize(1);
            assertThat(products.get(0).getName()).isEqualTo("Laptop");
        }
    }

    @Nested
    @DisplayName("Update Product")
    class UpdateProductTests {

        @Test
        @DisplayName("Should update product successfully")
        void updateProduct_Success() {
            ProductRequest request = new ProductRequest();
            request.setName("Updated Laptop");
            request.setPrice(1200.0);
            request.setDescription("Updated description");
            request.setCategoryId(1L);
            request.setStockQuantity(60);

            Product updatedProduct = new Product();
            updatedProduct.setId(1L);
            updatedProduct.setName("Updated Laptop");
            updatedProduct.setPrice(1200.0);
            updatedProduct.setCategory(testCategory);
            updatedProduct.setStockQuantity(60);

            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
            when(reviewService.getAverageRating(1L)).thenReturn(0.0);
            when(reviewService.getReviewCount(1L)).thenReturn(0L);

            ProductResponse response = productService.updateProduct(1L, request);

            assertThat(response.getName()).isEqualTo("Updated Laptop");
            assertThat(response.getPrice()).isEqualTo(1200.0);
        }
    }

    @Nested
    @DisplayName("Delete Product")
    class DeleteProductTests {

        @Test
        @DisplayName("Should delete product successfully")
        void deleteProduct_Success() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

            productService.deleteProduct(1L);

            verify(productRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent product")
        void deleteProduct_NotFound() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.deleteProduct(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ========== Stock Management Tests ==========

    @Nested
    @DisplayName("Stock Management")
    class StockTests {

        @Test
        @DisplayName("Should reduce stock successfully")
        void reduceStock_Success() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            productService.reduceStock(1L, 10);

            assertThat(testProduct.getStockQuantity()).isEqualTo(40);
            verify(productRepository, atLeastOnce()).save(testProduct);
        }

        @Test
        @DisplayName("Should throw exception when insufficient stock")
        void reduceStock_InsufficientStock() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

            assertThatThrownBy(() -> productService.reduceStock(1L, 100))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Insufficient stock");
        }

        @Test
        @DisplayName("Should restore stock successfully")
        void restoreStock_Success() {
            testProduct.setStockQuantity(30);
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            productService.restoreStock(1L, 20);

            assertThat(testProduct.getStockQuantity()).isEqualTo(50);
        }
    }

    // ========== Stock-Based Pricing Tests ==========

    @Nested
    @DisplayName("Stock-Based AI Pricing")
    class StockBasedPricingTests {

        @Test
        @DisplayName("Price should increase when stock reduces")
        void priceIncreasesWhenStockReduces() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            productService.reduceStock(1L, 40); // 80% sold

            // price = 1000 * (1 + 0.25 * 0.8) = 1200
            assertThat(testProduct.getPrice()).isEqualTo(1200.0);
            assertThat(testProduct.getPriceChangePercent()).isEqualTo(20.0);
        }

        @Test
        @DisplayName("Price should be base price when no stock sold")
        void priceAtBaseWhenFullStock() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // Reduce 0 — technically stock stays at 50
            productService.reduceStock(1L, 0);

            assertThat(testProduct.getPrice()).isEqualTo(1000.0);
        }

        @Test
        @DisplayName("Max price increase should be 25%")
        void maxPriceIncrease25Percent() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            productService.reduceStock(1L, 49); // 98% sold

            // price = 1000 * (1 + 0.25 * 0.98) = 1245
            assertThat(testProduct.getPrice()).isLessThanOrEqualTo(1250.0);
            assertThat(testProduct.getPrice()).isGreaterThan(1200.0);
        }

        @Test
        @DisplayName("Demand level should be HIGH when 70%+ sold")
        void demandLevelHigh() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            productService.reduceStock(1L, 40); // 80% sold

            assertThat(testProduct.getDemandLevel()).isEqualTo(Product.DemandLevel.HIGH);
        }

        @Test
        @DisplayName("Demand level should be MEDIUM when 30-70% sold")
        void demandLevelMedium() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            productService.reduceStock(1L, 25); // 50% sold

            assertThat(testProduct.getDemandLevel()).isEqualTo(Product.DemandLevel.MEDIUM);
        }

        @Test
        @DisplayName("Demand level should be LOW when <30% sold")
        void demandLevelLow() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            productService.reduceStock(1L, 5); // 10% sold

            assertThat(testProduct.getDemandLevel()).isEqualTo(Product.DemandLevel.LOW);
        }

        @Test
        @DisplayName("Price should decrease when stock is restored")
        void priceDecreasesWhenStockRestored() {
            testProduct.setStockQuantity(10); // already 80% sold
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            productService.restoreStock(1L, 40); // back to full stock

            assertThat(testProduct.getPrice()).isEqualTo(1000.0);
            assertThat(testProduct.getDemandLevel()).isEqualTo(Product.DemandLevel.LOW);
        }
    }

    // ========== Recommendations Tests ==========

    @Nested
    @DisplayName("Recommendations")
    class RecommendationTests {

        @Test
        @DisplayName("Should return empty list when no recommendations")
        void noRecommendations() {
            when(orderItemRepository.findFrequentlyBoughtTogether(1L)).thenReturn(List.of());
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.findByCategoryIdAndIdNot(1L, 1L)).thenReturn(List.of());

            List<ProductResponse> recommendations = productService.getRecommendations(1L, 5);

            assertThat(recommendations).isEmpty();
        }

        @Test
        @DisplayName("Should filter out-of-stock products from recommendations")
        void filterOutOfStockRecommendations() {
            Product outOfStock = new Product();
            outOfStock.setId(2L);
            outOfStock.setName("Out of stock item");
            outOfStock.setStockQuantity(0);
            outOfStock.setCategory(testCategory);

            Product inStock = new Product();
            inStock.setId(3L);
            inStock.setName("In stock item");
            inStock.setStockQuantity(10);
            inStock.setPrice(500.0);
            inStock.setCategory(testCategory);

            when(orderItemRepository.findFrequentlyBoughtTogether(1L))
                    .thenReturn(List.of(new Object[]{2L, 5L}, new Object[]{3L, 3L}));
            when(productRepository.findAllById(List.of(2L, 3L)))
                    .thenReturn(List.of(outOfStock, inStock));
            when(reviewRepository.findReviewStatsByProductIds(anyList())).thenReturn(List.of());

            List<ProductResponse> recommendations = productService.getRecommendations(1L, 5);

            assertThat(recommendations).hasSize(1);
            assertThat(recommendations.get(0).getName()).isEqualTo("In stock item");
        }
    }
}