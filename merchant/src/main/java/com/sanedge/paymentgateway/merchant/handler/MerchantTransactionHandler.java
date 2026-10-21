package com.sanedge.paymentgateway.merchant.handler;

import java.util.List;

import org.springframework.stereotype.Service;

import com.google.protobuf.StringValue;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.paymentgateway.common.grpc.GrpcErrorMapper;
import com.sanedge.paymentgateway.common.grpc.ProtoMappers;
import com.sanedge.paymentgateway.merchant.domain.requests.FindAllMerchants;
import com.sanedge.paymentgateway.merchant.domain.requests.transactions.FindAllMerchantTransactionsByApiKey;
import com.sanedge.paymentgateway.merchant.domain.requests.transactions.FindAllMerchantTransactionsById;
import com.sanedge.paymentgateway.merchant.service.MerchantTransactionService;
import com.sanedge.paymentgateway.pb.merchant.ApiResponsePaginationMerchantTransaction;
import com.sanedge.paymentgateway.pb.merchant.FindAllMerchantTransaction;
import com.sanedge.paymentgateway.pb.merchant.FindAllMerchantTransactionApikey;
import com.sanedge.paymentgateway.pb.merchant.FindAllMerchantTransactionId;
import com.sanedge.paymentgateway.pb.merchant.MerchantTransactionResponse;
import com.sanedge.paymentgateway.pb.merchant.MerchantTransactionServiceGrpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MerchantTransactionHandler extends MerchantTransactionServiceGrpc.MerchantTransactionServiceImplBase {

    private final MerchantTransactionService merchantTransactionService;

    @Override
    public void findAllTransactionMerchant(FindAllMerchantTransaction request,
            StreamObserver<ApiResponsePaginationMerchantTransaction> observer) {
        try {
            FindAllMerchants req = new FindAllMerchants();
            req.setPage(request.getPage());
            req.setPageSize(request.getPageSize());
            req.setSearch(request.getSearch());

            ApiResponsePagination<List<com.sanedge.paymentgateway.merchant.domain.responses.MerchantTransactionResponse>> res = merchantTransactionService
                    .findAll(req);

            observer.onNext(toProto(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findAllTransactionByApikey(FindAllMerchantTransactionApikey request,
            StreamObserver<ApiResponsePaginationMerchantTransaction> observer) {
        try {
            FindAllMerchantTransactionsByApiKey req = new FindAllMerchantTransactionsByApiKey();
            req.setApiKey(request.getApiKey());
            req.setPage(request.getPage());
            req.setPageSize(request.getPageSize());
            req.setSearch(request.getSearch());

            ApiResponsePagination<List<com.sanedge.paymentgateway.merchant.domain.responses.MerchantTransactionResponse>> res = merchantTransactionService
                    .findByApiKey(req);

            observer.onNext(toProto(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findAllTransactionByMerchant(FindAllMerchantTransactionId request,
            StreamObserver<ApiResponsePaginationMerchantTransaction> observer) {
        try {
            FindAllMerchantTransactionsById req = new FindAllMerchantTransactionsById();
            req.setMerchantId(parseLong(request.getId()));
            req.setPage(request.getPage());
            req.setPageSize(request.getPageSize());
            req.setSearch(request.getSearch());

            ApiResponsePagination<List<com.sanedge.paymentgateway.merchant.domain.responses.MerchantTransactionResponse>> res = merchantTransactionService
                    .findById(req);

            observer.onNext(toProto(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private ApiResponsePaginationMerchantTransaction toProto(
            ApiResponsePagination<List<com.sanedge.paymentgateway.merchant.domain.responses.MerchantTransactionResponse>> res) {
        ApiResponsePaginationMerchantTransaction.Builder builder = ApiResponsePaginationMerchantTransaction.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()))
                .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
        res.getData().forEach(r -> builder.addData(toProto(r)));
        return builder.build();
    }

    private MerchantTransactionResponse toProto(
            com.sanedge.paymentgateway.merchant.domain.responses.MerchantTransactionResponse r) {
        return MerchantTransactionResponse.newBuilder()
                .setId(r.getId() == null ? 0 : r.getId().intValue())
                .setCardNumber(ProtoMappers.nvl(r.getCardNumber()))
                .setAmount(r.getAmount() == null ? 0 : r.getAmount().intValue())
                .setPaymentMethod(ProtoMappers.nvl(r.getPaymentMethod()))
                .setMerchantId(r.getMerchantId() == null ? 0 : r.getMerchantId().intValue())
                .setMerchantName(ProtoMappers.nvl(r.getMerchantName()))
                .setTransactionTime(ProtoMappers.nvl(r.getTransactionTime()))
                .setCreatedAt(ProtoMappers.nvl(r.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(r.getUpdatedAt()))
                .setDeletedAt(StringValue.of(ProtoMappers.nvl(r.getDeletedAt())))
                .build();
    }

    private Long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
