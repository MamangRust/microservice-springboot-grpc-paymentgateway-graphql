package com.sanedge.paymentgateway.gateway.dto.responses.card.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardDashboard {
    private Long totalBalance;
    private Long totalTopup;
    private Long totalWithdraw;
    private Long totalTransaction;
    private Long totalTransfer;
}