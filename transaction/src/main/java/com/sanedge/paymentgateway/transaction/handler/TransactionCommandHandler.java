package com.sanedge.paymentgateway.transaction.handler;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Service;

import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.grpc.GrpcErrorMapper;
import com.sanedge.paymentgateway.common.grpc.ProtoMappers;
import com.sanedge.paymentgateway.pb.transaction.ApiResponseTransaction;
import com.sanedge.paymentgateway.pb.transaction.ApiResponseTransactionAll;
import com.sanedge.paymentgateway.pb.transaction.ApiResponseTransactionDelete;
import com.sanedge.paymentgateway.pb.transaction.ApiResponseTransactionDeleteAt;
import com.sanedge.paymentgateway.pb.transaction.CreateTransactionRequest;
import com.sanedge.paymentgateway.pb.transaction.FindByIdTransactionRequest;
import com.sanedge.paymentgateway.pb.transaction.TransactionCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.transaction.TransactionResponse;
import com.sanedge.paymentgateway.pb.transaction.TransactionResponseDeleteAt;
import com.sanedge.paymentgateway.pb.transaction.UpdateTransactionRequest;
import com.sanedge.paymentgateway.transaction.service.TransactionCommandService;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionCommandHandler extends TransactionCommandServiceGrpc.TransactionCommandServiceImplBase {

    private final TransactionCommandService transactionCommandService;

    @Override
    public void createTransaction(CreateTransactionRequest request, StreamObserver<ApiResponseTransaction> observer) {
        try {
            com.sanedge.paymentgateway.transaction.domain.requests.CreateTransactionRequest req = new com.sanedge.paymentgateway.transaction.domain.requests.CreateTransactionRequest();
            req.setCardNumber(request.getCardNumber());
            req.setAmount((long) request.getAmount());
            req.setPaymentMethod(request.getPaymentMethod());
            req.setMerchantId((long) request.getMerchantId());
            if (request.hasTransactionTime()) {
                req.setTransactionTime(toLocalDateTime(request.getTransactionTime()));
            }

            ApiResponse<com.sanedge.paymentgateway.transaction.domain.responses.TransactionResponse> res = transactionCommandService
                    .create(request.getApiKey(), req);

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
    public void updateTransaction(UpdateTransactionRequest request, StreamObserver<ApiResponseTransaction> observer) {
        try {
            com.sanedge.paymentgateway.transaction.domain.requests.UpdateTransactionRequest req = new com.sanedge.paymentgateway.transaction.domain.requests.UpdateTransactionRequest();
            req.setTransactionId((long) request.getTransactionId());
            req.setCardNumber(request.getCardNumber());
            req.setAmount((long) request.getAmount());
            req.setPaymentMethod(request.getPaymentMethod());
            req.setMerchantId((long) request.getMerchantId());
            if (request.hasTransactionTime()) {
                req.setTransactionTime(toLocalDateTime(request.getTransactionTime()));
            }

            ApiResponse<com.sanedge.paymentgateway.transaction.domain.responses.TransactionResponse> res = transactionCommandService
                    .update(request.getApiKey(), req);

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
    public void trashedTransaction(FindByIdTransactionRequest request,
            StreamObserver<ApiResponseTransactionDeleteAt> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.transaction.domain.responses.TransactionResponseDeleteAt> res = transactionCommandService
                    .trashed((long) request.getTransactionId());
            observer.onNext(toDeleteAt(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreTransaction(FindByIdTransactionRequest request,
            StreamObserver<ApiResponseTransactionDeleteAt> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.transaction.domain.responses.TransactionResponseDeleteAt> res = transactionCommandService
                    .restore((long) request.getTransactionId());
            observer.onNext(toDeleteAt(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteTransactionPermanent(FindByIdTransactionRequest request,
            StreamObserver<ApiResponseTransactionDelete> observer) {
        try {
            ApiResponse<Boolean> res = transactionCommandService.deletePermanent((long) request.getTransactionId());
            observer.onNext(ApiResponseTransactionDelete.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreAllTransaction(Empty request, StreamObserver<ApiResponseTransactionAll> observer) {
        try {
            ApiResponse<Boolean> res = transactionCommandService.restoreAll();
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteAllTransactionPermanent(Empty request, StreamObserver<ApiResponseTransactionAll> observer) {
        try {
            ApiResponse<Boolean> res = transactionCommandService.deleteAll();
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private ApiResponseTransactionDeleteAt toDeleteAt(
            ApiResponse<com.sanedge.paymentgateway.transaction.domain.responses.TransactionResponseDeleteAt> res) {
        com.sanedge.paymentgateway.transaction.domain.responses.TransactionResponseDeleteAt d = res.getData();
        TransactionResponseDeleteAt data = TransactionResponseDeleteAt.newBuilder()
                .setId(d.getId() == null ? 0 : d.getId().intValue())
                .setCardNumber(ProtoMappers.nvl(d.getCardNumber()))
                .setTransactionNo(ProtoMappers.nvl(d.getTransactionNo()))
                .setAmount(d.getAmount() == null ? 0 : d.getAmount().intValue())
                .setPaymentMethod(ProtoMappers.nvl(d.getPaymentMethod()))
                .setMerchantId(d.getMerchantId() == null ? 0 : d.getMerchantId().intValue())
                .setTransactionTime(ProtoMappers.nvl(d.getTransactionTime()))
                .setCreatedAt(ProtoMappers.nvl(d.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(d.getUpdatedAt()))
                .setDeletedAt(StringValue.of(ProtoMappers.nvl(d.getDeletedAt())))
                .build();
        return ApiResponseTransactionDeleteAt.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()))
                .setData(data)
                .build();
    }

    private ApiResponseTransactionAll toAll(ApiResponse<Boolean> res) {
        return ApiResponseTransactionAll.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()))
                .build();
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

    private LocalDateTime toLocalDateTime(com.google.protobuf.Timestamp ts) {
        return LocalDateTime.ofEpochSecond(ts.getSeconds(), ts.getNanos(), ZoneOffset.UTC);
    }
}
