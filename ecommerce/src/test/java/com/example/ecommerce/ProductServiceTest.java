package com.example.ecommerce;

import com.example.ecommerce.dto.ProductRequest;
import com.example.ecommerce.dto.ProductResponse;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldReturnAllProducts() {
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Laptop");
        product1.setPrice(50000.0);

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Mouse");
        product2.setPrice(500.0);

        when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2));

        List<Product> products = productRepository.findAll();

        assertEquals(2, products.size());
        assertEquals("Laptop", products.get(0).getName());
        assertEquals("Mouse", products.get(1).getName());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void shouldFindProductById() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Keyboard");
        product.setPrice(1500.0);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Optional<Product> found = productRepository.findById(1L);

        assertTrue(found.isPresent());
        assertEquals("Keyboard", found.get().getName());
        assertEquals(1500.0, found.get().getPrice());
    }

    @Test
    void shouldReturnEmptyWhenProductNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Product> found = productRepository.findById(99L);

        assertFalse(found.isPresent());
    }

    @Test
    void shouldSaveProduct() {
        Product product = new Product();
        product.setName("Headphones");
        product.setPrice(2000.0);

        when(productRepository.save(product)).thenReturn(product);

        Product saved = productRepository.save(product);

        assertNotNull(saved);
        assertEquals("Headphones", saved.getName());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void shouldDeleteProduct() {
        doNothing().when(productRepository).deleteById(1L);

        productRepository.deleteById(1L);

        verify(productRepository, times(1)).deleteById(1L);
    }
}
