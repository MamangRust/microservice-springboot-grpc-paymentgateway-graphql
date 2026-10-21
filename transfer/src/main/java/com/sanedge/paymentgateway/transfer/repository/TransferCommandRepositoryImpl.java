package com.sanedge.paymentgateway.transfer.repository;

import org.springframework.stereotype.Repository;

import com.sanedge.paymentgateway.transfer.models.Transfer;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
public class TransferCommandRepositoryImpl implements TransferCommandRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public Transfer updateTransferAmount(Long transferId, Long amount) {
        Transfer transfer = em.find(Transfer.class, transferId);
        if (transfer != null) {
            em.createNativeQuery(
                    "UPDATE transfers " +
                            "SET transfer_amount = :amount, transfer_time = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP "
                            +
                            "WHERE transfer_id = :transferId AND deleted_at IS NULL")
                    .setParameter("transferId", transferId)
                    .setParameter("amount", amount)
                    .executeUpdate();
            em.refresh(transfer);
        }
        return transfer;
    }

    @Override
    @Transactional
    public Transfer updateTransferStatus(Long transferId, String status) {
        Transfer transfer = em.find(Transfer.class, transferId);
        if (transfer != null) {
            em.createNativeQuery(
                    "UPDATE transfers SET status = :status, updated_at = CURRENT_TIMESTAMP " +
                            "WHERE transfer_id = :transferId AND deleted_at IS NULL")
                    .setParameter("transferId", transferId)
                    .setParameter("status", status)
                    .executeUpdate();
            em.refresh(transfer);
        }
        return transfer;
    }

    @Override
    @Transactional
    public Transfer trashed(Long transferId) {
        Transfer transfer = em.find(Transfer.class, transferId);
        if (transfer != null) {
            em.createNativeQuery(
                    "UPDATE transfers SET deleted_at = CURRENT_TIMESTAMP " +
                            "WHERE transfer_id = :transferId AND deleted_at IS NULL")
                    .setParameter("transferId", transferId)
                    .executeUpdate();
            em.refresh(transfer);
        }
        return transfer;
    }

    @Override
    @Transactional
    public Transfer restore(Long transferId) {
        Transfer transfer = em.find(Transfer.class, transferId);
        if (transfer != null) {
            em.createNativeQuery(
                    "UPDATE transfers SET deleted_at = NULL " +
                            "WHERE transfer_id = :transferId AND deleted_at IS NOT NULL")
                    .setParameter("transferId", transferId)
                    .executeUpdate();
            em.refresh(transfer);
        }
        return transfer;
    }

    @Override
    @Transactional
    public boolean deletePermanent(Long transferId) {
        Transfer transfer = em.find(Transfer.class, transferId);
        int deleted = em.createNativeQuery(
                "DELETE FROM transfers WHERE transfer_id = :transferId")
                .setParameter("transferId", transferId)
                .executeUpdate();
        if (transfer != null) {
            em.detach(transfer);
        }
        return deleted > 0;
    }

    @Override
    @Transactional
    public boolean restoreAllDeleted() {
        int updated = em.createNativeQuery(
                "UPDATE transfers SET deleted_at = NULL WHERE deleted_at IS NOT NULL")
                .executeUpdate();
        return updated > 0;
    }

    @Override
    @Transactional
    public boolean deleteAllDeleted() {
        int deleted = em.createNativeQuery(
                "DELETE FROM transfers WHERE deleted_at IS NOT NULL")
                .executeUpdate();
        return deleted > 0;
    }
}
