package com.sanedge.paymentgateway.merchant.models;

import java.sql.Timestamp;

public interface MerchantTransactions {
    Long getTransactionId();
    String getCardNumber();
    Long getAmount();
    String getPaymentMethod();
    Long getMerchantId();
    String getMerchantName();
    Timestamp getTransactionTime();
    Timestamp getCreatedAt();
    Timestamp getUpdatedAt();
    Timestamp getDeletedAt();
}
