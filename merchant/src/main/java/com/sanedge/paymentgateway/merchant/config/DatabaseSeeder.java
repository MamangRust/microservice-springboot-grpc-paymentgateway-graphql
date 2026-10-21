package com.sanedge.paymentgateway.merchant.config;

import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.sanedge.paymentgateway.common.enums.Status;
import com.sanedge.paymentgateway.merchant.models.Merchant;
import com.sanedge.paymentgateway.merchant.repository.MerchantCommandRepository;
import com.sanedge.paymentgateway.merchant.repository.MerchantQueryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseSeeder implements CommandLineRunner {

    private final MerchantQueryRepository merchantQueryRepository;
    private final MerchantCommandRepository merchantCommandRepository;

    @Override
    public void run(String... args) {
        try {
            seedMerchants();
            log.info("Database seeding completed successfully.");
        } catch (Exception e) {
            log.error("Error during database seeding: {}", e.getMessage());
        }
    }

    private void seedMerchants() {
        if (merchantQueryRepository.count() == 0) {
            Merchant merchant = new Merchant();
            merchant.setName("Toko Elektronik");
            merchant.setMerchantNo(UUID.randomUUID());
            merchant.setApiKey(UUID.randomUUID().toString());
            merchant.setUserId(1L);
            merchant.setStatus(Status.SUCCESS);

            merchantCommandRepository.save(merchant);
            log.info("Merchants seeded.");
        }
    }
}
