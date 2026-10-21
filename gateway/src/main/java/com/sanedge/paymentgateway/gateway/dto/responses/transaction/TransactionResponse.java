package com.sanedge.paymentgateway.gateway.dto.responses.transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private String transactionNo;
    private String cardNumber;
    private Long amount;
    private String paymentMethod;
    private Long merchantId;
    private String transactionTime;
    private String createdAt;
    private String updatedAt;

    
}