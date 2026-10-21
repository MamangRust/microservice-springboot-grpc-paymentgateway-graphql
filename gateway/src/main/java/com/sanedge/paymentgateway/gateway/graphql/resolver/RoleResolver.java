package com.sanedge.paymentgateway.gateway.graphql.resolver;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.google.protobuf.Empty;
import com.sanedge.paymentgateway.common.grpc.ProtoMappers;
import com.sanedge.paymentgateway.gateway.dto.responses.role.RoleResponse;
import com.sanedge.paymentgateway.gateway.dto.responses.role.RoleResponseDeleteAt;
import com.sanedge.paymentgateway.gateway.graphql.Gql;
import com.sanedge.paymentgateway.gateway.graphql.dto.Page;
import com.sanedge.paymentgateway.gateway.mapper.ProtoMapper;
import com.sanedge.paymentgateway.pb.role.RoleCommandServiceGrpc;
import com.sanedge.paymentgateway.pb.role.RoleServiceGrpc;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class RoleResolver {

    private final RoleServiceGrpc.RoleServiceBlockingStub roleQueryStub;
    private final RoleCommandServiceGrpc.RoleCommandServiceBlockingStub roleCommandStub;

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Page<RoleResponse> roles(@Argument Integer page, @Argument Integer pageSize, @Argument String search) {
        var resp = roleQueryStub.findAllRole(com.sanedge.paymentgateway.pb.role.FindAllRoleRequest.newBuilder()
                .setPage(Gql.i(page)).setPageSize(Gql.i(pageSize)).setSearch(Gql.s(search)).build());
        return Page.of(ProtoMapper.roles(resp.getDataList()),
                ProtoMappers.fromProtoPagination(resp.getPaginationMeta()));
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public RoleResponse role(@Argument Long id) {
        var resp = roleQueryStub.findByIdRole(com.sanedge.paymentgateway.pb.role.FindByIdRoleRequest.newBuilder()
                .setRoleId(Gql.i(id)).build());
        return ProtoMapper.role(resp.getData());
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Page<RoleResponseDeleteAt> activeRoles(@Argument Integer page, @Argument Integer pageSize,
            @Argument String search) {
        var resp = roleQueryStub.findByActive(com.sanedge.paymentgateway.pb.role.FindAllRoleRequest.newBuilder()
                .setPage(Gql.i(page)).setPageSize(Gql.i(pageSize)).setSearch(Gql.s(search)).build());
        return Page.of(ProtoMapper.rolesDeleteAt(resp.getDataList()),
                ProtoMappers.fromProtoPagination(resp.getPaginationMeta()));
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Page<RoleResponseDeleteAt> trashedRoles(@Argument Integer page, @Argument Integer pageSize,
            @Argument String search) {
        var resp = roleQueryStub.findByTrashed(com.sanedge.paymentgateway.pb.role.FindAllRoleRequest.newBuilder()
                .setPage(Gql.i(page)).setPageSize(Gql.i(pageSize)).setSearch(Gql.s(search)).build());
        return Page.of(ProtoMapper.rolesDeleteAt(resp.getDataList()),
                ProtoMappers.fromProtoPagination(resp.getPaginationMeta()));
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public java.util.List<RoleResponse> rolesByUser(@Argument Long userId) {
        var resp = roleQueryStub.findByUserId(com.sanedge.paymentgateway.pb.role.FindByIdUserRoleRequest.newBuilder()
                .setUserId(Gql.i(userId)).build());
        return ProtoMapper.roles(resp.getDataList());
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public RoleResponse roleByName(@Argument String name) {
        var resp = roleQueryStub.findByNameRole(com.sanedge.paymentgateway.pb.role.FindByNameRoleRequest.newBuilder()
                .setName(Gql.s(name)).build());
        return ProtoMapper.role(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public RoleResponse createRole(@Argument("input") CreateRole input) {
        var resp = roleCommandStub.createRole(com.sanedge.paymentgateway.pb.role.CreateRoleRequest.newBuilder()
                .setName(Gql.s(input.name())).build());
        return ProtoMapper.role(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public RoleResponse updateRole(@Argument("input") UpdateRole input) {
        var resp = roleCommandStub.updateRole(com.sanedge.paymentgateway.pb.role.UpdateRoleRequest.newBuilder()
                .setId(Gql.i(input.id()))
                .setName(Gql.s(input.name())).build());
        return ProtoMapper.role(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public RoleResponseDeleteAt trashRole(@Argument Long id) {
        var resp = roleCommandStub.trashedRole(com.sanedge.paymentgateway.pb.role.FindByIdRoleRequest.newBuilder()
                .setRoleId(Gql.i(id)).build());
        return ProtoMapper.roleDeleteAt(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public RoleResponseDeleteAt restoreRole(@Argument Long id) {
        var resp = roleCommandStub.restoreRole(com.sanedge.paymentgateway.pb.role.FindByIdRoleRequest.newBuilder()
                .setRoleId(Gql.i(id)).build());
        return ProtoMapper.roleDeleteAt(resp.getData());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Boolean deleteRolePermanent(@Argument Long id) {
        var resp = roleCommandStub.deleteRolePermanent(
                com.sanedge.paymentgateway.pb.role.FindByIdRoleRequest.newBuilder()
                        .setRoleId(Gql.i(id)).build());
        return Gql.ok(resp.getStatus());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Boolean restoreAllRoles() {
        var resp = roleCommandStub.restoreAllRole(Empty.getDefaultInstance());
        return Gql.ok(resp.getStatus());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Boolean deleteAllRolesPermanent() {
        var resp = roleCommandStub.deleteAllRolePermanent(Empty.getDefaultInstance());
        return Gql.ok(resp.getStatus());
    }

    public record CreateRole(String name) {
    }

    public record UpdateRole(Long id, String name) {
    }
}
