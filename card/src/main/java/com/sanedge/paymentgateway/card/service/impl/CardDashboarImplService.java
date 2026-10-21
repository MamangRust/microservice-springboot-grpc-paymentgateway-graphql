package com.sanedge.paymentgateway.card.service.impl;

import org.springframework.stereotype.Service;

import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.card.domain.responses.dashboard.CardDashboard;
import com.sanedge.paymentgateway.card.domain.responses.dashboard.CardDashboardCard;
import com.sanedge.paymentgateway.card.repository.dashboard.CardDashboardByCardRepository;
import com.sanedge.paymentgateway.card.repository.dashboard.CardDashboardRepository;
import com.sanedge.paymentgateway.card.service.CardDashboardService;
import com.sanedge.paymentgateway.common.observability.TracingMetrics;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CardDashboarImplService implements CardDashboardService {
    private final CardDashboardRepository cardDashboardRepository;
    private final CardDashboardByCardRepository cardDashboardByCardRepository;
    private final TracingMetrics tracingMetrics;

    @Override
    public ApiResponse<CardDashboard> dashboard() {
        Attributes attrs = Attributes.builder()
                .put("operation", "get_global_dashboard")
                .build();

        return tracingMetrics.traceAndMeasure("getGlobalDashboard", "get_global_dashboard", attrs, () -> {
            try {
                log.info("📊 Fetching global dashboard statistics");

                Long totalBalance = cardDashboardRepository.getTotalBalance();
                Long totalTopup = cardDashboardRepository.getTotalTopupAmount();
                Long totalTransaction = cardDashboardRepository.getTotalTransactionAmount();
                Long totalTransfer = cardDashboardRepository.getTotalTransferAmount();
                Long totalWithdraw = cardDashboardRepository.getTotalWithdrawAmount();

                CardDashboard dashboard = CardDashboard.builder()
                        .totalBalance(totalBalance)
                        .totalTopup(totalTopup)
                        .totalTransaction(totalTransaction)
                        .totalTransfer(totalTransfer)
                        .totalWithdraw(totalWithdraw)
                        .build();

                log.info("✅ Global dashboard retrieved successfully");
                return ApiResponse.<CardDashboard>builder()
                        .status("success")
                        .message("Global dashboard retrieved successfully")
                        .data(dashboard)
                        .build();

            } catch (Exception e) {
                log.error("❌ Failed to fetch dashboard data", e);
                return ApiResponse.<CardDashboard>builder()
                        .status("success")
                        .message("Failed to fetch dashboard data: " + e.getMessage())
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<CardDashboardCard> dashboardByCard(String cardNumber) {
        Attributes attrs = Attributes.builder()
                .put("operation", "get_dashboard_by_card")
                .put("card.number", cardNumber != null ? cardNumber : "")
                .build();

        return tracingMetrics.traceAndMeasure("getDashboardByCard", "get_dashboard_by_card", attrs, () -> {
            try {
                log.info("💳📊 Fetching dashboard for card: {}", cardNumber);

                Long totalBalance = cardDashboardByCardRepository.getTotalBalanceByCard(cardNumber);
                Long totalTopup = cardDashboardByCardRepository.getTotalTopupAmountByCard(cardNumber);
                Long totalTransaction = cardDashboardByCardRepository.getTotalTransactionAmountByCard(cardNumber);
                Long totalTransferSend = cardDashboardByCardRepository.getTotalTransferAmountBySender(cardNumber);
                Long totalTransferReceiver = cardDashboardByCardRepository.getTotalTransferAmountByReceiver(cardNumber);
                Long totalWithdraw = cardDashboardByCardRepository.getTotalWithdrawAmountByCard(cardNumber);

                CardDashboardCard dashboardCard = CardDashboardCard.builder()
                        .totalBalance(totalBalance)
                        .totalTopup(totalTopup)
                        .totalTransaction(totalTransaction)
                        .totalTransferSend(totalTransferSend)
                        .totalTransferReceiver(totalTransferReceiver)
                        .totalWithdraw(totalWithdraw)
                        .build();

                log.info("✅ Dashboard for card {} retrieved successfully", cardNumber);
                return ApiResponse.<CardDashboardCard>builder()
                        .status("success")
                        .message("Dashboard for card " + cardNumber + " retrieved successfully")
                        .data(dashboardCard)
                        .build();

            } catch (Exception e) {
                log.error("❌ Failed to fetch dashboard for card {}", cardNumber, e);
                return ApiResponse.<CardDashboardCard>builder()
                        .status("success")
                        .message("Failed to fetch dashboard for card " + cardNumber + ": " + e.getMessage())
                        .build();
            }
        });
    }
}