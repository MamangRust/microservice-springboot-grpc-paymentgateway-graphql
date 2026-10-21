package com.sanedge.paymentgateway.card.service;

import java.util.List;

import com.sanedge.paymentgateway.card.domain.requests.FindAllCards;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.paymentgateway.card.domain.responses.CardResponse;
import com.sanedge.paymentgateway.card.domain.responses.CardResponseDeleteAt;

public interface CardQueryService {
    ApiResponsePagination<List<CardResponse>> findAll(FindAllCards req);

    ApiResponsePagination<List<CardResponseDeleteAt>> findByActive(FindAllCards req);

    ApiResponsePagination<List<CardResponseDeleteAt>> findByTrashed(FindAllCards req);

    ApiResponse<CardResponse> findById(Long cardId);

    ApiResponse<CardResponse> findByUserId(Long userId);

    ApiResponse<CardResponse> findByCardNumber(String cardNumber);
}
