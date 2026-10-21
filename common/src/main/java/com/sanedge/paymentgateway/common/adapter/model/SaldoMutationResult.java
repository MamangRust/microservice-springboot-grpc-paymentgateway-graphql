package com.sanedge.paymentgateway.common.adapter.model;

/**
 * Slim saldo view returned by balance mutations (debit/credit/update).
 */
public record SaldoMutationResult(int saldoId, String cardNumber, int totalBalance) {
}
