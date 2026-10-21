package com.sanedge.paymentgateway.topup.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopupMonthStatusSuccess {
    private String reportYear;
    private String monthName;
    private Long totalSuccess;
    private Long totalAmount;
}
