package com.sanedge.paymentgateway.withdraw.service;

import java.util.List;

import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.paymentgateway.withdraw.domain.requests.FindAllWithdrawCardNumber;
import com.sanedge.paymentgateway.withdraw.domain.requests.FindAllWithdraws;
import com.sanedge.paymentgateway.withdraw.domain.responses.WithdrawResponse;
import com.sanedge.paymentgateway.withdraw.domain.responses.WithdrawResponseDeleteAt;

public interface WithdrawQueryService {

    ApiResponsePagination<List<WithdrawResponse>> findAll(FindAllWithdraws req);

    ApiResponsePagination<List<WithdrawResponse>> findAllByCardNumber(FindAllWithdrawCardNumber req);

    ApiResponse<WithdrawResponse> findById(Long withdrawId);

    ApiResponse<List<WithdrawResponse>> findByCard(String cardNumber);

    ApiResponsePagination<List<WithdrawResponseDeleteAt>> findByActive(FindAllWithdraws req);

    ApiResponsePagination<List<WithdrawResponseDeleteAt>> findByTrashed(FindAllWithdraws req);
}
