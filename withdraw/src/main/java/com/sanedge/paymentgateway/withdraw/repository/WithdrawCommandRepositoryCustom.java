package com.sanedge.paymentgateway.withdraw.repository;

import com.sanedge.paymentgateway.withdraw.models.Withdraw;

public interface WithdrawCommandRepositoryCustom {
    Withdraw updateStatus(Long withdrawId, String status);

    Withdraw trashed(Long withdrawId);

    Withdraw restore(Long withdrawId);

    boolean deletePermanent(Long withdrawId);

    boolean restoreAllDeleted();

    boolean deleteAllDeleted();
}
