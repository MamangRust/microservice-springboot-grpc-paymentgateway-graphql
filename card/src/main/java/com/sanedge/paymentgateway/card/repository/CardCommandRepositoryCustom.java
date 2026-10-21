package com.sanedge.paymentgateway.card.repository;

import com.sanedge.paymentgateway.card.models.Card;

public interface CardCommandRepositoryCustom {
    Card trashed(Long cardId);

    Card restore(Long cardId);

    boolean deletePermanent(Long cardId);

    boolean restoreAllDeleted();

    boolean deleteAllDeleted();
}