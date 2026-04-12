package com.example.coreservice.repository; // 🎯 Matches your core-service folder

import com.example.coreservice.entity.Cart; // 🎯 Correct import for Core Service
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // 🚀 This specific query kills the LazyInitializationException for good.
    // It fetches the Cart, the Items, and the Product info in one SQL call.
    @Query("SELECT c FROM Cart c " +
            "LEFT JOIN FETCH c.items i " +
            "LEFT JOIN FETCH i.product " +
            "WHERE c.userId = :userId")
    Optional<Cart> findByUserId(@Param("userId") Long userId);
}