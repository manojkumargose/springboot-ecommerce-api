package com.example.ecommerce.service.interfaces;

import com.example.ecommerce.dto.OrderRequest;
import com.example.ecommerce.dto.OrderResponse;
import java.util.List;

public interface IOrderService {
    OrderResponse createOrder(OrderRequest request);
    List<OrderResponse> getAllOrders();
    OrderResponse getOrderById(Long id);
    OrderResponse updateOrderStatus(Long id, String status);
}