package com.example.ecommerce.controller;

import com.example.ecommerce.dto.ProductResponse;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.DemandTrackingService;
import com.example.ecommerce.service.EventPublisherService;
import com.example.ecommerce.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock private ProductService productService;
    @Mock private DemandTrackingService demandTrackingService;
    @Mock private EventPublisherService eventPublisherService;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ProductController productController;

    @Test
    @DisplayName("Should get product by ID")
    void getProduct_Success() {
        ProductResponse response = new ProductResponse();
        response.setId(1L);
        response.setName("Laptop");
        response.setPrice(1000.0);

        when(productService.getProductById(1L)).thenReturn(response);

        var result = productController.getProduct(1L);

        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData().getName()).isEqualTo("Laptop");
    }

    @Test
    @DisplayName("Should delete product")
    void deleteProduct_Success() {
        doNothing().when(productService).deleteProduct(1L);

        var result = productController.deleteProduct(1L);

        assertThat(result.getBody()).isNotNull();
        verify(productService).deleteProduct(1L);
    }

    @Test
    @DisplayName("Should get recommendations")
    void getRecommendations_Success() {
        ProductResponse rec = new ProductResponse();
        rec.setId(2L);
        rec.setName("Mouse");

        when(productService.getRecommendations(1L, 4)).thenReturn(List.of(rec));

        var result = productController.getRecommendations(1L, 4);

        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData()).hasSize(1);
        assertThat(result.getBody().getData().get(0).getName()).isEqualTo("Mouse");
    }

    @Test
    @DisplayName("Should get out of stock products")
    void getOutOfStock_Success() {
        when(productService.getOutOfStockProducts()).thenReturn(List.of());

        var result = productController.getOutOfStockProducts();

        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData()).isEmpty();
    }
}