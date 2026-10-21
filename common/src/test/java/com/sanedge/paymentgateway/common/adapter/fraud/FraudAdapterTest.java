package com.sanedge.paymentgateway.common.adapter.fraud;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sanedge.paymentgateway.pb.ai_security.AISecurityServiceGrpc.AISecurityServiceBlockingStub;
import com.sanedge.paymentgateway.pb.ai_security.FraudRequest;
import com.sanedge.paymentgateway.pb.ai_security.FraudResponse;
import com.sanedge.paymentgateway.pb.ai_security.SecurityDomain;
import com.sanedge.paymentgateway.pb.ai_security.SecurityRequest;
import com.sanedge.paymentgateway.pb.ai_security.SecurityResponse;

@ExtendWith(MockitoExtension.class)
class FraudAdapterTest {

    @Mock
    private AISecurityServiceBlockingStub stub;

    private FraudAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new FraudAdapter(stub);
    }

    @Test
    void detectFraudMapsRequestAndResponse() {
        when(stub.detectFraud(any())).thenReturn(FraudResponse.newBuilder()
                .setTransactionId("txn-1")
                .setRiskScore(0.9)
                .setIsFraudulent(true)
                .setReason("high risk")
                .build());

        FraudPort.FraudResult result = adapter.detectFraud(new FraudPort.FraudQuery(
                "txn-1", 2, 42, 150000.0, "IDR", "CASH", "device-1", "127.0.0.1", 1730000000000L));

        ArgumentCaptor<FraudRequest> captor = ArgumentCaptor.forClass(FraudRequest.class);
        verify(stub).detectFraud(captor.capture());
        FraudRequest req = captor.getValue();

        assertThat(req.getTransactionId()).isEqualTo("txn-1");
        assertThat(req.getMerchantId()).isEqualTo(2);
        assertThat(req.getUserId()).isEqualTo(42);
        assertThat(req.getAmount()).isEqualTo(150000.0);
        assertThat(req.getCurrency()).isEqualTo("IDR");
        assertThat(req.getTimestamp()).isEqualTo(1730000000000L);

        assertThat(result.transactionId()).isEqualTo("txn-1");
        assertThat(result.riskScore()).isEqualTo(0.9);
        assertThat(result.fraudulent()).isTrue();
        assertThat(result.reason()).isEqualTo("high risk");
    }

    @Test
    void verifySecurityMapsRequestAndResponse() {
        when(stub.verifySecurity(any())).thenReturn(SecurityResponse.newBuilder()
                .setIsSafe(false)
                .setRiskScore(0.95)
                .setReason("deny")
                .setAction("DENY")
                .build());

        FraudPort.SecurityResult result = adapter.verifySecurity(
                new FraudPort.SecurityQuery(SecurityDomain.TRANSFER, "42", 500000.0));

        ArgumentCaptor<SecurityRequest> captor = ArgumentCaptor.forClass(SecurityRequest.class);
        verify(stub).verifySecurity(captor.capture());
        SecurityRequest req = captor.getValue();

        assertThat(req.getDomain()).isEqualTo(SecurityDomain.TRANSFER);
        assertThat(req.getEntityId()).isEqualTo("42");
        assertThat(req.getAmount()).isEqualTo(500000.0);

        assertThat(result.safe()).isFalse();
        assertThat(result.action()).isEqualTo("DENY");
        assertThat(result.riskScore()).isEqualTo(0.95);
    }
}
