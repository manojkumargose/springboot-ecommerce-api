package com.example.coreservice.service;

import com.example.coreservice.dto.*;
import com.example.coreservice.entity.*;
import com.example.coreservice.messaging.DemandEventPublisher;
import com.example.coreservice.repository.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final DemandEventPublisher demandEventPublisher;
    private final CartService cartService;

    /**
     * 🛒 Place a new order and notify the AI of demand
     */
    @CircuitBreaker(name = "orderService", fallbackMethod = "placeOrderFallback")
    public OrderResponse placeOrder(Long userId, OrderRequest req) {
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus("PENDING");

        List<OrderItem> items = new ArrayList<>();
        double total = 0;

        for (OrderRequest.OrderItemRequest itemReq : req.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemReq.getQuantity());

            // 💰 Price Logic: Use surged price if available, otherwise base price
            Double finalPrice = product.getCurrentPrice() != null ? product.getCurrentPrice() : product.getPrice();
            item.setPrice(finalPrice);

            items.add(item);
            total += finalPrice * item.getQuantity();

            // 📣 Notify AI Service of the demand surge
            demandEventPublisher.publishDemandEvent(product.getId(), "PURCHASE", (long) itemReq.getQuantity(), userId);
        }

        order.setItems(items);
        order.setTotalAmount(total);
        order.setFinalAmount(total);

        cartService.clearCart(userId);
        return mapToResponse(orderRepository.save(order));
    }

    /**
     * 🛡️ Resilience4j Fallback - Triggers if AI Service or DB is struggling
     */
    public OrderResponse placeOrderFallback(Long userId, OrderRequest req, Throwable t) {
        log.error("Circuit Breaker triggered for user {}: {}", userId, t.getMessage());
        return OrderResponse.builder()
                .userId(userId)
                .status("FAILED_TEMPORARILY")
                .message("Order system is under heavy load. Please try again in a moment.")
                .build();
    }

    /**
     * 🔍 Retrieve a specific order
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id, Long userId) {
        return orderRepository.findById(id)
                .filter(o -> o.getUserId().equals(userId))
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Order not found or unauthorized"));
    }

    /**
     * ❌ Cancel an order
     */
    public OrderResponse cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if(!order.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to cancel this order");
        }

        order.setStatus("CANCELLED");
        return mapToResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * 🗺️ Helper: Map Entity to DTO
     */
    private OrderResponse mapToResponse(Order o) {
        return OrderResponse.builder()
                .id(o.getId())
                .userId(o.getUserId())
                .status(o.getStatus())
                .totalAmount(o.getTotalAmount())
                .createdAt(o.getCreatedAt())
                .items(o.getItems().stream().map(i ->
                        OrderResponse.OrderItemResponse.builder()
                                .productId(i.getProduct().getId())
                                .productName(i.getProduct().getName())
                                .quantity(i.getQuantity())
                                .price(i.getPrice())
                                .build()
                ).collect(Collectors.toList()))
                .build();
    }
}