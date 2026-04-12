package com.example.aipricingservice.service;

import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;

@Service
public class RecommendationService {

    public List<String> getRecommendationsForUser(Long userId) {
        // This simulates your AI logic!
        return Arrays.asList(
                "Premium Wireless Headphones",
                "Mechanical Gaming Keyboard",
                "Ultra-Wide Monitor"
        );
    }
}