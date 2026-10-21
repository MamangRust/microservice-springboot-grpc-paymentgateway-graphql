package com.sanedge.paymentgateway.card.service;

import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.card.domain.responses.dashboard.CardDashboard;
import com.sanedge.paymentgateway.card.domain.responses.dashboard.CardDashboardCard;

public interface CardDashboardService {
    ApiResponse<CardDashboard> dashboard();

    ApiResponse<CardDashboardCard> dashboardByCard(String card_number);
}
