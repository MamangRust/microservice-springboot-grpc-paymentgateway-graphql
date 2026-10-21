package com.sanedge.paymentgateway.common.adapter.fraud;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sanedge.paymentgateway.pb.ai_security.SecurityDomain;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

@ExtendWith(MockitoExtension.class)
class FraudFallbackTest {

    @Mock
    private FraudPort port;

    @Test
    void detectFraudPropagatesVerdictFromService() {
        when(port.detectFraud(any())).thenReturn(new FraudPort.FraudResult("txn-1", 0.9, true, "high risk"));

        FraudPort.FraudResult result = FraudFallback.detectFraud(port, query("txn-1"));

        assertThat(result.fraudulent()).isTrue();
        assertThat(result.riskScore()).isEqualTo(0.9);
    }

    @Test
    void detectFraudFailsOpenWhenServiceUnavailable() {
        when(port.detectFraud(any())).thenThrow(new StatusRuntimeException(Status.UNAVAILABLE));

        FraudPort.FraudResult result = FraudFallback.detectFraud(port, query("txn-1"));

        assertThat(result.fraudulent()).isFalse();
        assertThat(result.riskScore()).isEqualTo(0.1);
        assertThat(result.reason()).contains("unavailable");
    }

    @Test
    void verifySecurityFailsOpenWhenServiceUnavailable() {
        when(port.verifySecurity(any())).thenThrow(new StatusRuntimeException(Status.DEADLINE_EXCEEDED));

        FraudPort.SecurityResult result = FraudFallback.verifySecurity(port,
                new FraudPort.SecurityQuery(SecurityDomain.TRANSFER, "42", 100000.0));

        assertThat(result.safe()).isTrue();
        assertThat(result.action()).isEqualTo("ALLOW");
        assertThat(result.riskScore()).isEqualTo(0.1);
    }

    private static FraudPort.FraudQuery query(String transactionId) {
        return new FraudPort.FraudQuery(transactionId, 1, 42, 150000.0, "IDR", "CASH", "", "", System.currentTimeMillis());
    }
}
