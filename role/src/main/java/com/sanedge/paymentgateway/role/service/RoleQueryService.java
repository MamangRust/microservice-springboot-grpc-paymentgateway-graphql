package com.sanedge.paymentgateway.role.service;

import java.util.List;

import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.paymentgateway.role.domain.requests.FindAllRoles;
import com.sanedge.paymentgateway.role.domain.responses.RoleResponse;
import com.sanedge.paymentgateway.role.domain.responses.RoleResponseDeleteAt;

public interface RoleQueryService {
    ApiResponsePagination<List<RoleResponse>> findAll(FindAllRoles req);

    ApiResponsePagination<List<RoleResponseDeleteAt>> findByActive(FindAllRoles req);

    ApiResponsePagination<List<RoleResponseDeleteAt>> findByTrashed(FindAllRoles req);

    ApiResponse<RoleResponse> findById(Long id);

    ApiResponse<List<RoleResponse>> findByUserId(Long userId);

    ApiResponse<RoleResponse> findByName(String name);
}
