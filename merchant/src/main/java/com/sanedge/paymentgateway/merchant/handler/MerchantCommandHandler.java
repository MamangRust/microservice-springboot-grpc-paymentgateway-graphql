package com.sanedge.paymentgateway.merchant.handler;

import org.springframework.stereotype.Service;

import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.grpc.GrpcErrorMapper;
import com.sanedge.paymentgateway.common.grpc.ProtoMappers;
import com.sanedge.paymentgateway.merchant.service.MerchantCommandService;
import com.sanedge.paymentgateway.pb.merchant.ApiResponseMerchant;
import com.sanedge.paymentgateway.pb.merchant.ApiResponseMerchantAll;
import com.sanedge.paymentgateway.pb.merchant.ApiResponseMerchantDelete;
import com.sanedge.paymentgateway.pb.merchant.ApiResponseMerchantDeleteAt;
import com.sanedge.paymentgateway.pb.merchant.CreateMerchantRequest;
import com.sanedge.paymentgateway.pb.merchant.FindByIdMerchantRequest;
import com.sanedge.paymentgateway.pb.merchant.MerchantCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.merchant.MerchantResponse;
import com.sanedge.paymentgateway.pb.merchant.MerchantResponseDeleteAt;
import com.sanedge.paymentgateway.pb.merchant.UpdateMerchantRequest;
import com.sanedge.paymentgateway.pb.merchant.UpdateMerchantStatusRequest;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MerchantCommandHandler extends MerchantCommandServiceGrpc.MerchantCommandServiceImplBase {

    private final MerchantCommandService merchantCommandService;

    @Override
    public void createMerchant(CreateMerchantRequest request, StreamObserver<ApiResponseMerchant> observer) {
        try {
            com.sanedge.paymentgateway.merchant.domain.requests.CreateMerchantRequest req = new com.sanedge.paymentgateway.merchant.domain.requests.CreateMerchantRequest();
            req.setName(request.getName());
            req.setUserId((long) request.getUserId());

            ApiResponse<com.sanedge.paymentgateway.merchant.domain.responses.MerchantResponse> res = merchantCommandService
                    .createMerchant(req);

            observer.onNext(ApiResponseMerchant.newBuilder()
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
    public void updateMerchant(UpdateMerchantRequest request, StreamObserver<ApiResponseMerchant> observer) {
        try {
            com.sanedge.paymentgateway.merchant.domain.requests.UpdateMerchantRequest req = new com.sanedge.paymentgateway.merchant.domain.requests.UpdateMerchantRequest();
            req.setMerchantId((long) request.getMerchantId());
            req.setName(request.getName());
            req.setUserId((long) request.getUserId());
            req.setStatus(request.getStatus());

            ApiResponse<com.sanedge.paymentgateway.merchant.domain.responses.MerchantResponse> res = merchantCommandService
                    .updateMerchant(req);

            observer.onNext(ApiResponseMerchant.newBuilder()
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
    public void updateMerchantStatus(UpdateMerchantStatusRequest request,
            StreamObserver<ApiResponseMerchant> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.merchant.domain.responses.MerchantResponse> res = merchantCommandService
                    .updateMerchantStatus((int) request.getMerchantId(), request.getStatus());

            observer.onNext(ApiResponseMerchant.newBuilder()
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
    public void trashedMerchant(FindByIdMerchantRequest request, StreamObserver<ApiResponseMerchantDeleteAt> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.merchant.domain.responses.MerchantResponseDeleteAt> res = merchantCommandService
                    .trashMerchant((long) request.getMerchantId());
            observer.onNext(toDeleteAt(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreMerchant(FindByIdMerchantRequest request, StreamObserver<ApiResponseMerchantDeleteAt> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.merchant.domain.responses.MerchantResponseDeleteAt> res = merchantCommandService
                    .restoreMerchant((long) request.getMerchantId());
            observer.onNext(toDeleteAt(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteMerchantPermanent(FindByIdMerchantRequest request,
            StreamObserver<ApiResponseMerchantDelete> observer) {
        try {
            ApiResponse<Boolean> res = merchantCommandService.deleteMerchant((long) request.getMerchantId());
            observer.onNext(ApiResponseMerchantDelete.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreAllMerchant(Empty request, StreamObserver<ApiResponseMerchantAll> observer) {
        try {
            ApiResponse<Boolean> res = merchantCommandService.restoreAll();
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteAllMerchantPermanent(Empty request, StreamObserver<ApiResponseMerchantAll> observer) {
        try {
            ApiResponse<Boolean> res = merchantCommandService.deleteAll();
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private MerchantResponse toProto(com.sanedge.paymentgateway.merchant.domain.responses.MerchantResponse r) {
        if (r == null) {
            return MerchantResponse.getDefaultInstance();
        }
        return MerchantResponse.newBuilder()
                .setId(r.getId() == null ? 0 : r.getId().intValue())
                .setName(ProtoMappers.nvl(r.getName()))
                .setApiKey(ProtoMappers.nvl(r.getApiKey()))
                .setStatus(ProtoMappers.nvl(r.getStatus()))
                .setUserId(r.getUserId() == null ? 0 : r.getUserId().intValue())
                .setCreatedAt(ProtoMappers.nvl(r.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(r.getUpdatedAt()))
                .build();
    }

    private ApiResponseMerchantDeleteAt toDeleteAt(
            ApiResponse<com.sanedge.paymentgateway.merchant.domain.responses.MerchantResponseDeleteAt> res) {
        MerchantResponseDeleteAt data = MerchantResponseDeleteAt.newBuilder()
                .setId(res.getData().getId() == null ? 0 : res.getData().getId().intValue())
                .setName(ProtoMappers.nvl(res.getData().getName()))
                .setApiKey(ProtoMappers.nvl(res.getData().getApiKey()))
                .setStatus(ProtoMappers.nvl(res.getData().getStatus()))
                .setUserId(res.getData().getUserId() == null ? 0 : res.getData().getUserId().intValue())
                .setCreatedAt(ProtoMappers.nvl(res.getData().getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(res.getData().getUpdatedAt()))
                .setDeletedAt(StringValue.of(ProtoMappers.nvl(res.getData().getDeletedAt())))
                .build();
        return ApiResponseMerchantDeleteAt.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()))
                .setData(data)
                .build();
    }

    private ApiResponseMerchantAll toAll(ApiResponse<Boolean> res) {
        return ApiResponseMerchantAll.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()))
                .build();
    }
}
