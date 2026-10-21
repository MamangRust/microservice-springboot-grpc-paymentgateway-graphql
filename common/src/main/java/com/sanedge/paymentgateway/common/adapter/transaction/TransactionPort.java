package com.sanedge.paymentgateway.common.adapter.transaction;

import java.util.List;

import com.sanedge.paymentgateway.common.adapter.model.Transaction;
import com.sanedge.paymentgateway.common.adapter.support.Paged;

/**
 * Port for reading transactions owned by the transaction service, replacing
 * direct {@code @GrpcClient("transaction")} usage in consumer services.
 */
public interface TransactionPort {

    Paged<Transaction> findAll(int page, int pageSize, String search);

    List<Transaction> findByMerchantId(int merchantId);
}
