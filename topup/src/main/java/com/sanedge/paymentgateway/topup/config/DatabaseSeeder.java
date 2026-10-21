package com.sanedge.paymentgateway.topup.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Topup rows reference cards and saldos owned by other services, so this
 * seeder intentionally performs no downstream calls and creates no data.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseSeeder implements CommandLineRunner {

    @Override
    public void run(String... args) {
        log.info("Topup seeder: no local seed data (topups depend on card/saldo services).");
    }
}
