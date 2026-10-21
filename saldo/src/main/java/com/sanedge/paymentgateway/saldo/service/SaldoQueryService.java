package com.sanedge.paymentgateway.saldo.service;

import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.paymentgateway.saldo.domain.requests.FindAllSaldos;
import com.sanedge.paymentgateway.saldo.domain.responses.SaldoResponse;
import com.sanedge.paymentgateway.saldo.domain.responses.SaldoResponseDeleteAt;

import java.util.List;

public interface SaldoQueryService {

    ApiResponsePagination<List<SaldoResponse>> findAll(FindAllSaldos request);

    ApiResponsePagination<List<SaldoResponseDeleteAt>> findActive(FindAllSaldos request);

    ApiResponsePagination<List<SaldoResponseDeleteAt>> findTrashed(FindAllSaldos request);

    ApiResponse<SaldoResponse> findByCard(String cardNumber);

    ApiResponse<SaldoResponse> findById(Long id);
}
