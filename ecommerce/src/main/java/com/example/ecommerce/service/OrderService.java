package com.example.ecommerce.service;

import com.example.ecommerce.dto.OrderRequest;
import com.example.ecommerce.dto.OrderResponse;
import com.example.ecommerce.entity.*;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final CouponService couponService;
    private final EmailService emailService;
    private final DemandTrackingService demandTrackingService;  // ← NEW

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository,
                        ProductService productService,
                        CouponService couponService,
                        EmailService emailService,
                        DemandTrackingService demandTrackingService) {  // ← CHANGED
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.productService = productService;
        this.couponService = couponService;
        this.emailService = emailService;
        this.demandTrackingService = demandTrackingService;  // ← NEW
    }

    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setStatus("PENDING");

        List<OrderItem> items = new ArrayList<>();
        double total = 0;

        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found: " + itemReq.getProductId()));

            productService.reduceStock(product.getId(), itemReq.getQuantity());

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemReq.getQuantity());
            item.setPrice(product.getPrice());
            items.add(item);
            total += product.getPrice() * itemReq.getQuantity();
        }

        order.setItems(items);
        order.setTotalAmount(total);

        if (request.getCouponCode() != null
                && !request.getCouponCode().isEmpty()) {
            Coupon coupon = couponService.validateCoupon(
                    request.getCouponCode(), total);
            double discount = couponService.calculateDiscount(coupon, total);
            order.setCouponCode(coupon.getCode());
            order.setDiscountAmount(discount);
            order.setFinalAmount(Math.round((total - discount) * 100.0) / 100.0);
            couponService.incrementUsage(coupon);
        } else {
            order.setDiscountAmount(0.0);
            order.setFinalAmount(total);
        }

        Order saved = orderRepository.save(order);

        // ─── NEW: Track PURCHASE events for demand-based pricing ───
        for (OrderItem item : saved.getItems()) {
            try {
                demandTrackingService.trackPurchase(
                        item.getProduct().getId(),
                        user.getId()
                );
            } catch (Exception e) {
                // Don't let tracking failure break order placement
                System.err.println("Failed to track purchase event for product #"
                        + item.getProduct().getId() + ": " + e.getMessage());
            }
        }
        // ─── END NEW ──────────────────────────────────────────────

        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            try {
                emailService.sendOrderPlacedEmail(user.getEmail(), saved);
            } catch (Exception e) {
                System.err.println("Order confirmation email failed for order #"
                        + saved.getId() + ": " + e.getMessage());
            }
        }

        return toResponsePublic(saved);
    }

    public List<OrderResponse> getMyOrders() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return orderRepository.findByUserId(user.getId())
                .stream()
                .map(this::toResponsePublic)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::toResponsePublic)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        String currentStatus = order.getStatus();

        if (currentStatus.equalsIgnoreCase("CANCELLED")) {
            throw new RuntimeException("Cannot update a cancelled order");
        }
        if (currentStatus.equalsIgnoreCase("DELIVERED")) {
            throw new RuntimeException("Cannot update a delivered order");
        }

        if (status.equalsIgnoreCase("CANCELLED")) {
            for (OrderItem item : order.getItems()) {
                productService.restoreStock(
                        item.getProduct().getId(),
                        item.getQuantity());
            }
        }

        order.setStatus(status.toUpperCase());
        Order saved = orderRepository.save(order);

        String userEmail = saved.getUser().getEmail();
        if (userEmail != null && !userEmail.isEmpty()) {
            try {
                emailService.sendOrderStatusEmail(userEmail, orderId, status);
            } catch (Exception e) {
                System.err.println("Status update email failed for order #"
                        + orderId + ": " + e.getMessage());
            }
        }

        return toResponsePublic(saved);
    }

    @Transactional
    public OrderResponse cancelMyOrder(Long orderId) {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to cancel this order");
        }

        if (!order.getStatus().equalsIgnoreCase("PENDING")) {
            throw new RuntimeException("Only PENDING orders can be cancelled");
        }

        for (OrderItem item : order.getItems()) {
            productService.restoreStock(
                    item.getProduct().getId(),
                    item.getQuantity());
        }

        order.setStatus("CANCELLED");
        Order saved = orderRepository.save(order);

        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            try {
                emailService.sendOrderCancelledEmail(user.getEmail(), orderId);
            } catch (Exception e) {
                System.err.println("Cancellation email failed for order #"
                        + orderId + ": " + e.getMessage());
            }
        }

        return toResponsePublic(saved);
    }

    public OrderResponse toResponsePublic(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUsername(order.getUser().getUsername());
        response.setTotalAmount(order.getTotalAmount());
        response.setDiscountAmount(order.getDiscountAmount());
        response.setFinalAmount(order.getFinalAmount());
        response.setCouponCode(order.getCouponCode());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());

        List<OrderResponse.OrderItemResponse> itemResponses = order.getItems()
                .stream().map(item -> {
                    OrderResponse.OrderItemResponse ir =
                            new OrderResponse.OrderItemResponse();
                    ir.setProductId(item.getProduct().getId());
                    ir.setProductName(item.getProduct().getName());
                    ir.setQuantity(item.getQuantity());
                    ir.setPrice(item.getPrice());
                    return ir;
                }).collect(Collectors.toList());

        response.setItems(itemResponses);
        return response;
    }
}