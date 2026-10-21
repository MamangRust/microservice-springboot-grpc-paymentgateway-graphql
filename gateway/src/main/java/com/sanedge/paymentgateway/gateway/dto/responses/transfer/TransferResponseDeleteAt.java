package com.sanedge.paymentgateway.gateway.dto.responses.transfer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponseDeleteAt {
    private Long id;
    private String transferNo;
    private String transferFrom;
    private String transferTo;
    private Long transferAmount;
    private String transferTime;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;

    
}