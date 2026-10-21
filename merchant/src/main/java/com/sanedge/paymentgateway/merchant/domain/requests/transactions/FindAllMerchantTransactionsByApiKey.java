package com.sanedge.paymentgateway.merchant.domain.requests.transactions;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class FindAllMerchantTransactionsByApiKey {
    @NotBlank(message = "api_key wajib diisi")
    private String apiKey;

    @Min(value = 1)
    private Integer page = 1;

    @Min(value = 1)
    private Integer pageSize = 10;

    private String search = "";
}