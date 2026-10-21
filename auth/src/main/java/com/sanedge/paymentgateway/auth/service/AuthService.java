package com.sanedge.paymentgateway.auth.service;

import com.sanedge.paymentgateway.auth.domain.requests.AuthRequest;
import com.sanedge.paymentgateway.auth.domain.requests.RegisterRequest;
import com.sanedge.paymentgateway.auth.domain.responses.TokenResponse;
import com.sanedge.paymentgateway.auth.domain.responses.UserResponse;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;

public interface AuthService {
    ApiResponse<TokenResponse> login(AuthRequest loginRequest);

    ApiResponse<UserResponse> register(RegisterRequest registerRequest);

    ApiResponse<Void> forgotPassword(String email);

    ApiResponse<TokenResponse> refreshToken(String refreshToken);

    ApiResponse<UserResponse> getCurrentUser(Integer userId);
}
