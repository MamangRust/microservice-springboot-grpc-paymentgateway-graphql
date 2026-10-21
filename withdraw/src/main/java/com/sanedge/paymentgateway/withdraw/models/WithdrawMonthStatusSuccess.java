package com.sanedge.paymentgateway.withdraw.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawMonthStatusSuccess {
    private String reportYear;
    private String monthName;
    private Long totalSuccess;
    private Long totalAmount;
}
