package com.sanedge.paymentgateway.transaction.service;

import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.transaction.domain.requests.CreateTransactionRequest;
import com.sanedge.paymentgateway.transaction.domain.requests.UpdateTransactionRequest;
import com.sanedge.paymentgateway.transaction.domain.responses.TransactionResponse;
import com.sanedge.paymentgateway.transaction.domain.responses.TransactionResponseDeleteAt;

public interface TransactionCommandService {

    ApiResponse<TransactionResponse> create(String apiKey, CreateTransactionRequest req);

    ApiResponse<TransactionResponse> update(String apiKey, UpdateTransactionRequest req);

    ApiResponse<TransactionResponseDeleteAt> trashed(Long transactionId);

    ApiResponse<TransactionResponseDeleteAt> restore(Long transactionId);

    ApiResponse<Boolean> deletePermanent(Long transactionId);

    ApiResponse<Boolean> restoreAll();

    ApiResponse<Boolean> deleteAll();
}
