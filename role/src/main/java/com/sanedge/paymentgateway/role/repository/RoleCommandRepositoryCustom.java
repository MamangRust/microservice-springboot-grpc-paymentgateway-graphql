package com.sanedge.paymentgateway.role.repository;

import com.sanedge.paymentgateway.role.models.Role;

public interface RoleCommandRepositoryCustom {
    Role trashed(Long roleId);

    Role restore(Long roleId);

    Role deletePermanent(Long roleId);

    boolean restoreAllDeleted();

    boolean deleteAllDeleted();
}
