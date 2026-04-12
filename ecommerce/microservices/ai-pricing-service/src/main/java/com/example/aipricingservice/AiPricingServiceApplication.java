package com.example.aipricingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration; // 👈 Add this import

// 🚀 THE FIX: Completely turn off the default Spring Security bouncer
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class AiPricingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiPricingServiceApplication.class, args);
    }
}