package com.sanedge.paymentgateway.merchant.service;

import java.util.List;

import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.paymentgateway.merchant.domain.requests.FindAllMerchantDocuments;
import com.sanedge.paymentgateway.merchant.domain.responses.MerchantDocumentResponse;
import com.sanedge.paymentgateway.merchant.domain.responses.MerchantDocumentResponseDeleteAt;

/** Query side of merchant documents. */
public interface MerchantDocumentQueryService {

    ApiResponsePagination<List<MerchantDocumentResponse>> findAll(FindAllMerchantDocuments req);

    ApiResponsePagination<List<MerchantDocumentResponseDeleteAt>> findAllActive(FindAllMerchantDocuments req);

    ApiResponsePagination<List<MerchantDocumentResponseDeleteAt>> findAllTrashed(FindAllMerchantDocuments req);

    ApiResponse<MerchantDocumentResponse> findById(Long documentId);
}
