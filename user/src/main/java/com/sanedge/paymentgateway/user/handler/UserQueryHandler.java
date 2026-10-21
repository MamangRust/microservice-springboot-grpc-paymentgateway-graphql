package com.sanedge.paymentgateway.user.handler;

import java.util.List;

import org.springframework.stereotype.Service;

import com.google.protobuf.StringValue;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.paymentgateway.common.grpc.GrpcErrorMapper;
import com.sanedge.paymentgateway.common.grpc.ProtoMappers;
import com.sanedge.paymentgateway.pb.user.ApiResponsePaginationUser;
import com.sanedge.paymentgateway.pb.user.ApiResponsePaginationUserDeleteAt;
import com.sanedge.paymentgateway.pb.user.ApiResponseUser;
import com.sanedge.paymentgateway.pb.user.FindAllUserRequest;
import com.sanedge.paymentgateway.pb.user.FindByEmailRequest;
import com.sanedge.paymentgateway.pb.user.FindByIdUserRequest;
import com.sanedge.paymentgateway.pb.user.UserQueryServiceGrpc;
import com.sanedge.paymentgateway.pb.user.UserResponse;
import com.sanedge.paymentgateway.pb.user.UserResponseDeleteAt;
import com.sanedge.paymentgateway.user.service.UserQueryService;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserQueryHandler extends UserQueryServiceGrpc.UserQueryServiceImplBase {

    private final UserQueryService userQueryService;

    @Override
    public void findAll(FindAllUserRequest request, StreamObserver<ApiResponsePaginationUser> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.paymentgateway.user.domain.responses.UserResponse>> res = userQueryService
                    .findAll(toFindAll(request));

            ApiResponsePaginationUser.Builder builder = ApiResponsePaginationUser.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            res.getData().forEach(u -> builder.addData(toProto(u)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findById(FindByIdUserRequest request, StreamObserver<ApiResponseUser> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.user.domain.responses.UserResponse> res = userQueryService
                    .findById((long) request.getId());

            ApiResponseUser.Builder builder = ApiResponseUser.newBuilder()
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
    public void findByEmail(FindByEmailRequest request, StreamObserver<ApiResponseUser> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.user.domain.responses.UserResponse> res = userQueryService
                    .findByEmail(request.getEmail());

            ApiResponseUser.Builder builder = ApiResponseUser.newBuilder()
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
    public void findByActive(FindAllUserRequest request, StreamObserver<ApiResponsePaginationUserDeleteAt> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.paymentgateway.user.domain.responses.UserResponseDeleteAt>> res = userQueryService
                    .findByActive(toFindAll(request));

            ApiResponsePaginationUserDeleteAt.Builder builder = ApiResponsePaginationUserDeleteAt.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            res.getData().forEach(u -> builder.addData(toProtoDeleteAt(u)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findByTrashed(FindAllUserRequest request, StreamObserver<ApiResponsePaginationUserDeleteAt> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.paymentgateway.user.domain.responses.UserResponseDeleteAt>> res = userQueryService
                    .findByTrashed(toFindAll(request));

            ApiResponsePaginationUserDeleteAt.Builder builder = ApiResponsePaginationUserDeleteAt.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            res.getData().forEach(u -> builder.addData(toProtoDeleteAt(u)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private com.sanedge.paymentgateway.user.domain.requests.FindAllUserRequest toFindAll(FindAllUserRequest request) {
        com.sanedge.paymentgateway.user.domain.requests.FindAllUserRequest req = new com.sanedge.paymentgateway.user.domain.requests.FindAllUserRequest();
        req.setPage(request.getPage());
        req.setPageSize(request.getPageSize());
        req.setSearch(request.getSearch());
        return req;
    }

    private UserResponse toProto(com.sanedge.paymentgateway.user.domain.responses.UserResponse u) {
        return UserResponse.newBuilder()
                .setId(u.getId() == null ? 0 : u.getId().intValue())
                .setFirstname(ProtoMappers.nvl(u.getFirstname()))
                .setLastname(ProtoMappers.nvl(u.getLastname()))
                .setEmail(ProtoMappers.nvl(u.getEmail()))
                .setCreatedAt(ProtoMappers.nvl(u.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(u.getUpdatedAt()))
                .build();
    }

    private UserResponseDeleteAt toProtoDeleteAt(
            com.sanedge.paymentgateway.user.domain.responses.UserResponseDeleteAt u) {
        return UserResponseDeleteAt.newBuilder()
                .setId(u.getId() == null ? 0 : u.getId().intValue())
                .setFirstname(ProtoMappers.nvl(u.getFirstname()))
                .setLastname(ProtoMappers.nvl(u.getLastname()))
                .setEmail(ProtoMappers.nvl(u.getEmail()))
                .setCreatedAt(ProtoMappers.nvl(u.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(u.getUpdatedAt()))
                .setDeletedAt(StringValue.of(ProtoMappers.nvl(u.getDeletedAt())))
                .build();
    }
}
