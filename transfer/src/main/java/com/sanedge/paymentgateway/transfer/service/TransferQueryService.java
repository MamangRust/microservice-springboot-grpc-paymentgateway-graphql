package com.sanedge.paymentgateway.transfer.service;

import java.util.List;

import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.paymentgateway.transfer.domain.requests.FindAllTransfers;
import com.sanedge.paymentgateway.transfer.domain.responses.TransferResponse;
import com.sanedge.paymentgateway.transfer.domain.responses.TransferResponseDeleteAt;

public interface TransferQueryService {

    ApiResponsePagination<List<TransferResponse>> findAll(FindAllTransfers req);

    ApiResponse<TransferResponse> findById(Long transferId);

    ApiResponsePagination<List<TransferResponseDeleteAt>> findByActive(FindAllTransfers req);

    ApiResponsePagination<List<TransferResponseDeleteAt>> findByTrashed(FindAllTransfers req);

    ApiResponse<List<TransferResponse>> findByTransferFrom(String transferFrom);

    ApiResponse<List<TransferResponse>> findByTransferTo(String transferTo);
}
