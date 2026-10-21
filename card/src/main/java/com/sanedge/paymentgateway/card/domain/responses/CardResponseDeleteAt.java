package com.sanedge.paymentgateway.card.domain.responses;

import com.sanedge.paymentgateway.card.models.Card;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardResponseDeleteAt {
    private Long id;
    private Long userId;
    private String cardNumber;
    private String cardType;
    private String expireDate;
    private String cvv;
    private String cardProvider;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;

    public static CardResponseDeleteAt from(Card card) {
        return CardResponseDeleteAt.builder()
                .id(card.getCardId())
                .userId(card.getUserId())
                .cardNumber(card.getCardNumber())
                .cardType(card.getCardType())
                .expireDate(card.getExpireDate().toString())
                .cvv(card.getCvv())
                .cardProvider(card.getCardProvider())
                .createdAt(card.getCreatedAt().toString())
                .updatedAt(card.getUpdatedAt().toString())
                .deletedAt(card.getDeletedAt() != null ? card.getDeletedAt().toString() : null)
                .build();
    }
}
