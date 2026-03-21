package com.example.ecommerce.repository;

import com.example.ecommerce.entity.PricingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 📁 Location: com.example.ecommerce.repository
 * 📝 Action:   CREATE NEW FILE (Interface)
 */
@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {

    // Find active rule for a specific category
    Optional<PricingRule> findByCategoryIdAndIsActiveTrue(Long categoryId);

    // Find the global default rule (no category assigned)
    Optional<PricingRule> findByCategoryIsNullAndIsActiveTrue();

    // Find all active rules
    List<PricingRule> findByIsActiveTrue();
}