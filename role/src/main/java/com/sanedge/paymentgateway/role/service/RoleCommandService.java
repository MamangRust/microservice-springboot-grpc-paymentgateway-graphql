package com.sanedge.paymentgateway.role.service;

import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.role.domain.requests.CreateRoleRequest;
import com.sanedge.paymentgateway.role.domain.requests.UpdateRoleRequest;
import com.sanedge.paymentgateway.role.domain.responses.RoleResponse;
import com.sanedge.paymentgateway.role.domain.responses.RoleResponseDeleteAt;

public interface RoleCommandService {
    ApiResponse<RoleResponse> create(CreateRoleRequest request);

    ApiResponse<RoleResponse> update(UpdateRoleRequest request);

    ApiResponse<RoleResponseDeleteAt> trash(Long id);

    ApiResponse<RoleResponseDeleteAt> restore(Long id);

    ApiResponse<Boolean> delete(Long id);

    ApiResponse<Boolean> restoreAll();

    ApiResponse<Boolean> deleteAll();
}
