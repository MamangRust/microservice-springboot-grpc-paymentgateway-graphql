package com.sanedge.paymentgateway.transaction.handler;

import java.util.List;

import org.springframework.stereotype.Service;

import com.google.protobuf.StringValue;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.paymentgateway.common.grpc.GrpcErrorMapper;
import com.sanedge.paymentgateway.common.grpc.ProtoMappers;
import com.sanedge.paymentgateway.pb.transaction.ApiResponsePaginationTransaction;
import com.sanedge.paymentgateway.pb.transaction.ApiResponsePaginationTransactionDeleteAt;
import com.sanedge.paymentgateway.pb.transaction.ApiResponseTransaction;
import com.sanedge.paymentgateway.pb.transaction.ApiResponseTransactions;
import com.sanedge.paymentgateway.pb.transaction.FindAllTransactionCardNumberRequest;
import com.sanedge.paymentgateway.pb.transaction.FindAllTransactionRequest;
import com.sanedge.paymentgateway.pb.transaction.FindByIdTransactionRequest;
import com.sanedge.paymentgateway.pb.transaction.FindTransactionByMerchantIdRequest;
import com.sanedge.paymentgateway.pb.transaction.TransactionQueryServiceGrpc;
import com.sanedge.paymentgateway.pb.transaction.TransactionResponse;
import com.sanedge.paymentgateway.pb.transaction.TransactionResponseDeleteAt;
import com.sanedge.paymentgateway.transaction.domain.requests.FindAllTransactionCardNumber;
import com.sanedge.paymentgateway.transaction.domain.requests.FindAllTransactions;
import com.sanedge.paymentgateway.transaction.service.TransactionQueryService;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionQueryHandler extends TransactionQueryServiceGrpc.TransactionQueryServiceImplBase {

    private final TransactionQueryService transactionQueryService;

    @Override
    public void findAllTransaction(FindAllTransactionRequest request,
            StreamObserver<ApiResponsePaginationTransaction> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.paymentgateway.transaction.domain.responses.TransactionResponse>> res = transactionQueryService
                    .findAll(toFindAll(request));

            ApiResponsePaginationTransaction.Builder builder = ApiResponsePaginationTransaction.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            res.getData().forEach(r -> builder.addData(toProto(r)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findAllTransactionByCardNumber(FindAllTransactionCardNumberRequest request,
            StreamObserver<ApiResponsePaginationTransaction> observer) {
        try {
            FindAllTransactionCardNumber req = new FindAllTransactionCardNumber();
            req.setCardNumber(request.getCardNumber());
            req.setPage(request.getPage());
            req.setPageSize(request.getPageSize());
            req.setSearch(request.getSearch());

            ApiResponsePagination<List<com.sanedge.paymentgateway.transaction.domain.responses.TransactionResponse>> res = transactionQueryService
                    .findAllByCardNumber(req);

            ApiResponsePaginationTransaction.Builder builder = ApiResponsePaginationTransaction.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            res.getData().forEach(r -> builder.addData(toProto(r)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findByIdTransaction(FindByIdTransactionRequest request, StreamObserver<ApiResponseTransaction> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.transaction.domain.responses.TransactionResponse> res = transactionQueryService
                    .findById((long) request.getTransactionId());

            observer.onNext(ApiResponseTransaction.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setData(toProto(res.getData()))
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findTransactionByMerchantId(FindTransactionByMerchantIdRequest request,
            StreamObserver<ApiResponseTransactions> observer) {
        try {
            ApiResponse<List<com.sanedge.paymentgateway.transaction.domain.responses.TransactionResponse>> res = transactionQueryService
                    .findByMerchantId((long) request.getMerchantId());

            ApiResponseTransactions.Builder builder = ApiResponseTransactions.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            res.getData().forEach(r -> builder.addData(toProto(r)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findByActiveTransaction(FindAllTransactionRequest request,
            StreamObserver<ApiResponsePaginationTransactionDeleteAt> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.paymentgateway.transaction.domain.responses.TransactionResponseDeleteAt>> res = transactionQueryService
                    .findByActive(toFindAll(request));

            ApiResponsePaginationTransactionDeleteAt.Builder builder = ApiResponsePaginationTransactionDeleteAt
                    .newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            res.getData().forEach(r -> builder.addData(toProtoDeleteAt(r)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findByTrashedTransaction(FindAllTransactionRequest request,
            StreamObserver<ApiResponsePaginationTransactionDeleteAt> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.paymentgateway.transaction.domain.responses.TransactionResponseDeleteAt>> res = transactionQueryService
                    .findByTrashed(toFindAll(request));

            ApiResponsePaginationTransactionDeleteAt.Builder builder = ApiResponsePaginationTransactionDeleteAt
                    .newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            res.getData().forEach(r -> builder.addData(toProtoDeleteAt(r)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private FindAllTransactions toFindAll(FindAllTransactionRequest request) {
        FindAllTransactions req = new FindAllTransactions();
        req.setPage(request.getPage());
        req.setPageSize(request.getPageSize());
        req.setSearch(request.getSearch());
        return req;
    }

    private TransactionResponse toProto(
            com.sanedge.paymentgateway.transaction.domain.responses.TransactionResponse r) {
        if (r == null) {
            return TransactionResponse.getDefaultInstance();
        }
        return TransactionResponse.newBuilder()
                .setId(r.getId() == null ? 0 : r.getId().intValue())
                .setCardNumber(ProtoMappers.nvl(r.getCardNumber()))
                .setTransactionNo(ProtoMappers.nvl(r.getTransactionNo()))
                .setAmount(r.getAmount() == null ? 0 : r.getAmount().intValue())
                .setPaymentMethod(ProtoMappers.nvl(r.getPaymentMethod()))
                .setMerchantId(r.getMerchantId() == null ? 0 : r.getMerchantId().intValue())
                .setTransactionTime(ProtoMappers.nvl(r.getTransactionTime()))
                .setCreatedAt(ProtoMappers.nvl(r.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(r.getUpdatedAt()))
                .build();
    }

    private TransactionResponseDeleteAt toProtoDeleteAt(
            com.sanedge.paymentgateway.transaction.domain.responses.TransactionResponseDeleteAt r) {
        return TransactionResponseDeleteAt.newBuilder()
                .setId(r.getId() == null ? 0 : r.getId().intValue())
                .setCardNumber(ProtoMappers.nvl(r.getCardNumber()))
                .setTransactionNo(ProtoMappers.nvl(r.getTransactionNo()))
                .setAmount(r.getAmount() == null ? 0 : r.getAmount().intValue())
                .setPaymentMethod(ProtoMappers.nvl(r.getPaymentMethod()))
                .setMerchantId(r.getMerchantId() == null ? 0 : r.getMerchantId().intValue())
                .setTransactionTime(ProtoMappers.nvl(r.getTransactionTime()))
                .setCreatedAt(ProtoMappers.nvl(r.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(r.getUpdatedAt()))
                .setDeletedAt(StringValue.of(ProtoMappers.nvl(r.getDeletedAt())))
                .build();
    }
}
