package com.sanedge.paymentgateway.saldo.domain.responses;

import com.sanedge.paymentgateway.saldo.models.Saldo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaldoResponseDeleteAt {
        private Long id;
        private String cardNumber;
        private Long totalBalance;
        private Long withdrawAmount;
        private String withdrawTime;
        private String createdAt;
        private String updatedAt;
        private String deletedAt;

        public static SaldoResponseDeleteAt from(Saldo saldo) {
                return SaldoResponseDeleteAt.builder()
                                .id(saldo.getSaldoId())
                                .cardNumber(saldo.getCardNumber())
                                .totalBalance(saldo.getTotalBalance())
                                .withdrawAmount(saldo.getWithdrawAmount())
                                .withdrawTime(
                                                saldo.getWithdrawTime() != null ? saldo.getWithdrawTime().toString()
                                                                : null)
                                .createdAt(
                                                saldo.getCreatedAt() != null ? saldo.getCreatedAt().toString() : null)
                                .updatedAt(
                                                saldo.getUpdatedAt() != null ? saldo.getUpdatedAt().toString() : null)
                                .deletedAt(
                                                saldo.getDeletedAt() != null ? saldo.getDeletedAt().toString() : null)
                                .build();
        }
}
