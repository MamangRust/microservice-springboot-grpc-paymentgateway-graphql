package com.sanedge.paymentgateway.merchant.service;

import com.sanedge.paymentgateway.merchant.domain.requests.CreateMerchantRequest;
import com.sanedge.paymentgateway.merchant.domain.requests.UpdateMerchantRequest;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.merchant.domain.responses.MerchantResponse;
import com.sanedge.paymentgateway.merchant.domain.responses.MerchantResponseDeleteAt;

public interface MerchantCommandService {
    ApiResponse<MerchantResponse> createMerchant(CreateMerchantRequest req);

    ApiResponse<MerchantResponse> updateMerchant(UpdateMerchantRequest req);

    ApiResponse<MerchantResponse> updateMerchantStatus(Integer merchantId, String status);

    ApiResponse<MerchantResponseDeleteAt> trashMerchant(Long id);

    ApiResponse<MerchantResponseDeleteAt> restoreMerchant(Long id);

    ApiResponse<Boolean> deleteMerchant(Long id);

    ApiResponse<Boolean> restoreAll();

    ApiResponse<Boolean> deleteAll();
}
