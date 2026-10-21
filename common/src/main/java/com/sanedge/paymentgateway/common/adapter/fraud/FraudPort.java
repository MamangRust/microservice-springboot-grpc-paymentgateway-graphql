package com.sanedge.paymentgateway.common.adapter.fraud;

import com.sanedge.paymentgateway.pb.ai_security.SecurityDomain;

/**
 * Port for the ai-security service: fraud detection and domain security
 * verification, replacing direct stub usage in consumer services.
 */
public interface FraudPort {

    FraudResult detectFraud(FraudQuery query);

    SecurityResult verifySecurity(SecurityQuery query);

    record FraudQuery(
            String transactionId,
            int merchantId,
            int userId,
            double amount,
            String currency,
            String paymentMethod,
            String deviceId,
            String ipAddress,
            long timestamp) {
    }

    record FraudResult(String transactionId, double riskScore, boolean fraudulent, String reason) {

        /** Fail-open default used when the ai-security service is unreachable. */
        public static FraudResult fallback(String transactionId) {
            return new FraudResult(transactionId, 0.1, false, "Fallback: Normal (ai-security unavailable)");
        }
    }

    record SecurityQuery(SecurityDomain domain, String entityId, double amount) {
    }

    record SecurityResult(boolean safe, double riskScore, String reason, String action) {

        /** Fail-open default used when the ai-security service is unreachable. */
        public static SecurityResult fallback() {
            return new SecurityResult(true, 0.1, "Fallback: Safe (ai-security unavailable)", "ALLOW");
        }
    }
}
