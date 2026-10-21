package com.sanedge.paymentgateway.common.adapter.fraud;

import com.sanedge.paymentgateway.pb.ai_security.AISecurityServiceGrpc.AISecurityServiceBlockingStub;
import com.sanedge.paymentgateway.pb.ai_security.FraudRequest;
import com.sanedge.paymentgateway.pb.ai_security.FraudResponse;
import com.sanedge.paymentgateway.pb.ai_security.SecurityRequest;
import com.sanedge.paymentgateway.pb.ai_security.SecurityResponse;

public class FraudAdapter implements FraudPort {

    private final AISecurityServiceBlockingStub stub;

    public FraudAdapter(AISecurityServiceBlockingStub stub) {
        this.stub = stub;
    }

    @Override
    public FraudResult detectFraud(FraudQuery query) {
        FraudResponse resp = stub.detectFraud(FraudRequest.newBuilder()
                .setTransactionId(query.transactionId())
                .setMerchantId(query.merchantId())
                .setUserId(query.userId())
                .setAmount(query.amount())
                .setCurrency(query.currency())
                .setPaymentMethod(query.paymentMethod())
                .setDeviceId(query.deviceId())
                .setIpAddress(query.ipAddress())
                .setTimestamp(query.timestamp())
                .build());
        return new FraudResult(resp.getTransactionId(), resp.getRiskScore(), resp.getIsFraudulent(),
                resp.getReason());
    }

    @Override
    public SecurityResult verifySecurity(SecurityQuery query) {
        SecurityResponse resp = stub.verifySecurity(SecurityRequest.newBuilder()
                .setDomain(query.domain())
                .setEntityId(query.entityId())
                .setAmount(query.amount())
                .build());
        return new SecurityResult(resp.getIsSafe(), resp.getRiskScore(), resp.getReason(), resp.getAction());
    }
}
