package com.sanedge.paymentgateway.saldo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.sanedge.paymentgateway.saldo.repository.SaldoQueryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Saldo rows are tied to card numbers owned by the card service, so there is no
 * self-contained seed data to create here. The runner only reports the current
 * row count; it never calls downstream services.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseSeeder implements CommandLineRunner {

    private final SaldoQueryRepository saldoQueryRepository;

    @Override
    public void run(String... args) {
        try {
            long count = saldoQueryRepository.count();
            log.info("Database seeding skipped for saldo service (existing saldo rows: {})", count);
        } catch (Exception e) {
            log.error("Error during database seeding: {}", e.getMessage());
        }
    }
}
