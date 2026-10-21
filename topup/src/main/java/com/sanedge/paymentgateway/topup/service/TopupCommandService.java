package com.sanedge.paymentgateway.topup.service;

import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.topup.domain.requests.CreateTopupRequest;
import com.sanedge.paymentgateway.topup.domain.requests.UpdateTopupRequest;
import com.sanedge.paymentgateway.topup.domain.responses.TopupResponse;
import com.sanedge.paymentgateway.topup.domain.responses.TopupResponseDeleteAt;

public interface TopupCommandService {

    ApiResponse<TopupResponse> create(CreateTopupRequest req);

    ApiResponse<TopupResponse> update(UpdateTopupRequest req);

    ApiResponse<TopupResponseDeleteAt> trashed(Long topupId);

    ApiResponse<TopupResponseDeleteAt> restore(Long topupId);

    ApiResponse<Boolean> deletePermanent(Long topupId);

    ApiResponse<Boolean> restoreAll();

    ApiResponse<Boolean> deleteAll();
}
