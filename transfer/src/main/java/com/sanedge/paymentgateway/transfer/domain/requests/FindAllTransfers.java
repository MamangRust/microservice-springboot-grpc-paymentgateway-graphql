package com.sanedge.paymentgateway.transfer.domain.requests;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class FindAllTransfers {
    @Min(value = 1, message = "Page minimal 1")
    private Integer page = 1;

    @Min(value = 1, message = "Page size minimal 1")
    private Integer pageSize = 10;

    private String search = "";
}
