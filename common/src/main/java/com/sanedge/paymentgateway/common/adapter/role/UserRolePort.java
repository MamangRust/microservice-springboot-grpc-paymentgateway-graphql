package com.sanedge.paymentgateway.common.adapter.role;

import java.util.List;

import com.sanedge.paymentgateway.common.adapter.model.Role;
import com.sanedge.paymentgateway.common.adapter.model.UserRole;

/**
 * User&harr;role surface: resolving a user's roles and (un)assigning them. Owns
 * the calls to the role service's user-role RPCs.
 */
public interface UserRolePort {

    List<Role> findByUserId(int userId);

    UserRole assignRoleToUser(int userId, int roleId);

    void removeRoleFromUser(int userId, int roleId);
}
