package com.sanedge.paymentgateway.card.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseSeeder implements CommandLineRunner {

    @Override
    public void run(String... args) {
        try {
            log.info("Card service has no standalone seed data; skipping database seeding.");
        } catch (Exception e) {
            log.error("Error during database seeding: {}", e.getMessage());
        }
    }
}
