package com.sanedge.paymentgateway.gateway.dto.responses.withdraw;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawResponse {
    private Long id;
    private String withdrawNo;
    private String cardNumber;
    private Long withdrawAmount;
    private String withdrawTime;
    private String createdAt;
    private String updatedAt;

    
}