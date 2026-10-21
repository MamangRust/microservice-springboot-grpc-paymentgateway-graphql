package com.sanedge.paymentgateway.merchant.service;

import java.util.List;

import com.sanedge.paymentgateway.merchant.domain.requests.FindAllMerchants;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.paymentgateway.merchant.domain.responses.MerchantResponse;
import com.sanedge.paymentgateway.merchant.domain.responses.MerchantResponseDeleteAt;

public interface MerchantQueryService {
    ApiResponsePagination<List<MerchantResponse>> findAll(FindAllMerchants req);

    ApiResponsePagination<List<MerchantResponseDeleteAt>> findByActive(FindAllMerchants req);

    ApiResponsePagination<List<MerchantResponseDeleteAt>> findByTrashed(FindAllMerchants req);

    ApiResponse<MerchantResponse> findById(Long cardId);

    ApiResponse<MerchantResponse> findByApiKey(String api_key);

    ApiResponse<List<MerchantResponse>> findByUserId(Long user_id);
}
