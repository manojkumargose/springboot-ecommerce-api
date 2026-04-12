package com.example.ecommerce.repository;

import com.example.ecommerce.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT oi2.product.id, COUNT(oi2) as freq FROM OrderItem oi1 " +
            "JOIN OrderItem oi2 ON oi1.order.id = oi2.order.id " +
            "WHERE oi1.product.id = :productId AND oi2.product.id != :productId " +
            "GROUP BY oi2.product.id ORDER BY freq DESC")
    List<Object[]> findFrequentlyBoughtTogether(@Param("productId") Long productId);
}