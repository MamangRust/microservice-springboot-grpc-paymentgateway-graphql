package com.sanedge.paymentgateway.card.utils;

import java.security.SecureRandom;
import java.util.regex.Pattern;

public class CardNumberGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Pattern CARD_PATTERN = Pattern.compile("^\\d{16}$");

    public static String randomCardNumber() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("4");

        for (int i = 0; i < 15; i++) {
            sb.append(secureRandom.nextInt(10));
        }

        String candidate = sb.toString();

        if (CARD_PATTERN.matcher(candidate).matches()) {
            return candidate;
        } else {
            throw new Exception("Generated number is invalid");
        }
    }
}