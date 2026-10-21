package com.sanedge.paymentgateway.card.repository;

import org.springframework.stereotype.Repository;

import com.sanedge.paymentgateway.card.models.Card;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
public class CardCommandRepositoryImpl implements CardCommandRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public Card trashed(Long cardId) {
        Card card = em.find(Card.class, cardId);
        if (card != null) {
            em.createNativeQuery(
                    "UPDATE cards SET deleted_at = CURRENT_TIMESTAMP " +
                            "WHERE card_id = :cardId AND deleted_at IS NULL")
                    .setParameter("cardId", cardId)
                    .executeUpdate();
            em.refresh(card);
        }
        return card;
    }

    @Override
    @Transactional
    public Card restore(Long cardId) {
        Card card = em.find(Card.class, cardId);
        if (card != null) {
            em.createNativeQuery(
                    "UPDATE cards SET deleted_at = NULL " +
                            "WHERE card_id = :cardId AND deleted_at IS NOT NULL")
                    .setParameter("cardId", cardId)
                    .executeUpdate();
            em.refresh(card);
        }
        return card;
    }

    @Override
    @Transactional
    public boolean deletePermanent(Long cardId) {
        Card card = em.find(Card.class, cardId);
        int deleted = em.createNativeQuery(
                "DELETE FROM cards WHERE card_id = :cardId")
                .setParameter("cardId", cardId)
                .executeUpdate();
        if (card != null) {
            em.detach(card);
        }
        return deleted > 0;
    }

    @Override
    @Transactional
    public boolean restoreAllDeleted() {
        int updated = em.createNativeQuery(
                "UPDATE cards SET deleted_at = NULL WHERE deleted_at IS NOT NULL")
                .executeUpdate();
        return updated > 0;
    }

    @Override
    @Transactional
    public boolean deleteAllDeleted() {
        int deleted = em.createNativeQuery(
                "DELETE FROM cards WHERE deleted_at IS NOT NULL")
                .executeUpdate();
        return deleted > 0;
    }
}
