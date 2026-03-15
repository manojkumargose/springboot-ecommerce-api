package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // ─── Find orders by user ──────────────────────────────────
    List<Order> findByUserId(Long userId);

    // ─── Count orders by status ───────────────────────────────
    Long countByStatus(String status);

    // ─── Best selling products ────────────────────────────────
    @Query("SELECT oi.product.id as productId, " +
            "oi.product.name as productName, " +
            "SUM(oi.quantity) as totalSold, " +
            "SUM(oi.quantity * oi.price) as totalRevenue " +
            "FROM OrderItem oi " +
            "WHERE oi.order.status != 'CANCELLED' " +
            "GROUP BY oi.product.id, oi.product.name " +
            "ORDER BY totalSold DESC")
    List<Object[]> findBestSellingProducts(Pageable pageable);
}