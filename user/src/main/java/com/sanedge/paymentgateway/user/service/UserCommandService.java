package com.sanedge.paymentgateway.user.service;

import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.user.domain.requests.CreateUserRequest;
import com.sanedge.paymentgateway.user.domain.requests.UpdateUserRequest;
import com.sanedge.paymentgateway.user.domain.responses.UserResponse;
import com.sanedge.paymentgateway.user.domain.responses.UserResponseDeleteAt;
import com.sanedge.paymentgateway.user.domain.responses.VerifyPasswordResponse;

public interface UserCommandService {

    ApiResponse<UserResponse> create(CreateUserRequest req);

    ApiResponse<UserResponse> update(UpdateUserRequest req);

    ApiResponse<UserResponseDeleteAt> trashed(Long userId);

    ApiResponse<UserResponseDeleteAt> restore(Long userId);

    ApiResponse<Boolean> deletePermanent(Long userId);

    ApiResponse<Boolean> restoreAll();

    ApiResponse<Boolean> deleteAll();

    VerifyPasswordResponse verifyPassword(String email, String password);
}
