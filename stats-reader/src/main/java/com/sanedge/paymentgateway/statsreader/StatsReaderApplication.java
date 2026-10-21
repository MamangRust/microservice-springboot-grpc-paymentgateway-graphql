package com.sanedge.paymentgateway.statsreader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.sanedge.paymentgateway")
public class StatsReaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(StatsReaderApplication.class, args);
    }
}
