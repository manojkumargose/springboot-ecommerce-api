package com.example.ecommerce.service;

import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private EventPublisherService eventPublisherService;

    @InjectMocks
    private CartService cartService;

    private User testUser;
    private Product testProduct;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Laptop");
        testProduct.setPrice(1000.0);
        testProduct.setStockQuantity(50);

        testCart = new Cart();
        testCart.setUserId(1L);
        testCart.setItems(new ArrayList<>());

        // Mock SecurityContext so getLoggedInUserId() works
        SecurityContext securityContext = mock(SecurityContext.class);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("testuser", null);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        lenient().when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
    }

    @Test
    @DisplayName("Should add new item to cart")
    void addToCart_Success() {
        when(cartRepository.findByUserId(1L)).thenReturn(testCart);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        Cart result = cartService.addToCart(1L, 2);

        assertThat(result).isNotNull();
        assertThat(testCart.getItems()).hasSize(1);
        assertThat(testCart.getItems().get(0).getQuantity()).isEqualTo(2);
        verify(eventPublisherService).publishAddToCart(1L, 1L);
    }

    @Test
    @DisplayName("Should throw when product not found")
    void addToCart_ProductNotFound() {
        when(cartRepository.findByUserId(1L)).thenReturn(testCart);
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addToCart(999L, 1))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    @DisplayName("Should throw when product out of stock")
    void addToCart_OutOfStock() {
        testProduct.setStockQuantity(0);
        when(cartRepository.findByUserId(1L)).thenReturn(testCart);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        assertThatThrownBy(() -> cartService.addToCart(1L, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("out of stock");
    }

    @Test
    @DisplayName("Should update quantity if item already in cart")
    void addToCart_UpdateExistingItem() {
        CartItem existingItem = new CartItem();
        existingItem.setProductId(1L);
        existingItem.setQuantity(2);
        existingItem.setCart(testCart);
        testCart.getItems().add(existingItem);

        when(cartRepository.findByUserId(1L)).thenReturn(testCart);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        cartService.addToCart(1L, 3);

        assertThat(existingItem.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should throw when insufficient stock for total quantity")
    void addToCart_InsufficientStockForTotal() {
        CartItem existingItem = new CartItem();
        existingItem.setProductId(1L);
        existingItem.setQuantity(48);
        existingItem.setCart(testCart);
        testCart.getItems().add(existingItem);

        when(cartRepository.findByUserId(1L)).thenReturn(testCart);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        assertThatThrownBy(() -> cartService.addToCart(1L, 5))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot add more");
    }

    @Test
    @DisplayName("Should remove item from cart")
    void removeFromCart_Success() {
        CartItem item = new CartItem();
        item.setProductId(1L);
        item.setQuantity(2);
        item.setCart(testCart);
        testCart.getItems().add(item);

        when(cartRepository.findByUserId(1L)).thenReturn(testCart);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        Cart result = cartService.removeFromCart(1L);

        assertThat(result.getItems()).isEmpty();
    }

    @Test
    @DisplayName("Should throw when removing from non-existent cart")
    void removeFromCart_CartNotFound() {
        when(cartRepository.findByUserId(1L)).thenReturn(null);

        assertThatThrownBy(() -> cartService.removeFromCart(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}