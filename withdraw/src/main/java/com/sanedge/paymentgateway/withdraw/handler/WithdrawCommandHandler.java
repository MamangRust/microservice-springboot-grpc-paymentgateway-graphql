package com.sanedge.paymentgateway.withdraw.handler;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Service;

import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import com.google.protobuf.Timestamp;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.grpc.GrpcErrorMapper;
import com.sanedge.paymentgateway.common.grpc.ProtoMappers;
import com.sanedge.paymentgateway.pb.withdraw.ApIResponseWithdrawDeleteAt;
import com.sanedge.paymentgateway.pb.withdraw.ApiResponseWithdraw;
import com.sanedge.paymentgateway.pb.withdraw.ApiResponseWithdrawAll;
import com.sanedge.paymentgateway.pb.withdraw.ApiResponseWithdrawDelete;
import com.sanedge.paymentgateway.pb.withdraw.CreateWithdrawRequest;
import com.sanedge.paymentgateway.pb.withdraw.FindByIdWithdrawRequest;
import com.sanedge.paymentgateway.pb.withdraw.UpdateWithdrawRequest;
import com.sanedge.paymentgateway.pb.withdraw.WithdrawCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.withdraw.WithdrawResponse;
import com.sanedge.paymentgateway.pb.withdraw.WithdrawResponseDeleteAt;
import com.sanedge.paymentgateway.withdraw.service.WithdrawCommandService;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WithdrawCommandHandler extends WithdrawCommandServiceGrpc.WithdrawCommandServiceImplBase {

    private final WithdrawCommandService withdrawCommandService;

    @Override
    public void createWithdraw(CreateWithdrawRequest request, StreamObserver<ApiResponseWithdraw> observer) {
        try {
            com.sanedge.paymentgateway.withdraw.domain.requests.CreateWithdrawRequest req = new com.sanedge.paymentgateway.withdraw.domain.requests.CreateWithdrawRequest();
            req.setCardNumber(request.getCardNumber());
            req.setWithdrawAmount((long) request.getWithdrawAmount());
            req.setWithdrawTime(toLocalDateTime(request.getWithdrawTime()));

            ApiResponse<com.sanedge.paymentgateway.withdraw.domain.responses.WithdrawResponse> res = withdrawCommandService
                    .create(req);

            observer.onNext(ApiResponseWithdraw.newBuilder()
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
    public void updateWithdraw(UpdateWithdrawRequest request, StreamObserver<ApiResponseWithdraw> observer) {
        try {
            com.sanedge.paymentgateway.withdraw.domain.requests.UpdateWithdrawRequest req = new com.sanedge.paymentgateway.withdraw.domain.requests.UpdateWithdrawRequest();
            req.setWithdrawId((long) request.getWithdrawId());
            req.setCardNumber(request.getCardNumber());
            req.setWithdrawAmount((long) request.getWithdrawAmount());
            req.setWithdrawTime(toLocalDateTime(request.getWithdrawTime()));

            ApiResponse<com.sanedge.paymentgateway.withdraw.domain.responses.WithdrawResponse> res = withdrawCommandService
                    .update(req);

            observer.onNext(ApiResponseWithdraw.newBuilder()
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
    public void trashedWithdraw(FindByIdWithdrawRequest request, StreamObserver<ApIResponseWithdrawDeleteAt> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.withdraw.domain.responses.WithdrawResponseDeleteAt> res = withdrawCommandService
                    .trashed((long) request.getWithdrawId());
            observer.onNext(toDeleteAt(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreWithdraw(FindByIdWithdrawRequest request, StreamObserver<ApIResponseWithdrawDeleteAt> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.withdraw.domain.responses.WithdrawResponseDeleteAt> res = withdrawCommandService
                    .restore((long) request.getWithdrawId());
            observer.onNext(toDeleteAt(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteWithdrawPermanent(FindByIdWithdrawRequest request,
            StreamObserver<ApiResponseWithdrawDelete> observer) {
        try {
            ApiResponse<Boolean> res = withdrawCommandService.deletePermanent((long) request.getWithdrawId());
            observer.onNext(ApiResponseWithdrawDelete.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreAllWithdraw(Empty request, StreamObserver<ApiResponseWithdrawAll> observer) {
        try {
            ApiResponse<Boolean> res = withdrawCommandService.restoreAll();
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteAllWithdrawPermanent(Empty request, StreamObserver<ApiResponseWithdrawAll> observer) {
        try {
            ApiResponse<Boolean> res = withdrawCommandService.deleteAll();
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private LocalDateTime toLocalDateTime(Timestamp ts) {
        if (ts == null || (ts.getSeconds() == 0 && ts.getNanos() == 0)) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos()), ZoneOffset.UTC);
    }

    private WithdrawResponse toProto(com.sanedge.paymentgateway.withdraw.domain.responses.WithdrawResponse r) {
        if (r == null) {
            return WithdrawResponse.getDefaultInstance();
        }
        return WithdrawResponse.newBuilder()
                .setWithdrawId(r.getId() == null ? 0 : r.getId().intValue())
                .setWithdrawNo(ProtoMappers.nvl(r.getWithdrawNo()))
                .setCardNumber(ProtoMappers.nvl(r.getCardNumber()))
                .setWithdrawAmount(r.getWithdrawAmount() == null ? 0 : r.getWithdrawAmount().intValue())
                .setWithdrawTime(ProtoMappers.nvl(r.getWithdrawTime()))
                .setCreatedAt(ProtoMappers.nvl(r.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(r.getUpdatedAt()))
                .build();
    }

    private WithdrawResponseDeleteAt toProtoDeleteAt(
            com.sanedge.paymentgateway.withdraw.domain.responses.WithdrawResponseDeleteAt r) {
        if (r == null) {
            return WithdrawResponseDeleteAt.getDefaultInstance();
        }
        return WithdrawResponseDeleteAt.newBuilder()
                .setWithdrawId(r.getId() == null ? 0 : r.getId().intValue())
                .setWithdrawNo(ProtoMappers.nvl(r.getWithdrawNo()))
                .setCardNumber(ProtoMappers.nvl(r.getCardNumber()))
                .setWithdrawAmount(r.getWithdrawAmount() == null ? 0 : r.getWithdrawAmount().intValue())
                .setWithdrawTime(ProtoMappers.nvl(r.getWithdrawTime()))
                .setCreatedAt(ProtoMappers.nvl(r.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(r.getUpdatedAt()))
                .setDeletedAt(StringValue.of(ProtoMappers.nvl(r.getDeletedAt())))
                .build();
    }

    private ApIResponseWithdrawDeleteAt toDeleteAt(
            ApiResponse<com.sanedge.paymentgateway.withdraw.domain.responses.WithdrawResponseDeleteAt> res) {
        return ApIResponseWithdrawDeleteAt.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()))
                .setData(toProtoDeleteAt(res.getData()))
                .build();
    }

    private ApiResponseWithdrawAll toAll(ApiResponse<Boolean> res) {
        return ApiResponseWithdrawAll.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()))
                .build();
    }
}
