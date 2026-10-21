package com.sanedge.paymentgateway.merchant.repository;

import org.springframework.stereotype.Repository;

import com.sanedge.paymentgateway.merchant.models.Merchant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
public class MerchantCommandRepositoryImpl implements MerchantCommandRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public Merchant trashed(Long merchantId) {
        Merchant merchant = em.find(Merchant.class, merchantId);
        if (merchant != null) {
            em.createNativeQuery(
                    "UPDATE merchants SET deleted_at = CURRENT_TIMESTAMP " +
                            "WHERE merchant_id = :merchantId AND deleted_at IS NULL")
                    .setParameter("merchantId", merchantId)
                    .executeUpdate();
            em.refresh(merchant);
        }
        return merchant;
    }

    @Override
    @Transactional
    public Merchant restore(Long merchantId) {
        Merchant merchant = em.find(Merchant.class, merchantId);
        if (merchant != null) {
            em.createNativeQuery(
                    "UPDATE merchants SET deleted_at = NULL " +
                            "WHERE merchant_id = :merchantId AND deleted_at IS NOT NULL")
                    .setParameter("merchantId", merchantId)
                    .executeUpdate();
            em.refresh(merchant);
        }
        return merchant;
    }

    @Override
    @Transactional
    public boolean deletePermanent(Long merchantId) {
        Merchant merchant = em.find(Merchant.class, merchantId);
        int deleted = em.createNativeQuery(
                "DELETE FROM merchants WHERE merchant_id = :merchantId")
                .setParameter("merchantId", merchantId)
                .executeUpdate();
        if (merchant != null) {
            em.detach(merchant);
        }
        return deleted > 0;
    }

    @Override
    @Transactional
    public boolean restoreAllDeleted() {
        int updated = em.createNativeQuery(
                "UPDATE merchants SET deleted_at = NULL WHERE deleted_at IS NOT NULL")
                .executeUpdate();
        return updated > 0;
    }

    @Override
    @Transactional
    public boolean deleteAllDeleted() {
        int deleted = em.createNativeQuery(
                "DELETE FROM merchants WHERE deleted_at IS NOT NULL")
                .executeUpdate();
        return deleted > 0;
    }

    @Override
    @Transactional
    public boolean updateStatus(Long merchantId, String status) {
        Merchant merchant = em.find(Merchant.class, merchantId);
        int updated = em.createNativeQuery(
                "UPDATE merchants SET status = :status, updated_at = CURRENT_TIMESTAMP " +
                        "WHERE merchant_id = :merchantId AND deleted_at IS NULL")
                .setParameter("merchantId", merchantId)
                .setParameter("status", status)
                .executeUpdate();
        if (merchant != null) {
            em.refresh(merchant);
        }
        return updated > 0;
    }
}