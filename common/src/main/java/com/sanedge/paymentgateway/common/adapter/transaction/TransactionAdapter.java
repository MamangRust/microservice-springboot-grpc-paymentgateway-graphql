package com.sanedge.paymentgateway.common.adapter.transaction;

import java.util.ArrayList;
import java.util.List;

import com.sanedge.paymentgateway.common.adapter.model.Transaction;
import com.sanedge.paymentgateway.common.adapter.support.Paged;
import com.sanedge.paymentgateway.common.adapter.support.ProtoTime;
import com.sanedge.paymentgateway.pb.transaction.FindAllTransactionRequest;
import com.sanedge.paymentgateway.pb.transaction.FindTransactionByMerchantIdRequest;
import com.sanedge.paymentgateway.pb.transaction.TransactionQueryServiceGrpc.TransactionQueryServiceBlockingStub;
import com.sanedge.paymentgateway.pb.transaction.TransactionResponse;

public class TransactionAdapter implements TransactionPort {

    private final TransactionQueryServiceBlockingStub query;

    public TransactionAdapter(TransactionQueryServiceBlockingStub query) {
        this.query = query;
    }

    @Override
    public Paged<Transaction> findAll(int page, int pageSize, String search) {
        var resp = query.findAllTransaction(FindAllTransactionRequest.newBuilder()
                .setPage(page)
                .setPageSize(pageSize)
                .setSearch(nullSafe(search))
                .build());
        if (resp == null) {
            return Paged.empty();
        }
        int total = resp.hasPaginationMeta() ? resp.getPaginationMeta().getTotalRecords() : resp.getDataCount();
        return Paged.of(toList(resp.getDataList()), total);
    }

    @Override
    public List<Transaction> findByMerchantId(int merchantId) {
        var resp = query.findTransactionByMerchantId(
                FindTransactionByMerchantIdRequest.newBuilder().setMerchantId(merchantId).build());
        return resp == null ? List.of() : toList(resp.getDataList());
    }

    private static List<Transaction> toList(List<TransactionResponse> rows) {
        List<Transaction> out = new ArrayList<>(rows.size());
        for (TransactionResponse r : rows) {
            if (r == null) {
                continue;
            }
            out.add(new Transaction(r.getId(), r.getCardNumber(), r.getTransactionNo(), r.getAmount(),
                    r.getPaymentMethod(), r.getMerchantId(), ProtoTime.parse(r.getTransactionTime()),
                    ProtoTime.parse(r.getCreatedAt()), ProtoTime.parse(r.getUpdatedAt())));
        }
        return out;
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
