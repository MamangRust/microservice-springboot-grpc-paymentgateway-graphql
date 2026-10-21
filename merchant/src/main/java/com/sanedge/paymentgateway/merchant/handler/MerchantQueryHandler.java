package com.sanedge.paymentgateway.merchant.handler;

import java.util.List;

import org.springframework.stereotype.Service;

import com.google.protobuf.StringValue;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.paymentgateway.common.grpc.GrpcErrorMapper;
import com.sanedge.paymentgateway.common.grpc.ProtoMappers;
import com.sanedge.paymentgateway.merchant.domain.requests.FindAllMerchants;
import com.sanedge.paymentgateway.merchant.service.MerchantQueryService;
import com.sanedge.paymentgateway.pb.merchant.ApiResponseMerchant;
import com.sanedge.paymentgateway.pb.merchant.ApiResponsePaginationMerchant;
import com.sanedge.paymentgateway.pb.merchant.ApiResponsePaginationMerchantDeleteAt;
import com.sanedge.paymentgateway.pb.merchant.ApiResponsesMerchant;
import com.sanedge.paymentgateway.pb.merchant.FindAllMerchantRequest;
import com.sanedge.paymentgateway.pb.merchant.FindByApiKeyRequest;
import com.sanedge.paymentgateway.pb.merchant.FindByIdMerchantRequest;
import com.sanedge.paymentgateway.pb.merchant.FindByMerchantUserIdRequest;
import com.sanedge.paymentgateway.pb.merchant.MerchantQueryServiceGrpc;
import com.sanedge.paymentgateway.pb.merchant.MerchantResponse;
import com.sanedge.paymentgateway.pb.merchant.MerchantResponseDeleteAt;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MerchantQueryHandler extends MerchantQueryServiceGrpc.MerchantQueryServiceImplBase {

    private final MerchantQueryService merchantQueryService;

    @Override
    public void findAllMerchant(FindAllMerchantRequest request, StreamObserver<ApiResponsePaginationMerchant> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.paymentgateway.merchant.domain.responses.MerchantResponse>> res = merchantQueryService
                    .findAll(toFindAll(request));

            ApiResponsePaginationMerchant.Builder builder = ApiResponsePaginationMerchant.newBuilder()
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
    public void findByIdMerchant(FindByIdMerchantRequest request, StreamObserver<ApiResponseMerchant> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.merchant.domain.responses.MerchantResponse> res = merchantQueryService
                    .findById((long) request.getMerchantId());

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
    public void findByApiKey(FindByApiKeyRequest request, StreamObserver<ApiResponseMerchant> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.merchant.domain.responses.MerchantResponse> res = merchantQueryService
                    .findByApiKey(request.getApiKey());

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
    public void findByMerchantUserId(FindByMerchantUserIdRequest request,
            StreamObserver<ApiResponsesMerchant> observer) {
        try {
            ApiResponse<List<com.sanedge.paymentgateway.merchant.domain.responses.MerchantResponse>> res = merchantQueryService
                    .findByUserId((long) request.getUserId());

            ApiResponsesMerchant.Builder builder = ApiResponsesMerchant.newBuilder()
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
    public void findByActive(FindAllMerchantRequest request,
            StreamObserver<ApiResponsePaginationMerchantDeleteAt> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.paymentgateway.merchant.domain.responses.MerchantResponseDeleteAt>> res = merchantQueryService
                    .findByActive(toFindAll(request));

            ApiResponsePaginationMerchantDeleteAt.Builder builder = ApiResponsePaginationMerchantDeleteAt.newBuilder()
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
    public void findByTrashed(FindAllMerchantRequest request,
            StreamObserver<ApiResponsePaginationMerchantDeleteAt> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.paymentgateway.merchant.domain.responses.MerchantResponseDeleteAt>> res = merchantQueryService
                    .findByTrashed(toFindAll(request));

            ApiResponsePaginationMerchantDeleteAt.Builder builder = ApiResponsePaginationMerchantDeleteAt.newBuilder()
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

    private FindAllMerchants toFindAll(FindAllMerchantRequest request) {
        FindAllMerchants req = new FindAllMerchants();
        req.setPage(request.getPage());
        req.setPageSize(request.getPageSize());
        req.setSearch(request.getSearch());
        return req;
    }

    private MerchantResponse toProto(com.sanedge.paymentgateway.merchant.domain.responses.MerchantResponse r) {
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

    private MerchantResponseDeleteAt toProtoDeleteAt(
            com.sanedge.paymentgateway.merchant.domain.responses.MerchantResponseDeleteAt r) {
        return MerchantResponseDeleteAt.newBuilder()
                .setId(r.getId() == null ? 0 : r.getId().intValue())
                .setName(ProtoMappers.nvl(r.getName()))
                .setApiKey(ProtoMappers.nvl(r.getApiKey()))
                .setStatus(ProtoMappers.nvl(r.getStatus()))
                .setUserId(r.getUserId() == null ? 0 : r.getUserId().intValue())
                .setCreatedAt(ProtoMappers.nvl(r.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(r.getUpdatedAt()))
                .setDeletedAt(StringValue.of(ProtoMappers.nvl(r.getDeletedAt())))
                .build();
    }
}
