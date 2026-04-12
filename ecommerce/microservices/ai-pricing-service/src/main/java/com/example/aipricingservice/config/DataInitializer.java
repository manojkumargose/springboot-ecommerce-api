package com.example.aipricingservice.config;

import com.example.aipricingservice.entity.DemandEvent;
import com.example.aipricingservice.repository.DemandEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final DemandEventRepository repository;
    private final Random random = new Random();

    @Override
    public void run(String... args) {
        if (repository.count() == 0) {
            log.info("Database is empty. Generating mock demand data...");

            // Use the Enum values directly to ensure 100% compatibility
            DemandEvent.EventType[] eventTypes = DemandEvent.EventType.values();

            for (int i = 0; i < 100; i++) {
                // Randomly pick a Product ID between 1 and 10
                long productId = random.nextLong(10) + 1;

                // Pick a random valid EventType from the Enum
                DemandEvent.EventType type = eventTypes[random.nextInt(eventTypes.length)];

                DemandEvent event = DemandEvent.builder()
                        .productId(productId)
                        .eventType(type)
                        .userId((long) (random.nextInt(50) + 1)) // Mock user IDs
                        .createdAt(LocalDateTime.now().minusHours(random.nextInt(48))) // Spread over 2 days
                        .build();

                repository.save(event);
            }
            log.info("Successfully injected 100 mock demand events!");
        } else {
            log.info("Database already contains data. Skipping initialization.");
        }
    }
}