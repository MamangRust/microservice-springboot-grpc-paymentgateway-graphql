package com.sanedge.paymentgateway.common.adapter.fraud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fail-open wrapper around {@link FraudPort}: when the ai-security service is
 * unreachable (timeout, circuit open, transport error) the call is replaced
 * with a low-risk default so payment flows keep running. A positive fraud /
 * DENY verdict from the service itself is NOT swallowed here.
 */
public final class FraudFallback {

    private static final Logger log = LoggerFactory.getLogger(FraudFallback.class);

    private FraudFallback() {
    }

    public static FraudPort.FraudResult detectFraud(FraudPort port, FraudPort.FraudQuery query) {
        try {
            return port.detectFraud(query);
        } catch (RuntimeException e) {
            log.warn("ai-security DetectFraud unavailable ({}), failing open for transaction {}",
                    e.getMessage(), query.transactionId());
            return FraudPort.FraudResult.fallback(query.transactionId());
        }
    }

    public static FraudPort.SecurityResult verifySecurity(FraudPort port, FraudPort.SecurityQuery query) {
        try {
            return port.verifySecurity(query);
        } catch (RuntimeException e) {
            log.warn("ai-security VerifySecurity unavailable ({}), failing open for {} domain={}",
                    e.getMessage(), query.entityId(), query.domain());
            return FraudPort.SecurityResult.fallback();
        }
    }
}
