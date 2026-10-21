package com.sanedge.paymentgateway.transaction.service;

import java.util.List;

import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.paymentgateway.transaction.domain.requests.FindAllTransactionCardNumber;
import com.sanedge.paymentgateway.transaction.domain.requests.FindAllTransactions;
import com.sanedge.paymentgateway.transaction.domain.responses.TransactionResponse;
import com.sanedge.paymentgateway.transaction.domain.responses.TransactionResponseDeleteAt;

public interface TransactionQueryService {

    ApiResponsePagination<List<TransactionResponse>> findAll(FindAllTransactions req);

    ApiResponsePagination<List<TransactionResponse>> findAllByCardNumber(FindAllTransactionCardNumber req);

    ApiResponsePagination<List<TransactionResponseDeleteAt>> findByActive(FindAllTransactions req);

    ApiResponsePagination<List<TransactionResponseDeleteAt>> findByTrashed(FindAllTransactions req);

    ApiResponse<TransactionResponse> findById(Long transactionId);

    ApiResponse<List<TransactionResponse>> findByMerchantId(Long merchantId);
}
