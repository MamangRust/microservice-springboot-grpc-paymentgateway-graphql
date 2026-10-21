package com.sanedge.paymentgateway.merchant.repository;

import com.sanedge.paymentgateway.merchant.models.Merchant;

public interface MerchantCommandRepositoryCustom {
    Merchant trashed(Long merchantId);

    Merchant restore(Long merchantId);

    boolean deletePermanent(Long merchantId);

    boolean restoreAllDeleted();

    boolean deleteAllDeleted();

    boolean updateStatus(Long merchantId, String status);
}
