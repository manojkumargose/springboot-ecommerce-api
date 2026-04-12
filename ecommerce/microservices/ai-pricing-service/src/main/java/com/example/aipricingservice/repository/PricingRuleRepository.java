package com.example.aipricingservice.repository;

import com.example.aipricingservice.entity.PricingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {

    // FIXED: Changed IsActive to Active
    Optional<PricingRule> findByCategoryIdAndActiveTrue(Long categoryId);

    // FIXED: Changed IsActive to Active
    Optional<PricingRule> findByCategoryIdIsNullAndActiveTrue();

    // FIXED: Kept the correct one, removed the duplicate findByIsActiveTrue()
    List<PricingRule> findByActiveTrue();
}