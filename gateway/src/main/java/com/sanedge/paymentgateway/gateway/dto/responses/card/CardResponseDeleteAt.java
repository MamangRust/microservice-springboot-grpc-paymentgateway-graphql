package com.sanedge.paymentgateway.gateway.dto.responses.card;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardResponseDeleteAt {
    private Long id;
    private Long userId;
    private String cardNumber;
    private String cardType;
    private String expireDate;
    private String cvv;
    private String cardProvider;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;

    
}
