package com.example.coreservice.service;

import com.example.coreservice.dto.PaymentRequest;
import com.example.coreservice.entity.Order;
import com.example.coreservice.entity.Payment;
import com.example.coreservice.repository.OrderRepository;
import com.example.coreservice.repository.PaymentRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

// ... your @Service and class code continues below
@Service
@Transactional
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    public Payment processPayment(PaymentRequest req, Long userId) {
        // 1. Fetch the Order
        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + req.getOrderId()));

        // 2. Authorization Check
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: This order does not belong to you.");
        }

        // 3. Status Guard: Don't pay for an order that is already confirmed or cancelled
        if ("CONFIRMED".equals(order.getStatus())) {
            throw new IllegalStateException("Payment already processed. Order is already CONFIRMED.");
        }
        if ("CANCELLED".equals(order.getStatus())) {
            throw new IllegalStateException("Cannot pay for a CANCELLED order.");
        }

        // 4. Repository Guard: Double-check the Payment table (Idempotency)
        // This stops the "Duplicate entry" SQL error before it happens
        if (paymentRepository.findByOrderId(req.getOrderId()).isPresent()) {
            throw new IllegalStateException("Payment record already exists for this order.");
        }

        // 5. Create Payment Record
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getFinalAmount() != null ? order.getFinalAmount() : order.getTotalAmount());
        payment.setPaymentMethod(req.getPaymentMethod());
        payment.setTransactionId(req.getTransactionId());
        payment.setStatus("PAID");
        payment.setPaidAt(LocalDateTime.now());

        // 6. Update Order Status
        order.setStatus("CONFIRMED");
        orderRepository.save(order);

        // 7. Save and Return
        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Payment getPaymentByOrder(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for Order ID: " + orderId));
    }

    public Payment processPayment(@Valid Payment payment) {
        return null;
    }
}