package com.sanedge.paymentgateway.transaction.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Transaction service owns no reference data to seed. This runner exists so the
 * module mirrors the other services' seeding hook without performing any
 * downstream gRPC calls during startup.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseSeeder implements CommandLineRunner {

    @Override
    public void run(String... args) {
        log.info("Transaction service has no reference data to seed.");
    }
}
