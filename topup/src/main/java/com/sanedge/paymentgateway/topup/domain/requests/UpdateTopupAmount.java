package com.sanedge.paymentgateway.topup.domain.requests;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateTopupAmount {

    @Min(value = 1, message = "Topup ID wajib diisi")
    private Long topupId;

    @Min(value = 50000, message = "Minimal topup 50.000")
    private Long topupAmount;
}
