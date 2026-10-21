package com.sanedge.paymentgateway.saldo.repository;

import org.springframework.stereotype.Repository;

import com.sanedge.paymentgateway.saldo.models.Saldo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
public class SaldoCommandRepositoryImpl implements SaldoCommandRepositoryCustom {

        @PersistenceContext
        private EntityManager em;

        @Override
        @Transactional
        public Saldo trashed(Long saldoId) {
                Saldo saldo = em.find(Saldo.class, saldoId);
                if (saldo != null) {
                        em.createNativeQuery(
                                        "UPDATE saldos SET deleted_at = CURRENT_TIMESTAMP " +
                                                        "WHERE saldo_id = :saldoId AND deleted_at IS NULL")
                                        .setParameter("saldoId", saldoId)
                                        .executeUpdate();
                        em.refresh(saldo);
                }
                return saldo;
        }

        @Override
        @Transactional
        public Saldo restore(Long saldoId) {
                Saldo saldo = em.find(Saldo.class, saldoId);
                if (saldo != null) {
                        em.createNativeQuery(
                                        "UPDATE saldos SET deleted_at = NULL " +
                                                        "WHERE saldo_id = :saldoId AND deleted_at IS NOT NULL")
                                        .setParameter("saldoId", saldoId)
                                        .executeUpdate();
                        em.refresh(saldo);
                }
                return saldo;
        }

        @Override
        @Transactional
        public int updateBalanceByCardNumber(String cardNumber, Long newBalance) {
                int updated = em.createNativeQuery(
                                "UPDATE saldos SET total_balance = :newBalance, updated_at = CURRENT_TIMESTAMP " +
                                                "WHERE card_number = :cardNumber AND deleted_at IS NULL")
                                .setParameter("cardNumber", cardNumber)
                                .setParameter("newBalance", newBalance)
                                .executeUpdate();

                var saldoOpt = em.createQuery("SELECT s FROM Saldo s WHERE s.cardNumber = :cardNumber AND s.deletedAt IS NULL", Saldo.class)
                                .setParameter("cardNumber", cardNumber)
                                .getResultList().stream().findFirst();
                saldoOpt.ifPresent(em::refresh);

                return updated;
        }

        @Override
        @Transactional
        public int updateWithdrawByCardNumber(String cardNumber, Long withdrawAmount) {
                int updated = em.createNativeQuery(
                                "UPDATE saldos SET withdraw_amount = :withdrawAmount, withdraw_time = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP "
                                                +
                                                "WHERE card_number = :cardNumber AND deleted_at IS NULL")
                                .setParameter("cardNumber", cardNumber)
                                .setParameter("withdrawAmount", withdrawAmount)
                                .executeUpdate();

                var saldoOpt = em.createQuery("SELECT s FROM Saldo s WHERE s.cardNumber = :cardNumber AND s.deletedAt IS NULL", Saldo.class)
                                .setParameter("cardNumber", cardNumber)
                                .getResultList().stream().findFirst();
                saldoOpt.ifPresent(em::refresh);

                return updated;
        }

        @Override
        @Transactional
        public boolean deletePermanent(Long saldoId) {
                Saldo saldo = em.find(Saldo.class, saldoId);
                int deleted = em.createNativeQuery(
                                "DELETE FROM saldos WHERE saldo_id = :saldoId")
                                .setParameter("saldoId", saldoId)
                                .executeUpdate();
                if (saldo != null) {
                        em.detach(saldo);
                }
                return deleted > 0;
        }

        @Override
        @Transactional
        public boolean restoreAllDeleted() {
                int updated = em.createNativeQuery(
                                "UPDATE saldos SET deleted_at = NULL WHERE deleted_at IS NOT NULL")
                                .executeUpdate();
                return updated > 0;
        }

        @Override
        @Transactional
        public boolean deleteAllDeleted() {
                int deleted = em.createNativeQuery(
                                "DELETE FROM saldos WHERE deleted_at IS NOT NULL")
                                .executeUpdate();
                return deleted > 0;
        }
}
