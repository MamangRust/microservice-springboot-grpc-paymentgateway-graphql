package com.sanedge.paymentgateway.transfer.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Transfers depend on downstream card and saldo services, so there is nothing
 * to seed locally. Kept as a guarded no-op to mirror the other modules.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseSeeder implements CommandLineRunner {

    @Override
    public void run(String... args) {
        log.info("Database seeding completed successfully.");
    }
}
