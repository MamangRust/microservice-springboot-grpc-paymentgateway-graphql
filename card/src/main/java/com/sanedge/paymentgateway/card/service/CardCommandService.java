package com.sanedge.paymentgateway.card.service;

import com.sanedge.paymentgateway.card.domain.requests.CreateCardRequest;
import com.sanedge.paymentgateway.card.domain.requests.UpdateCardRequest;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.card.domain.responses.CardResponse;
import com.sanedge.paymentgateway.card.domain.responses.CardResponseDeleteAt;

public interface CardCommandService {
    ApiResponse<CardResponse> createCard(CreateCardRequest req);

    ApiResponse<CardResponse> updateCard(UpdateCardRequest req);

    ApiResponse<CardResponseDeleteAt> trashCard(Long id);

    ApiResponse<CardResponseDeleteAt> restoreCard(Long id);

    ApiResponse<Boolean> deleteCard(Long id);

    ApiResponse<Boolean> restoreAll();

    ApiResponse<Boolean> deleteAll();
}
