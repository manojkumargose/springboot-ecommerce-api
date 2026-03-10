package com.example.ecommerce.service;

import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.repository.CartRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    public Cart getCart(Long userId) {

        Cart cart = cartRepository.findByUserId(userId);

        if (cart == null) {
            cart = new Cart();
            cart.setUserId(userId);
            cart.setItems(new ArrayList<>());
            cartRepository.save(cart);
        }

        return cart;
    }

    public Cart addToCart(Long userId, Long productId, int quantity) {

        Cart cart = getCart(userId);

        CartItem item = new CartItem();
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setCart(cart);

        cart.getItems().add(item);

        return cartRepository.save(cart);
    }
}
