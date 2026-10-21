package com.sanedge.paymentgateway.common.adapter.saldo;

import com.sanedge.paymentgateway.common.adapter.model.Saldo;
import com.sanedge.paymentgateway.common.adapter.model.SaldoMutationResult;

/**
 * Port for the saldo service: balance reads and mutations, replacing direct
 * {@code @GrpcClient("saldo")} usage in consumer services.
 */
public interface SaldoPort {

    Saldo findByCardNumber(String cardNumber);

    /**
     * Applies a balance mutation. When {@code deltaBalance} is set the remote
     * service performs an atomic conditional update; otherwise
     * {@code totalBalance} is written directly.
     */
    SaldoMutationResult updateSaldoBalance(BalanceUpdate data);

    SaldoMutationResult updateSaldoWithdraw(WithdrawUpdate data);

    record BalanceUpdate(
            String cardNumber,
            int totalBalance,
            Integer deltaBalance,
            Integer minimumBalance,
            Integer withdrawAmount,
            String withdrawTime,
            String operationKey) {
    }

    record WithdrawUpdate(
            String cardNumber,
            int totalBalance,
            String withdrawTime,
            Integer withdrawAmount,
            Integer deltaBalance,
            Integer minimumBalance,
            String operationKey) {
    }
}
