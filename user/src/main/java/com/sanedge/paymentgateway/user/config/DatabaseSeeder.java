package com.sanedge.paymentgateway.user.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.sanedge.paymentgateway.user.models.User;
import com.sanedge.paymentgateway.user.repository.UserCommandRepository;
import com.sanedge.paymentgateway.user.repository.UserQueryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Seeds the users owned by this module. It deliberately does not call the role
 * service: role assignment is handled by the caller (gateway/auth) and tests
 * disable seeding entirely.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseSeeder implements CommandLineRunner {

    private final UserQueryRepository userQueryRepository;
    private final UserCommandRepository userCommandRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            seedUsers();
            log.info("Database seeding completed successfully.");
        } catch (Exception e) {
            log.error("Error during database seeding: {}", e.getMessage());
        }
    }

    private void seedUsers() {
        if (userQueryRepository.count() == 0) {
            seedUser("admin", "Admin", "User", "admin@sanedge.com");
            seedUser("user", "John", "Doe", "user@sanedge.com");
            log.info("Users seeded.");
        }
    }

    private void seedUser(String username, String firstname, String lastname, String email) {
        User user = new User();
        user.setUsername(username);
        user.setFirstname(firstname);
        user.setLastname(lastname);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password"));
        userCommandRepository.save(user);
    }
}
