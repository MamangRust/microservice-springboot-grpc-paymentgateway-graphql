package com.sanedge.paymentgateway.common.adapter.saldo;

import com.sanedge.paymentgateway.common.adapter.model.Saldo;
import com.sanedge.paymentgateway.common.adapter.model.SaldoMutationResult;
import com.sanedge.paymentgateway.common.adapter.support.ProtoTime;
import com.sanedge.paymentgateway.common.exception.ResourceNotFoundException;
import com.sanedge.paymentgateway.pb.card.FindByCardNumberRequest;
import com.sanedge.paymentgateway.pb.saldo.SaldoCommandServiceGrpc.SaldoCommandServiceBlockingStub;
import com.sanedge.paymentgateway.pb.saldo.SaldoQueryServiceGrpc.SaldoQueryServiceBlockingStub;
import com.sanedge.paymentgateway.pb.saldo.SaldoResponse;
import com.sanedge.paymentgateway.pb.saldo.UpdateSaldoBalanceRequest;
import com.sanedge.paymentgateway.pb.saldo.UpdateSaldoWithdrawRequest;

public class SaldoAdapter implements SaldoPort {

    private final SaldoQueryServiceBlockingStub query;

    private final SaldoCommandServiceBlockingStub command;

    public SaldoAdapter(SaldoQueryServiceBlockingStub query, SaldoCommandServiceBlockingStub command) {
        this.query = query;
        this.command = command;
    }

    @Override
    public Saldo findByCardNumber(String cardNumber) {
        var resp = query.findByCardNumber(FindByCardNumberRequest.newBuilder().setCardNumber(cardNumber).build());
        if (resp == null || !resp.hasData()) {
            throw new ResourceNotFoundException("Saldo not found for card: " + cardNumber);
        }
        return toSaldo(resp.getData());
    }

    @Override
    public SaldoMutationResult updateSaldoBalance(BalanceUpdate data) {
        UpdateSaldoBalanceRequest.Builder builder = UpdateSaldoBalanceRequest.newBuilder()
                .setCardNumber(data.cardNumber())
                .setTotalBalance(data.totalBalance());
        if (data.deltaBalance() != null) {
            builder.setDeltaBalance(data.deltaBalance());
        }
        if (data.minimumBalance() != null) {
            builder.setMinimumBalance(data.minimumBalance());
        }
        if (data.withdrawAmount() != null) {
            builder.setWithdrawAmount(data.withdrawAmount());
        }
        if (data.withdrawTime() != null) {
            builder.setWithdrawTime(data.withdrawTime());
        }
        if (data.operationKey() != null) {
            builder.setOperationKey(data.operationKey());
        }
        var resp = command.updateSaldoBalance(builder.build());
        if (resp == null || !resp.hasData()) {
            throw new ResourceNotFoundException("Saldo not found for card: " + data.cardNumber());
        }
        return toMutation(resp.getData());
    }

    @Override
    public SaldoMutationResult updateSaldoWithdraw(WithdrawUpdate data) {
        UpdateSaldoWithdrawRequest.Builder builder = UpdateSaldoWithdrawRequest.newBuilder()
                .setCardNumber(data.cardNumber())
                .setTotalBalance(data.totalBalance());
        if (data.withdrawTime() != null) {
            builder.setWithdrawTime(data.withdrawTime());
        }
        if (data.withdrawAmount() != null) {
            builder.setWithdrawAmount(data.withdrawAmount());
        }
        if (data.deltaBalance() != null) {
            builder.setDeltaBalance(data.deltaBalance());
        }
        if (data.minimumBalance() != null) {
            builder.setMinimumBalance(data.minimumBalance());
        }
        if (data.operationKey() != null) {
            builder.setOperationKey(data.operationKey());
        }
        var resp = command.updateSaldoWithdraw(builder.build());
        if (resp == null || !resp.hasData()) {
            throw new ResourceNotFoundException("Saldo not found for card: " + data.cardNumber());
        }
        return toMutation(resp.getData());
    }

    private static Saldo toSaldo(SaldoResponse r) {
        if (r == null) {
            return null;
        }
        Integer withdrawAmount = r.getWithdrawAmount() == 0 ? null : r.getWithdrawAmount();
        return new Saldo(r.getSaldoId(), r.getCardNumber(), r.getTotalBalance(), withdrawAmount,
                ProtoTime.parse(r.getWithdrawTime()), ProtoTime.parse(r.getCreatedAt()),
                ProtoTime.parse(r.getUpdatedAt()));
    }

    private static SaldoMutationResult toMutation(SaldoResponse r) {
        if (r == null) {
            return null;
        }
        return new SaldoMutationResult(r.getSaldoId(), r.getCardNumber(), r.getTotalBalance());
    }
}
