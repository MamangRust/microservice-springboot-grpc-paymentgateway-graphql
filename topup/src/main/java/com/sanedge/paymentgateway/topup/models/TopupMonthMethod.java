package com.sanedge.paymentgateway.topup.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopupMonthMethod {
    private String monthName;
    private String topupMethod;
    private Long totalTopups;
    private Long totalAmount;
}
