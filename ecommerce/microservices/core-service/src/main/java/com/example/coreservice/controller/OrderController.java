package com.example.coreservice.controller;

import com.example.coreservice.dto.ApiResponse;
import com.example.coreservice.dto.OrderRequest;
import com.example.coreservice.dto.OrderResponse;
import com.example.coreservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 🛒 Place a new order
     * Triggers the DemandEventPublisher to notify AI of purchase
     */
    @PostMapping("/place/{userId}")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @PathVariable Long userId,
            @RequestBody OrderRequest request) {
        OrderResponse response = orderService.placeOrder(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Order placed successfully", response));
    }

    /**
     * 📋 Fetch all orders for a specific user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(@PathVariable Long userId) {
        List<OrderResponse> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(ApiResponse.success("Orders fetched successfully", orders));
    }

    /**
     * 🔍 Fetch details of a specific order
     */
    @GetMapping("/{id}/user/{userId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @PathVariable Long id,
            @PathVariable Long userId) {
        OrderResponse order = orderService.getOrder(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Order details retrieved", order));
    }

    /**
     * ❌ Cancel an existing order
     */
    @PostMapping("/{id}/cancel/user/{userId}")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Long id,
            @PathVariable Long userId) {
        OrderResponse response = orderService.cancelOrder(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", response));
    }
}