package com.sanedge.paymentgateway.saldo.repository;

import com.sanedge.paymentgateway.saldo.models.Saldo;

public interface SaldoCommandRepositoryCustom {
    Saldo trashed(Long saldoId);

    Saldo restore(Long saldoId);

    int updateBalanceByCardNumber(String cardNumber, Long newBalance);

    int updateWithdrawByCardNumber(String cardNumber, Long withdrawAmount);

    boolean deletePermanent(Long saldoId);

    boolean restoreAllDeleted();

    boolean deleteAllDeleted();
}
