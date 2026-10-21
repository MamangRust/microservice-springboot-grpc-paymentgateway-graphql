package com.sanedge.paymentgateway.topup.handler;

import org.springframework.stereotype.Service;

import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.grpc.GrpcErrorMapper;
import com.sanedge.paymentgateway.common.grpc.ProtoMappers;
import com.sanedge.paymentgateway.pb.topup.ApiResponseTopup;
import com.sanedge.paymentgateway.pb.topup.ApiResponseTopupAll;
import com.sanedge.paymentgateway.pb.topup.ApiResponseTopupDelete;
import com.sanedge.paymentgateway.pb.topup.ApiResponseTopupDeleteAt;
import com.sanedge.paymentgateway.pb.topup.CreateTopupRequest;
import com.sanedge.paymentgateway.pb.topup.FindByIdTopupRequest;
import com.sanedge.paymentgateway.pb.topup.TopupCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.topup.TopupResponse;
import com.sanedge.paymentgateway.pb.topup.TopupResponseDeleteAt;
import com.sanedge.paymentgateway.pb.topup.UpdateTopupRequest;
import com.sanedge.paymentgateway.topup.service.TopupCommandService;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TopupCommandHandler extends TopupCommandServiceGrpc.TopupCommandServiceImplBase {

    private final TopupCommandService topupCommandService;

    @Override
    public void createTopup(CreateTopupRequest request, StreamObserver<ApiResponseTopup> observer) {
        try {
            com.sanedge.paymentgateway.topup.domain.requests.CreateTopupRequest req = new com.sanedge.paymentgateway.topup.domain.requests.CreateTopupRequest();
            req.setCardNumber(request.getCardNumber());
            req.setTopupAmount((long) request.getTopupAmount());
            req.setTopupMethod(request.getTopupMethod());

            ApiResponse<com.sanedge.paymentgateway.topup.domain.responses.TopupResponse> res = topupCommandService
                    .create(req);

            ApiResponseTopup.Builder builder = ApiResponseTopup.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null) {
                builder.setData(toProto(res.getData()));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void updateTopup(UpdateTopupRequest request, StreamObserver<ApiResponseTopup> observer) {
        try {
            com.sanedge.paymentgateway.topup.domain.requests.UpdateTopupRequest req = new com.sanedge.paymentgateway.topup.domain.requests.UpdateTopupRequest();
            req.setTopupId((long) request.getTopupId());
            req.setCardNumber(request.getCardNumber());
            req.setTopupAmount((long) request.getTopupAmount());
            req.setTopupMethod(request.getTopupMethod());

            ApiResponse<com.sanedge.paymentgateway.topup.domain.responses.TopupResponse> res = topupCommandService
                    .update(req);

            ApiResponseTopup.Builder builder = ApiResponseTopup.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null) {
                builder.setData(toProto(res.getData()));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void trashedTopup(FindByIdTopupRequest request, StreamObserver<ApiResponseTopupDeleteAt> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.topup.domain.responses.TopupResponseDeleteAt> res = topupCommandService
                    .trashed((long) request.getTopupId());

            ApiResponseTopupDeleteAt.Builder builder = ApiResponseTopupDeleteAt.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null) {
                builder.setData(toProtoDeleteAt(res.getData()));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreTopup(FindByIdTopupRequest request, StreamObserver<ApiResponseTopupDeleteAt> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.topup.domain.responses.TopupResponseDeleteAt> res = topupCommandService
                    .restore((long) request.getTopupId());

            ApiResponseTopupDeleteAt.Builder builder = ApiResponseTopupDeleteAt.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null) {
                builder.setData(toProtoDeleteAt(res.getData()));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteTopupPermanent(FindByIdTopupRequest request, StreamObserver<ApiResponseTopupDelete> observer) {
        try {
            ApiResponse<Boolean> res = topupCommandService.deletePermanent((long) request.getTopupId());
            observer.onNext(ApiResponseTopupDelete.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreAllTopup(Empty request, StreamObserver<ApiResponseTopupAll> observer) {
        try {
            ApiResponse<Boolean> res = topupCommandService.restoreAll();
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteAllTopupPermanent(Empty request, StreamObserver<ApiResponseTopupAll> observer) {
        try {
            ApiResponse<Boolean> res = topupCommandService.deleteAll();
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private ApiResponseTopupAll toAll(ApiResponse<Boolean> res) {
        return ApiResponseTopupAll.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()))
                .build();
    }

    static TopupResponse toProto(com.sanedge.paymentgateway.topup.domain.responses.TopupResponse r) {
        return TopupResponse.newBuilder()
                .setId(r.getId() == null ? 0 : r.getId().intValue())
                .setCardNumber(ProtoMappers.nvl(r.getCardNumber()))
                .setTopupNo(ProtoMappers.nvl(r.getTopupNo()))
                .setTopupAmount(r.getTopupAmount() == null ? 0 : r.getTopupAmount().intValue())
                .setTopupMethod(ProtoMappers.nvl(r.getTopupMethod()))
                .setTopupTime(ProtoMappers.nvl(r.getTopupTime()))
                .setCreatedAt(ProtoMappers.nvl(r.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(r.getUpdatedAt()))
                .build();
    }

    static TopupResponseDeleteAt toProtoDeleteAt(
            com.sanedge.paymentgateway.topup.domain.responses.TopupResponseDeleteAt r) {
        return TopupResponseDeleteAt.newBuilder()
                .setId(r.getId() == null ? 0 : r.getId().intValue())
                .setCardNumber(ProtoMappers.nvl(r.getCardNumber()))
                .setTopupNo(ProtoMappers.nvl(r.getTopupNo()))
                .setTopupAmount(r.getTopupAmount() == null ? 0 : r.getTopupAmount().intValue())
                .setTopupMethod(ProtoMappers.nvl(r.getTopupMethod()))
                .setTopupTime(ProtoMappers.nvl(r.getTopupTime()))
                .setCreatedAt(ProtoMappers.nvl(r.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(r.getUpdatedAt()))
                .setDeletedAt(StringValue.of(ProtoMappers.nvl(r.getDeletedAt())))
                .build();
    }
}
