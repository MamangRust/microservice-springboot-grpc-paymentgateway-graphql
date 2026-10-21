package com.sanedge.paymentgateway.withdraw.repository;

import org.springframework.stereotype.Repository;

import com.sanedge.paymentgateway.withdraw.models.Withdraw;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
public class WithdrawCommandRepositoryImpl implements WithdrawCommandRepositoryCustom {

        @PersistenceContext
        private EntityManager em;

        @Override
        @Transactional
        public Withdraw updateStatus(Long withdrawId, String status) {
                Withdraw withdraw = em.find(Withdraw.class, withdrawId);
                if (withdraw != null) {
                        em.createNativeQuery(
                                        "UPDATE withdraws SET status = :status, updated_at = CURRENT_TIMESTAMP " +
                                                        "WHERE withdraw_id = :withdrawId AND deleted_at IS NULL")
                                        .setParameter("withdrawId", withdrawId)
                                        .setParameter("status", status)
                                        .executeUpdate();
                        em.refresh(withdraw);
                }
                return withdraw;
        }

        @Override
        @Transactional
        public Withdraw trashed(Long withdrawId) {
                Withdraw withdraw = em.find(Withdraw.class, withdrawId);
                if (withdraw != null) {
                        em.createNativeQuery(
                                        "UPDATE withdraws SET deleted_at = CURRENT_TIMESTAMP " +
                                                        "WHERE withdraw_id = :withdrawId AND deleted_at IS NULL")
                                        .setParameter("withdrawId", withdrawId)
                                        .executeUpdate();
                        em.refresh(withdraw);
                }
                return withdraw;
        }

        @Override
        @Transactional
        public Withdraw restore(Long withdrawId) {
                Withdraw withdraw = em.find(Withdraw.class, withdrawId);
                if (withdraw != null) {
                        em.createNativeQuery(
                                        "UPDATE withdraws SET deleted_at = NULL " +
                                                        "WHERE withdraw_id = :withdrawId AND deleted_at IS NOT NULL")
                                        .setParameter("withdrawId", withdrawId)
                                        .executeUpdate();
                        em.refresh(withdraw);
                }
                return withdraw;
        }

        @Override
        @Transactional
        public boolean deletePermanent(Long withdrawId) {
                Withdraw withdraw = em.find(Withdraw.class, withdrawId);
                int deleted = em.createNativeQuery(
                                "DELETE FROM withdraws WHERE withdraw_id = :withdrawId")
                                .setParameter("withdrawId", withdrawId)
                                .executeUpdate();
                if (withdraw != null) {
                        em.detach(withdraw);
                }
                return deleted > 0;
        }

        @Override
        @Transactional
        public boolean restoreAllDeleted() {
                int updated = em.createNativeQuery(
                                "UPDATE withdraws SET deleted_at = NULL WHERE deleted_at IS NOT NULL")
                                .executeUpdate();
                em.clear();
                return updated > 0;
        }

        @Override
        @Transactional
        public boolean deleteAllDeleted() {
                int deleted = em.createNativeQuery(
                                "DELETE FROM withdraws WHERE deleted_at IS NOT NULL")
                                .executeUpdate();
                em.clear();
                return deleted > 0;
        }
}
