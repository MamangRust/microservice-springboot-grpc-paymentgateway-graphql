package com.sanedge.paymentgateway.topup.repository;

import com.sanedge.paymentgateway.topup.models.Topup;

public interface TopupCommandRepositoryCustom {
    Topup updateTopupAmount(Long topupId, Long amount);

    Topup updateTopupStatus(Long topupId, String status);

    Topup trashed(Long topupId);

    Topup restore(Long topupId);

    boolean deletePermanent(Long topupId);

    boolean restoreAllDeleted();

    boolean deleteAllDeleted();
}
