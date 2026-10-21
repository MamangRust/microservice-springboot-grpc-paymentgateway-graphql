package com.sanedge.paymentgateway.transaction.repository;

import com.sanedge.paymentgateway.transaction.models.Transaction;

public interface TransactionCommandRepositoryCustom {
    Transaction updateTransactionStatus(Long transactionId, String status);

    Transaction trashed(Long transactionId);

    Transaction restore(Long transactionId);

    boolean deletePermanent(Long transactionId);

    boolean restoreAllDeleted();

    boolean deleteAllDeleted();
}
