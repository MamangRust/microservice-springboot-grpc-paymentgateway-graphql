package com.sanedge.paymentgateway.saldo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.sanedge.paymentgateway")
public class SaldoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaldoApplication.class, args);
    }
}
