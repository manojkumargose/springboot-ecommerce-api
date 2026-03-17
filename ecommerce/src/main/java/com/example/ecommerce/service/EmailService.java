package com.example.ecommerce.service;

import com.example.ecommerce.entity.Order;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
            System.out.println("Email sent successfully to: " + to);
        } catch (MessagingException e) {
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
        }
    }

    @Async("emailTaskExecutor")
    public void sendWelcomeEmail(String to, String username) {
        String subject = "Welcome to Our Store!";
        String body = """
                <h2>Welcome, %s!</h2>
                <p>Thank you for registering at our store.</p>
                <p>Start shopping now and enjoy exclusive deals!</p>
                <br>
                <p>Happy Shopping!</p>
                """.formatted(username);
        sendEmail(to, subject, body);
    }

    @Async("emailTaskExecutor")
    public void sendOrderPlacedEmail(String to, Order order) {
        String subject = "Order Confirmed! #" + order.getId();

        StringBuilder items = new StringBuilder();
        order.getItems().forEach(item -> {
            items.append("<tr>")
                    .append("<td>").append(item.getProduct().getName()).append("</td>")
                    .append("<td>").append(item.getQuantity()).append("</td>")
                    .append("<td>Rs.").append(item.getPrice()).append("</td>")
                    .append("</tr>");
        });

        String body = """
                <h2>Order Confirmed!</h2>
                <p>Thank you for your order. Here are your order details:</p>
                <p><b>Order ID:</b> #%d</p>
                <p><b>Status:</b> %s</p>
                <table border="1" cellpadding="8" cellspacing="0">
                    <tr>
                        <th>Product</th>
                        <th>Quantity</th>
                        <th>Price</th>
                    </tr>
                    %s
                </table>
                <br>
                <p><b>Total Amount:</b> Rs.%s</p>
                %s
                <p><b>Final Amount:</b> Rs.%s</p>
                <br>
                <p>We will notify you when your order is shipped!</p>
                """.formatted(
                order.getId(),
                order.getStatus(),
                items.toString(),
                order.getTotalAmount(),
                order.getDiscountAmount() > 0
                        ? "<p><b>Discount:</b> -Rs." + order.getDiscountAmount() + "</p>"
                        : "",
                order.getFinalAmount()
        );

        sendEmail(to, subject, body);
    }

    @Async("emailTaskExecutor")
    public void sendOrderStatusEmail(String to, Long orderId, String status) {
        String subject = "Order #" + orderId + " Status Update";

        String statusMessage = switch (status.toUpperCase()) {
            case "CONFIRMED" -> "Your order has been confirmed!";
            case "SHIPPED" -> "Your order has been shipped!";
            case "DELIVERED" -> "Your order has been delivered!";
            case "CANCELLED" -> "Your order has been cancelled.";
            default -> "Your order status has been updated to: " + status;
        };

        String body = """
                <h2>Order Update</h2>
                <p><b>Order ID:</b> #%d</p>
                <p>%s</p>
                <br>
                <p>Thank you for shopping with us!</p>
                """.formatted(orderId, statusMessage);

        sendEmail(to, subject, body);
    }

    @Async("emailTaskExecutor")
    public void sendOrderCancelledEmail(String to, Long orderId) {
        String subject = "Order #" + orderId + " Cancelled";
        String body = """
                <h2>Order Cancelled</h2>
                <p>Your order <b>#%d</b> has been cancelled.</p>
                <p>If you paid for this order, a refund will be processed shortly.</p>
                <br>
                <p>We hope to see you again soon!</p>
                """.formatted(orderId);
        sendEmail(to, subject, body);
    }
}