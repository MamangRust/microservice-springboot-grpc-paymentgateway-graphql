package com.sanedge.paymentgateway.gateway.graphql.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * GraphQL-facing token pair (camelCase) as opposed to the REST
 * {@code TokenResponse} which keeps the legacy {@code access_token} names.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Token {
    private String accessToken;
    private String refreshToken;

    public static Token from(com.sanedge.paymentgateway.gateway.dto.responses.auth.TokenResponse t) {
        if (t == null) {
            return null;
        }
        return Token.builder()
                .accessToken(t.getAccess_token())
                .refreshToken(t.getRefresh_token())
                .build();
    }
}
