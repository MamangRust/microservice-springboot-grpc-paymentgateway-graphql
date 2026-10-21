package com.sanedge.paymentgateway.withdraw.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.sanedge.paymentgateway.withdraw.repository.WithdrawQueryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Withdraw rows depend on a card number owned by the card service, so this
 * module does not seed downstream data. The seeder is kept as a guarded
 * placeholder so the lifecycle matches the other modules.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseSeeder implements CommandLineRunner {

    private final WithdrawQueryRepository withdrawQueryRepository;

    @Override
    public void run(String... args) {
        try {
            long existing = withdrawQueryRepository.count();
            log.info("Withdraw service seeding skipped ({} existing withdraw rows).", existing);
        } catch (Exception e) {
            log.error("Error during database seeding: {}", e.getMessage());
        }
    }
}
