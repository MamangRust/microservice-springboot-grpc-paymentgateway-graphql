package com.sanedge.paymentgateway.common.adapter.card;

import com.sanedge.paymentgateway.common.adapter.model.Card;

/**
 * Port for the card service: card lookups and card mutation, replacing direct
 * {@code @GrpcClient("card")} usage in consumer services.
 */
public interface CardPort {

    Card findCardByUserId(int userId);

    /**
     * Looks up a card together with its owner's email. Used by flows that need
     * to notify the cardholder (topup, transfer, withdraw).
     */
    Card findUserCardByCardNumber(String cardNumber);

    Card findCardByCardNumber(String cardNumber);

    Card updateCard(UpdateData data);

    record UpdateData(
            int cardId,
            int userId,
            String cardType,
            String expireDate,
            String cvv,
            String cardProvider) {
    }
}
