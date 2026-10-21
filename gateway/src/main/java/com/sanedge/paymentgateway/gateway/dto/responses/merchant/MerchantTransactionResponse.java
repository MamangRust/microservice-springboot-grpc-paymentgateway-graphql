package com.sanedge.paymentgateway.gateway.dto.responses.merchant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantTransactionResponse {
    private Long id;
    private String cardNumber;
    private Long amount;
    private String paymentMethod;
    private Long merchantId;
    private String merchantName;
    private String transactionTime;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;

    
}