package com.sanedge.paymentgateway.transfer.repository;

import com.sanedge.paymentgateway.transfer.models.Transfer;

public interface TransferCommandRepositoryCustom {
    Transfer updateTransferAmount(Long transferId, Long amount);

    Transfer updateTransferStatus(Long transferId, String status);

    Transfer trashed(Long transferId);

    Transfer restore(Long transferId);

    boolean deletePermanent(Long transferId);

    boolean restoreAllDeleted();

    boolean deleteAllDeleted();
}
