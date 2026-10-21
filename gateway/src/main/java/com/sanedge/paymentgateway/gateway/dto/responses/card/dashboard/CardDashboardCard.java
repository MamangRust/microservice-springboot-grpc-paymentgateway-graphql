package com.sanedge.paymentgateway.gateway.dto.responses.card.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CardDashboardCard {
    private Long totalBalance;
    private Long totalTopup;
    private Long totalWithdraw;
    private Long totalTransaction;
    private Long totalTransferSend;
    private Long totalTransferReceiver;
}