package com.sanedge.paymentgateway.topup.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopupMonthStatusFailed {
    private String reportYear;
    private String monthName;
    private Long totalFailed;
    private Long totalAmount;
}
