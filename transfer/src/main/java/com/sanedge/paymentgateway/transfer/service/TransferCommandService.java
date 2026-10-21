package com.sanedge.paymentgateway.transfer.service;

import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.transfer.domain.requests.CreateTransferRequest;
import com.sanedge.paymentgateway.transfer.domain.requests.UpdateTransferRequest;
import com.sanedge.paymentgateway.transfer.domain.responses.TransferResponse;
import com.sanedge.paymentgateway.transfer.domain.responses.TransferResponseDeleteAt;

public interface TransferCommandService {

    ApiResponse<TransferResponse> create(CreateTransferRequest req);

    ApiResponse<TransferResponse> update(UpdateTransferRequest req);

    ApiResponse<TransferResponseDeleteAt> trashed(Long transferId);

    ApiResponse<TransferResponseDeleteAt> restore(Long transferId);

    ApiResponse<Boolean> deletePermanent(Long transferId);

    ApiResponse<Boolean> restoreAll();

    ApiResponse<Boolean> deleteAll();
}
