package com.sanedge.paymentgateway.topup.service;

import java.util.List;

import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.paymentgateway.topup.domain.requests.FindAllTopups;
import com.sanedge.paymentgateway.topup.domain.requests.FindAllTopupsByCardNumber;
import com.sanedge.paymentgateway.topup.domain.responses.TopupResponse;
import com.sanedge.paymentgateway.topup.domain.responses.TopupResponseDeleteAt;

public interface TopupQueryService {

    ApiResponsePagination<List<TopupResponse>> findAll(FindAllTopups req);

    ApiResponsePagination<List<TopupResponse>> findAllByCardNumber(FindAllTopupsByCardNumber req);

    ApiResponsePagination<List<TopupResponseDeleteAt>> findActive(FindAllTopups req);

    ApiResponsePagination<List<TopupResponseDeleteAt>> findTrashed(FindAllTopups req);

    ApiResponse<List<TopupResponse>> findByCard(String cardNumber);

    ApiResponse<TopupResponse> findById(Long topupId);
}
