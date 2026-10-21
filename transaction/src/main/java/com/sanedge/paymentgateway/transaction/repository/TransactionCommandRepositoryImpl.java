package com.sanedge.paymentgateway.transaction.repository;

import org.springframework.stereotype.Repository;

import com.sanedge.paymentgateway.transaction.models.Transaction;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
public class TransactionCommandRepositoryImpl implements TransactionCommandRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public Transaction updateTransactionStatus(Long transactionId, String status) {
        em.createNativeQuery(
                "UPDATE transactions SET status = :status, updated_at = CURRENT_TIMESTAMP " +
                        "WHERE transaction_id = :transactionId AND deleted_at IS NULL")
                .setParameter("transactionId", transactionId)
                .setParameter("status", status)
                .executeUpdate();
        em.clear();

                return em.find(Transaction.class, transactionId);
    }

    @Override
    @Transactional
    public Transaction trashed(Long transactionId) {
        em.createNativeQuery(
                "UPDATE transactions SET deleted_at = CURRENT_TIMESTAMP " +
                        "WHERE transaction_id = :transactionId AND deleted_at IS NULL")
                .setParameter("transactionId", transactionId)
                .executeUpdate();
        em.clear();

                return em.find(Transaction.class, transactionId);
    }

    @Override
    @Transactional
    public Transaction restore(Long transactionId) {
        em.createNativeQuery(
                "UPDATE transactions SET deleted_at = NULL " +
                        "WHERE transaction_id = :transactionId AND deleted_at IS NOT NULL")
                .setParameter("transactionId", transactionId)
                .executeUpdate();
        em.clear();

                return em.find(Transaction.class, transactionId);
    }

    @Override
    @Transactional
    public boolean deletePermanent(Long transactionId) {
        int deleted = em.createNativeQuery(
                "DELETE FROM transactions WHERE transaction_id = :transactionId")
                .setParameter("transactionId", transactionId)
                .executeUpdate();
        em.clear();
        return deleted > 0;
    }

    @Override
    @Transactional
    public boolean restoreAllDeleted() {
        int updated = em.createNativeQuery(
                "UPDATE transactions SET deleted_at = NULL WHERE deleted_at IS NOT NULL")
                .executeUpdate();
        em.clear();
        return updated > 0;
    }

    @Override
    @Transactional
    public boolean deleteAllDeleted() {
        int deleted = em.createNativeQuery(
                "DELETE FROM transactions WHERE deleted_at IS NOT NULL")
                .executeUpdate();
        em.clear();
        return deleted > 0;
    }
}
