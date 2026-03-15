package interfaces;

import com.example.ecommerce.dto.ApiResponse;

public interface ICartService {
    ApiResponse addToCart(Long productId, Integer quantity);
    ApiResponse removeFromCart(Long cartItemId);
    Object getCart();
    ApiResponse clearCart();
}