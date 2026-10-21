package com.sanedge.paymentgateway.common.adapter.role;

import java.util.ArrayList;
import java.util.List;

import com.sanedge.paymentgateway.common.adapter.model.Role;
import com.sanedge.paymentgateway.common.adapter.model.UserRole;
import com.sanedge.paymentgateway.common.adapter.support.AdapterException;
import com.sanedge.paymentgateway.pb.role.AssignRoleToUserRequest;
import com.sanedge.paymentgateway.pb.role.FindByIdUserRoleRequest;
import com.sanedge.paymentgateway.pb.role.RemoveRoleFromUserRequest;
import com.sanedge.paymentgateway.pb.role.RoleCommandServiceGrpc.RoleCommandServiceBlockingStub;
import com.sanedge.paymentgateway.pb.role.RoleResponse;
import com.sanedge.paymentgateway.pb.role.RoleServiceGrpc.RoleServiceBlockingStub;

public class UserRoleAdapter implements UserRolePort {

    private final RoleServiceBlockingStub query;

    private final RoleCommandServiceBlockingStub command;

    public UserRoleAdapter(RoleServiceBlockingStub query, RoleCommandServiceBlockingStub command) {
        this.query = query;
        this.command = command;
    }

    @Override
    public List<Role> findByUserId(int userId) {
        var resp = query.findByUserId(FindByIdUserRoleRequest.newBuilder().setUserId(userId).build());
        if (resp == null) {
            return List.of();
        }
        List<Role> roles = new ArrayList<>(resp.getDataCount());
        for (RoleResponse r : resp.getDataList()) {
            if (r != null) {
                roles.add(RoleAdapter.toRole(r));
            }
        }
        return roles;
    }

    @Override
    public UserRole assignRoleToUser(int userId, int roleId) {
        var resp = command.assignRoleToUser(AssignRoleToUserRequest.newBuilder()
                .setUserId(userId)
                .setRoleId(roleId)
                .build());
        if (resp == null || !resp.hasData()) {
            throw new AdapterException("assign role to user failed");
        }
        return new UserRole(resp.getData().getUserRoleId(), resp.getData().getUserId(),
                resp.getData().getRoleId());
    }

    @Override
    public void removeRoleFromUser(int userId, int roleId) {
        command.removeRoleFromUser(RemoveRoleFromUserRequest.newBuilder()
                .setUserId(userId)
                .setRoleId(roleId)
                .build());
    }
}
