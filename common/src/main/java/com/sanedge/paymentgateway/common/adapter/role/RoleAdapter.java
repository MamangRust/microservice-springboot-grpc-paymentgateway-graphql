package com.sanedge.paymentgateway.common.adapter.role;

import com.sanedge.paymentgateway.common.adapter.model.Role;
import com.sanedge.paymentgateway.common.adapter.support.ProtoTime;
import com.sanedge.paymentgateway.common.exception.ResourceNotFoundException;
import com.sanedge.paymentgateway.pb.role.FindByIdRoleRequest;
import com.sanedge.paymentgateway.pb.role.FindByNameRoleRequest;
import com.sanedge.paymentgateway.pb.role.RoleResponse;
import com.sanedge.paymentgateway.pb.role.RoleServiceGrpc.RoleServiceBlockingStub;

public class RoleAdapter implements RolePort {

    private final RoleServiceBlockingStub query;

    public RoleAdapter(RoleServiceBlockingStub query) {
        this.query = query;
    }

    @Override
    public Role findById(int roleId) {
        var resp = query.findByIdRole(FindByIdRoleRequest.newBuilder().setRoleId(roleId).build());
        if (resp == null || !resp.hasData()) {
            throw new ResourceNotFoundException("Role not found: " + roleId);
        }
        return toRole(resp.getData());
    }

    @Override
    public Role findByName(String name) {
        var resp = query.findByNameRole(FindByNameRoleRequest.newBuilder().setName(name).build());
        if (resp == null || !resp.hasData()) {
            throw new ResourceNotFoundException("Role not found: " + name);
        }
        return toRole(resp.getData());
    }

    static Role toRole(RoleResponse r) {
        if (r == null) {
            return null;
        }
        return new Role(r.getId(), r.getName(), ProtoTime.parse(r.getCreatedAt()),
                ProtoTime.parse(r.getUpdatedAt()));
    }
}
