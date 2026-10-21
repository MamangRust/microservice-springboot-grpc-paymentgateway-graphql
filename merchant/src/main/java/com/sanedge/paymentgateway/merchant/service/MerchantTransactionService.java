package com.sanedge.paymentgateway.merchant.service;

import java.util.List;

import com.sanedge.paymentgateway.merchant.domain.requests.FindAllMerchants;
import com.sanedge.paymentgateway.merchant.domain.requests.transactions.FindAllMerchantTransactionsByApiKey;
import com.sanedge.paymentgateway.merchant.domain.requests.transactions.FindAllMerchantTransactionsById;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.paymentgateway.merchant.domain.responses.MerchantTransactionResponse;

public interface MerchantTransactionService {
    ApiResponsePagination<List<MerchantTransactionResponse>> findAll(FindAllMerchants req);

    ApiResponsePagination<List<MerchantTransactionResponse>> findById(FindAllMerchantTransactionsById req);

    ApiResponsePagination<List<MerchantTransactionResponse>> findByApiKey(FindAllMerchantTransactionsByApiKey req);
}
