package com.example.coreservice.controller;

import com.example.coreservice.dto.ApiResponse;
import com.example.coreservice.entity.Payment;
import com.example.coreservice.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<Payment>> processPayment(@Valid @RequestBody Payment payment) {
        Payment processed = paymentService.processPayment(payment);
        return ResponseEntity.ok(ApiResponse.success("Payment processed successfully", processed));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<Payment>> getByOrder(@PathVariable Long orderId) {
        Payment payment = paymentService.getPaymentByOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Payment fetched successfully", payment));
    }
}