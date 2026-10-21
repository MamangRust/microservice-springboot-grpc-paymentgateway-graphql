package com.sanedge.paymentgateway.user.service;

import java.util.List;

import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.paymentgateway.user.domain.requests.FindAllUserRequest;
import com.sanedge.paymentgateway.user.domain.responses.UserResponse;
import com.sanedge.paymentgateway.user.domain.responses.UserResponseDeleteAt;

public interface UserQueryService {

    ApiResponsePagination<List<UserResponse>> findAll(FindAllUserRequest req);

    ApiResponse<UserResponse> findById(Long userId);

    ApiResponse<UserResponse> findByEmail(String email);

    ApiResponsePagination<List<UserResponseDeleteAt>> findByActive(FindAllUserRequest req);

    ApiResponsePagination<List<UserResponseDeleteAt>> findByTrashed(FindAllUserRequest req);
}
