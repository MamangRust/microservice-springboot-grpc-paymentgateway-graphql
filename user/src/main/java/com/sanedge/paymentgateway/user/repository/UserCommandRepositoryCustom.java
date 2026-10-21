package com.sanedge.paymentgateway.user.repository;

import com.sanedge.paymentgateway.user.models.User;

public interface UserCommandRepositoryCustom {
    User trashed(Long userId);

    User restore(Long userId);

    User deletePermanent(Long userId);

    boolean restoreAllDeleted();

    boolean deleteAllDeleted();
}
