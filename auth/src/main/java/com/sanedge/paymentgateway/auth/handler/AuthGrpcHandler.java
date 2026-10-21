package com.sanedge.paymentgateway.auth.handler;

import org.springframework.stereotype.Service;

import com.sanedge.paymentgateway.auth.service.AuthService;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.grpc.GrpcErrorMapper;
import com.sanedge.paymentgateway.common.grpc.ProtoMappers;
import com.sanedge.paymentgateway.pb.ApiResponseForgotPassword;
import com.sanedge.paymentgateway.pb.ApiResponseGetMe;
import com.sanedge.paymentgateway.pb.ApiResponseLogin;
import com.sanedge.paymentgateway.pb.ApiResponseRefreshToken;
import com.sanedge.paymentgateway.pb.ApiResponseRegister;
import com.sanedge.paymentgateway.pb.AuthServiceGrpc;
import com.sanedge.paymentgateway.pb.ForgotPasswordRequest;
import com.sanedge.paymentgateway.pb.GetMeRequest;
import com.sanedge.paymentgateway.pb.LoginRequest;
import com.sanedge.paymentgateway.pb.RegisterRequest;
import com.sanedge.paymentgateway.pb.RefreshTokenRequest;
import com.sanedge.paymentgateway.pb.user.UserResponse;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

/**
 * Exposes the monolith's auth flows over gRPC. {@code RegisterUser},
 * {@code LoginUser}, {@code RefreshToken}, {@code GetMe} and {@code ForgotPassword}
 * are backed by {@link AuthService}; {@code VerifyCode} and {@code ResetPassword}
 * remain UNIMPLEMENTED (the proto declares them, but there is no domain logic
 * behind them yet).
 */
@Service
@RequiredArgsConstructor
public class AuthGrpcHandler extends AuthServiceGrpc.AuthServiceImplBase {

    private final AuthService authService;

    @Override
    public void registerUser(RegisterRequest request, StreamObserver<ApiResponseRegister> observer) {
        try {
            com.sanedge.paymentgateway.auth.domain.requests.RegisterRequest req = new com.sanedge.paymentgateway.auth.domain.requests.RegisterRequest();
            req.setFirstname(request.getFirstname());
            req.setLastname(request.getLastname());
            req.setEmail(request.getEmail());
            req.setPassword(request.getPassword());
            req.setConfirmPassword(request.getConfirmPassword());

            ApiResponse<com.sanedge.paymentgateway.auth.domain.responses.UserResponse> res = authService.register(req);

            ApiResponseRegister.Builder builder = ApiResponseRegister.newBuilder()
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
    public void forgotPassword(ForgotPasswordRequest request, StreamObserver<ApiResponseForgotPassword> observer) {
        try {
            ApiResponse<Void> res = authService.forgotPassword(request.getEmail());

            observer.onNext(ApiResponseForgotPassword.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void loginUser(LoginRequest request, StreamObserver<ApiResponseLogin> observer) {
        try {
            com.sanedge.paymentgateway.auth.domain.requests.AuthRequest req = new com.sanedge.paymentgateway.auth.domain.requests.AuthRequest();
            req.setEmail(request.getEmail());
            req.setPassword(request.getPassword());

            ApiResponse<com.sanedge.paymentgateway.auth.domain.responses.TokenResponse> res = authService.login(req);

            ApiResponseLogin.Builder builder = ApiResponseLogin.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null) {
                builder.setData(toProtoToken(res.getData()));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void refreshToken(RefreshTokenRequest request, StreamObserver<ApiResponseRefreshToken> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.auth.domain.responses.TokenResponse> res = authService
                    .refreshToken(request.getRefreshToken());

            ApiResponseRefreshToken.Builder builder = ApiResponseRefreshToken.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null) {
                builder.setData(toProtoToken(res.getData()));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void getMe(GetMeRequest request, StreamObserver<ApiResponseGetMe> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.auth.domain.responses.UserResponse> res = authService
                    .getCurrentUser(request.getUserId());

            ApiResponseGetMe.Builder builder = ApiResponseGetMe.newBuilder()
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

    private UserResponse toProto(com.sanedge.paymentgateway.auth.domain.responses.UserResponse u) {
        return UserResponse.newBuilder()
                .setId(u.getId() == null ? 0 : u.getId())
                .setFirstname(ProtoMappers.nvl(u.getFirstname()))
                .setLastname(ProtoMappers.nvl(u.getLastname()))
                .setEmail(ProtoMappers.nvl(u.getEmail()))
                .setCreatedAt(ProtoMappers.nvl(u.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(u.getUpdatedAt()))
                .build();
    }

    private com.sanedge.paymentgateway.pb.TokenResponse toProtoToken(
            com.sanedge.paymentgateway.auth.domain.responses.TokenResponse t) {
        return com.sanedge.paymentgateway.pb.TokenResponse.newBuilder()
                .setAccessToken(ProtoMappers.nvl(t.getAccess_token()))
                .setRefreshToken(ProtoMappers.nvl(t.getRefresh_token()))
                .build();
    }
}
