package com.sanedge.paymentgateway.saldo.service;

import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.saldo.domain.requests.CreateSaldoRequest;
import com.sanedge.paymentgateway.saldo.domain.requests.UpdateSaldoBalance;
import com.sanedge.paymentgateway.saldo.domain.requests.UpdateSaldoRequest;
import com.sanedge.paymentgateway.saldo.domain.requests.UpdateSaldoWithdraw;
import com.sanedge.paymentgateway.saldo.domain.responses.SaldoResponse;
import com.sanedge.paymentgateway.saldo.domain.responses.SaldoResponseDeleteAt;

public interface SaldoCommandService {

    ApiResponse<SaldoResponse> create(CreateSaldoRequest request);

    ApiResponse<SaldoResponse> update(UpdateSaldoRequest request);

    ApiResponse<SaldoResponse> updateBalance(UpdateSaldoBalance request);

    ApiResponse<SaldoResponse> updateWithdraw(UpdateSaldoWithdraw request);

    ApiResponse<SaldoResponseDeleteAt> trash(Long id);

    ApiResponse<SaldoResponseDeleteAt> restore(Long id);

    ApiResponse<Boolean> delete(Long id);

    ApiResponse<Boolean> restoreAll();

    ApiResponse<Boolean> deleteAll();
}
