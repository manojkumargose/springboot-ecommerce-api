package com.example.ecommerce.service;

import com.example.ecommerce.dto.PaymentRequest;
import com.example.ecommerce.dto.PaymentResponse;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.Payment;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.PaymentRepository;
import com.example.ecommerce.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public PaymentService(PaymentRepository paymentRepository,
                          OrderRepository orderRepository,
                          UserRepository userRepository,
                          EmailService emailService) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    // ─── Get logged in user ───────────────────────────────────

    private User getLoggedInUser() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // ─── Create Payment ───────────────────────────────────────

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        User user = getLoggedInUser();

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found: " + request.getOrderId()));

        // ── Check order belongs to user ───────────────────────
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to pay for this order");
        }

        // ── Check order is in PENDING status ──────────────────
        if (!order.getStatus().equalsIgnoreCase("PENDING")) {
            throw new RuntimeException("Order is not in PENDING status");
        }

        // ── Check payment already exists ──────────────────────
        if (paymentRepository.findByOrderId(order.getId()).isPresent()) {
            throw new RuntimeException("Payment already exists for this order");
        }

        // ── Create payment record ─────────────────────────────
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getFinalAmount());
        payment.setStatus("PENDING");
        payment.setPaymentMethod(request.getPaymentMethod().toUpperCase());
        payment.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        return mapToResponse(paymentRepository.save(payment));
    }

    // ─── Verify Payment (Mock) ────────────────────────────────

    @Transactional
    public PaymentResponse verifyPayment(Long paymentId, boolean success) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found: " + paymentId));

        if (!payment.getStatus().equals("PENDING")) {
            throw new RuntimeException("Payment is already " + payment.getStatus());
        }

        if (success) {
            // ── Payment successful ────────────────────────────
            payment.setStatus("PAID");
            payment.setPaidAt(LocalDateTime.now());

            // ── Update order status to CONFIRMED ──────────────
            Order order = payment.getOrder();
            order.setStatus("CONFIRMED");
            orderRepository.save(order);

            // ── Send payment success email ────────────────────
            String userEmail = order.getUser().getEmail();
            if (userEmail != null && !userEmail.isEmpty()) {
                emailService.sendOrderStatusEmail(
                        userEmail, order.getId(), "CONFIRMED");
            }
        } else {
            // ── Payment failed ────────────────────────────────
            payment.setStatus("FAILED");
        }

        return mapToResponse(paymentRepository.save(payment));
    }

    // ─── Get My Payments ──────────────────────────────────────

    public List<PaymentResponse> getMyPayments() {
        User user = getLoggedInUser();
        return paymentRepository.findByOrderUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Get Payment by Order ─────────────────────────────────

    public PaymentResponse getPaymentByOrder(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found for order: " + orderId));
        return mapToResponse(payment);
    }

    // ─── Refund Payment ───────────────────────────────────────

    @Transactional
    public PaymentResponse refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found: " + paymentId));

        if (!payment.getStatus().equals("PAID")) {
            throw new RuntimeException("Only PAID payments can be refunded");
        }

        payment.setStatus("REFUNDED");
        return mapToResponse(paymentRepository.save(payment));
    }

    // ─── Map to Response ──────────────────────────────────────

    private PaymentResponse mapToResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setOrderId(payment.getOrder().getId());
        response.setAmount(payment.getAmount());
        response.setStatus(payment.getStatus());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setTransactionId(payment.getTransactionId());
        response.setCreatedAt(payment.getCreatedAt());
        response.setPaidAt(payment.getPaidAt());
        return response;
    }
}