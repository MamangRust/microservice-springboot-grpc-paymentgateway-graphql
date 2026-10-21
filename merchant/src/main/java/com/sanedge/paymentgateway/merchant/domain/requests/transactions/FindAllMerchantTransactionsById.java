package com.sanedge.paymentgateway.merchant.domain.requests.transactions;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class FindAllMerchantTransactionsById {
    @Min(value = 1, message = "merchant_id minimal 1")
    private Long merchantId;

    @Min(value = 1)
    private Integer page = 1;

    @Min(value = 1)
    private Integer pageSize = 10;

    private String search = "";
}