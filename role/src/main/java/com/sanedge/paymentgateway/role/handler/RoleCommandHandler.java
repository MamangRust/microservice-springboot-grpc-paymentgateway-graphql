package com.sanedge.paymentgateway.role.handler;

import org.springframework.stereotype.Service;

import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.grpc.GrpcErrorMapper;
import com.sanedge.paymentgateway.common.grpc.ProtoMappers;
import com.sanedge.paymentgateway.pb.role.ApiResponseRole;
import com.sanedge.paymentgateway.pb.role.ApiResponseRoleAll;
import com.sanedge.paymentgateway.pb.role.ApiResponseRoleDelete;
import com.sanedge.paymentgateway.pb.role.ApiResponseRoleDeleteAt;
import com.sanedge.paymentgateway.pb.role.CreateRoleRequest;
import com.sanedge.paymentgateway.pb.role.FindByIdRoleRequest;
import com.sanedge.paymentgateway.pb.role.RoleCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.role.RoleResponse;
import com.sanedge.paymentgateway.pb.role.RoleResponseDeleteAt;
import com.sanedge.paymentgateway.pb.role.UpdateRoleRequest;
import com.sanedge.paymentgateway.role.service.RoleCommandService;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleCommandHandler extends RoleCommandServiceGrpc.RoleCommandServiceImplBase {

    private final RoleCommandService roleCommandService;

    @Override
    public void createRole(CreateRoleRequest request, StreamObserver<ApiResponseRole> observer) {
        try {
            com.sanedge.paymentgateway.role.domain.requests.CreateRoleRequest req = new com.sanedge.paymentgateway.role.domain.requests.CreateRoleRequest();
            req.setName(request.getName());

            ApiResponse<com.sanedge.paymentgateway.role.domain.responses.RoleResponse> res = roleCommandService.create(req);

            observer.onNext(ApiResponseRole.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setData(RoleResponse.newBuilder()
                            .setId(res.getData().getId() == null ? 0 : res.getData().getId().intValue())
                            .setName(ProtoMappers.nvl(res.getData().getName()))
                            .setCreatedAt(ProtoMappers.nvl(res.getData().getCreatedAt()))
                            .setUpdatedAt(ProtoMappers.nvl(res.getData().getUpdatedAt()))
                            .build())
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void updateRole(UpdateRoleRequest request, StreamObserver<ApiResponseRole> observer) {
        try {
            com.sanedge.paymentgateway.role.domain.requests.UpdateRoleRequest req = new com.sanedge.paymentgateway.role.domain.requests.UpdateRoleRequest();
            req.setId((long) request.getId());
            req.setName(request.getName());

            ApiResponse<com.sanedge.paymentgateway.role.domain.responses.RoleResponse> res = roleCommandService.update(req);

            observer.onNext(ApiResponseRole.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setData(RoleResponse.newBuilder()
                            .setId(res.getData().getId() == null ? 0 : res.getData().getId().intValue())
                            .setName(ProtoMappers.nvl(res.getData().getName()))
                            .setCreatedAt(ProtoMappers.nvl(res.getData().getCreatedAt()))
                            .setUpdatedAt(ProtoMappers.nvl(res.getData().getUpdatedAt()))
                            .build())
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void trashedRole(FindByIdRoleRequest request, StreamObserver<ApiResponseRoleDeleteAt> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.role.domain.responses.RoleResponseDeleteAt> res = roleCommandService
                    .trash((long) request.getRoleId());
            observer.onNext(toDeleteAt(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreRole(FindByIdRoleRequest request, StreamObserver<ApiResponseRoleDeleteAt> observer) {
        try {
            ApiResponse<com.sanedge.paymentgateway.role.domain.responses.RoleResponseDeleteAt> res = roleCommandService
                    .restore((long) request.getRoleId());
            observer.onNext(toDeleteAt(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteRolePermanent(FindByIdRoleRequest request, StreamObserver<ApiResponseRoleDelete> observer) {
        try {
            ApiResponse<Boolean> res = roleCommandService.delete((long) request.getRoleId());
            observer.onNext(ApiResponseRoleDelete.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreAllRole(Empty request, StreamObserver<ApiResponseRoleAll> observer) {
        try {
            ApiResponse<Boolean> res = roleCommandService.restoreAll();
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteAllRolePermanent(Empty request, StreamObserver<ApiResponseRoleAll> observer) {
        try {
            ApiResponse<Boolean> res = roleCommandService.deleteAll();
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private ApiResponseRoleDeleteAt toDeleteAt(
            ApiResponse<com.sanedge.paymentgateway.role.domain.responses.RoleResponseDeleteAt> res) {
        RoleResponseDeleteAt data = RoleResponseDeleteAt.newBuilder()
                .setId(res.getData().getId() == null ? 0 : res.getData().getId().intValue())
                .setName(ProtoMappers.nvl(res.getData().getName()))
                .setCreatedAt(ProtoMappers.nvl(res.getData().getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(res.getData().getUpdatedAt()))
                .setDeletedAt(StringValue.of(ProtoMappers.nvl(res.getData().getDeletedAt())))
                .build();
        return ApiResponseRoleDeleteAt.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()))
                .setData(data)
                .build();
    }

    private ApiResponseRoleAll toAll(ApiResponse<Boolean> res) {
        return ApiResponseRoleAll.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()))
                .build();
    }
}
