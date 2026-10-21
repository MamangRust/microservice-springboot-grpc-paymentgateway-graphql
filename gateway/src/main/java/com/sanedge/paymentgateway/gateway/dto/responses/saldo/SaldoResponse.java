package com.sanedge.paymentgateway.gateway.dto.responses.saldo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaldoResponse {
        private Long id;
        private String cardNumber;
        private Long totalBalance;
        private Long withdrawAmount;
        private String withdrawTime;
        private String createdAt;
        private String updatedAt;

        
}
