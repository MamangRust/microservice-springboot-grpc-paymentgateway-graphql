package com.sanedge.paymentgateway.user.handler;

import org.springframework.stereotype.Service;

import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.grpc.GrpcErrorMapper;
import com.sanedge.paymentgateway.common.grpc.ProtoMappers;
import com.sanedge.paymentgateway.pb.user.ApiResponseUser;
import com.sanedge.paymentgateway.pb.user.ApiResponseUserAll;
import com.sanedge.paymentgateway.pb.user.ApiResponseUserDelete;
import com.sanedge.paymentgateway.pb.user.ApiResponseUserDeleteAt;
import com.sanedge.paymentgateway.pb.user.CreateUserRequest;
import com.sanedge.paymentgateway.pb.user.FindByIdUserRequest;
import com.sanedge.paymentgateway.pb.user.UpdateUserRequest;
import com.sanedge.paymentgateway.pb.user.UserCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.user.UserResponse;
import com.sanedge.paymentgateway.pb.user.UserResponseDeleteAt;
import com.sanedge.paymentgateway.pb.user.VerifyPasswordRequest;
import com.sanedge.paymentgateway.pb.user.VerifyPasswordResponse;
import com.sanedge.paymentgateway.user.service.UserCommandService;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserCommandHandler extends UserCommandServiceGrpc.UserCommandServiceImplBase {

    private final UserCommandService userCommandService;

    @Override
    public void create(CreateUserRequest request, StreamObserver<ApiResponseUser> observer) {
        try {
            com.sanedge.paymentgateway.user.domain.requests.CreateUserRequest req = new com.sanedge.paymentgateway.user.domain.requests.CreateUserRequest();
            req.setFirstname(request.getFirstname());
            req.setLastname(request.getLastname());
            req.setEmail(request.getEmail());
            req.setPassword(request.getPassword());
            req.setConfirmPassword(request.getConfirmPassword());

            ApiResponse<com.sanedge.paymentgateway.user.domain.responses.UserResponse> res = userCommandService
                    .create(req);

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
    public void update(UpdateUserRequest request, StreamObserver<ApiResponseUser> observer) {
        try {
            com.sanedge.paymentgateway.user.domain.requests.UpdateUserRequest req = new com.sanedge.paymentgateway.user.domain.requests.UpdateUserRequest();
            req.setId((long) request.getId());
            req.setFirstname(request.getFirstname());
            req.setLastname(request.getLastname());
            req.setEmail(request.getEmail());
            req.setPassword(request.getPassword());
            req.setConfirmPassword(request.getConfirmPassword());

            ApiResponse<com.sanedge.paymentgateway.user.domain.responses.UserResponse> res = userCommandService
                    .update(req);

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
    public void trashedUser(FindByIdUserRequest request, StreamObserver<ApiResponseUserDeleteAt> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.user.domain.responses.UserResponseDeleteAt> res = userCommandService
                    .trashed((long) request.getId());
            observer.onNext(toDeleteAt(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreUser(FindByIdUserRequest request, StreamObserver<ApiResponseUserDeleteAt> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.user.domain.responses.UserResponseDeleteAt> res = userCommandService
                    .restore((long) request.getId());
            observer.onNext(toDeleteAt(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteUserPermanent(FindByIdUserRequest request, StreamObserver<ApiResponseUserDelete> observer) {
        try {
            ApiResponse<Boolean> res = userCommandService.deletePermanent((long) request.getId());
            observer.onNext(ApiResponseUserDelete.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreAllUser(Empty request, StreamObserver<ApiResponseUserAll> observer) {
        try {
            ApiResponse<Boolean> res = userCommandService.restoreAll();
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteAllUserPermanent(Empty request, StreamObserver<ApiResponseUserAll> observer) {
        try {
            ApiResponse<Boolean> res = userCommandService.deleteAll();
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void verifyPassword(VerifyPasswordRequest request, StreamObserver<VerifyPasswordResponse> observer) {
        try {
            com.sanedge.paymentgateway.user.domain.responses.VerifyPasswordResponse res = userCommandService
                    .verifyPassword(request.getEmail(), request.getPassword());

            VerifyPasswordResponse.Builder builder = VerifyPasswordResponse.newBuilder()
                    .setValid(res.isValid());
            if (res.getUser() != null) {
                builder.setUser(toProto(res.getUser()));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
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

    private ApiResponseUserDeleteAt toDeleteAt(
            ApiResponse<com.sanedge.paymentgateway.user.domain.responses.UserResponseDeleteAt> res) {
        ApiResponseUserDeleteAt.Builder builder = ApiResponseUserDeleteAt.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()));
        if (res.getData() != null) {
            UserResponseDeleteAt data = UserResponseDeleteAt.newBuilder()
                    .setId(res.getData().getId() == null ? 0 : res.getData().getId().intValue())
                    .setFirstname(ProtoMappers.nvl(res.getData().getFirstname()))
                    .setLastname(ProtoMappers.nvl(res.getData().getLastname()))
                    .setEmail(ProtoMappers.nvl(res.getData().getEmail()))
                    .setCreatedAt(ProtoMappers.nvl(res.getData().getCreatedAt()))
                    .setUpdatedAt(ProtoMappers.nvl(res.getData().getUpdatedAt()))
                    .setDeletedAt(StringValue.of(ProtoMappers.nvl(res.getData().getDeletedAt())))
                    .build();
            builder.setData(data);
        }
        return builder.build();
    }

    private ApiResponseUserAll toAll(ApiResponse<Boolean> res) {
        return ApiResponseUserAll.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()))
                .build();
    }
}
