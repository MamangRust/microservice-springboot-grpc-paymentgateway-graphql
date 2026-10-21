package com.sanedge.paymentgateway.topup.repository;

import org.springframework.stereotype.Repository;

import com.sanedge.paymentgateway.topup.models.Topup;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
public class TopupCommandRepositoryImpl implements TopupCommandRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public Topup updateTopupAmount(Long topupId, Long amount) {
        em.createNativeQuery(
                "UPDATE topups SET topup_amount = :amount, updated_at = CURRENT_TIMESTAMP " +
                        "WHERE topup_id = :topupId AND deleted_at IS NULL")
                .setParameter("topupId", topupId)
                .setParameter("amount", amount)
                .executeUpdate();
        em.clear();

                return em.find(Topup.class, topupId);
    }

    @Override
    @Transactional
    public Topup updateTopupStatus(Long topupId, String status) {
        em.createNativeQuery(
                "UPDATE topups SET status = :status, updated_at = CURRENT_TIMESTAMP " +
                        "WHERE topup_id = :topupId AND deleted_at IS NULL")
                .setParameter("topupId", topupId)
                .setParameter("status", status)
                .executeUpdate();
        em.clear();

                return em.find(Topup.class, topupId);
    }

    @Override
    @Transactional
    public Topup trashed(Long topupId) {
        em.createNativeQuery(
                "UPDATE topups SET deleted_at = CURRENT_TIMESTAMP " +
                        "WHERE topup_id = :topupId AND deleted_at IS NULL")
                .setParameter("topupId", topupId)
                .executeUpdate();
        em.clear();

                return em.find(Topup.class, topupId);
    }

    @Override
    @Transactional
    public Topup restore(Long topupId) {
        em.createNativeQuery(
                "UPDATE topups SET deleted_at = NULL " +
                        "WHERE topup_id = :topupId AND deleted_at IS NOT NULL")
                .setParameter("topupId", topupId)
                .executeUpdate();
        em.clear();

                return em.find(Topup.class, topupId);
    }

    @Override
    @Transactional
    public boolean deletePermanent(Long topupId) {
        int deleted = em.createNativeQuery(
                "DELETE FROM topups WHERE topup_id = :topupId")
                .setParameter("topupId", topupId)
                .executeUpdate();
        em.clear();
        return deleted > 0;
    }

    @Override
    @Transactional
    public boolean restoreAllDeleted() {
        int updated = em.createNativeQuery(
                "UPDATE topups SET deleted_at = NULL WHERE deleted_at IS NOT NULL")
                .executeUpdate();
        em.clear();
        return updated > 0;
    }

    @Override
    @Transactional
    public boolean deleteAllDeleted() {
        int deleted = em.createNativeQuery(
                "DELETE FROM topups WHERE deleted_at IS NOT NULL")
                .executeUpdate();
        em.clear();
        return deleted > 0;
    }
}
