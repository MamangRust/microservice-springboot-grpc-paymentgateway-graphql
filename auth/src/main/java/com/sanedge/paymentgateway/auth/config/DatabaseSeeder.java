package com.sanedge.paymentgateway.auth.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * The auth service owns no reference data: refresh tokens are created on login.
 * The seeder exists to mirror the other modules' lifecycle and deliberately makes
 * no downstream gRPC calls at startup.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseSeeder implements CommandLineRunner {

    @Override
    public void run(String... args) {
        log.info("Auth database seeding skipped (refresh tokens are created on login).");
    }
}
