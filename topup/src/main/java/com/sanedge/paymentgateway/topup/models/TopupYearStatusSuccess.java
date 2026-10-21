package com.sanedge.paymentgateway.topup.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopupYearStatusSuccess {
    private String reportYear;
    private Long totalSuccess;
    private Long totalAmount;
}
