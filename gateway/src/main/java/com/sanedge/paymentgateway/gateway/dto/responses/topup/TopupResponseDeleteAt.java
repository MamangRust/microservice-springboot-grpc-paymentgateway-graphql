package com.sanedge.paymentgateway.gateway.dto.responses.topup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopupResponseDeleteAt {
    private Long id;
    private String cardNumber;
    private String topupNo;
    private Long topupAmount;
    private String topupMethod;
    private String topupTime;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;

    
}