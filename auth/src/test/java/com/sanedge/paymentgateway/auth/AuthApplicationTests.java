package com.sanedge.paymentgateway.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AuthApplicationTests {

    @Test
    void contextLoads() {
        // fails if any bean (handler, repositories, gRPC server/client config) cannot be wired
    }
}
