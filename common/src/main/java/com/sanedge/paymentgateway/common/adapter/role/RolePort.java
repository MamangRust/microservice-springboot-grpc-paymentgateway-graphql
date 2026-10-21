package com.sanedge.paymentgateway.common.adapter.role;

import com.sanedge.paymentgateway.common.adapter.model.Role;

/**
 * Read-only role surface. Role assignment lives in {@link UserRolePort}.
 */
public interface RolePort {

    Role findById(int roleId);

    Role findByName(String name);
}
