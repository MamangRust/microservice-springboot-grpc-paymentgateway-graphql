package com.sanedge.paymentgateway.gateway.graphql.resolver;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import com.sanedge.paymentgateway.gateway.dto.responses.user.UserResponse;
import com.sanedge.paymentgateway.gateway.graphql.Gql;
import com.sanedge.paymentgateway.gateway.graphql.dto.Token;
import com.sanedge.paymentgateway.gateway.mapper.ProtoMapper;
import com.sanedge.paymentgateway.gateway.security.UserDetailsImpl;
import com.sanedge.paymentgateway.pb.AuthServiceGrpc;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AuthResolver {

    private final AuthServiceGrpc.AuthServiceBlockingStub authStub;

    @QueryMapping
    public String healthchecker() {
        return "API is up and running!";
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public UserResponse me() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        int userId = 0;
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl details
                && details.getId() != null) {
            userId = details.getId().intValue();
        }
        var resp = authStub.getMe(com.sanedge.paymentgateway.pb.GetMeRequest.newBuilder()
                .setUserId(userId)
                .build());
        return ProtoMapper.user(resp.getData());
    }

    @MutationMapping
    public UserResponse register(@Argument("input") RegisterInput input) {
        var resp = authStub.registerUser(com.sanedge.paymentgateway.pb.RegisterRequest.newBuilder()
                .setFirstname(Gql.s(input.firstname()))
                .setLastname(Gql.s(input.lastname()))
                .setEmail(Gql.s(input.email()))
                .setPassword(Gql.s(input.password()))
                .setConfirmPassword(Gql.s(input.confirmPassword()))
                .build());
        return ProtoMapper.user(resp.getData());
    }

    @MutationMapping
    public Token login(@Argument("input") LoginInput input) {
        var resp = authStub.loginUser(com.sanedge.paymentgateway.pb.LoginRequest.newBuilder()
                .setEmail(Gql.s(input.username()))
                .setPassword(Gql.s(input.password()))
                .build());
        return Token.from(ProtoMapper.token(resp.getData()));
    }

    @MutationMapping
    public Token refreshToken(@Argument String refreshToken) {
        var resp = authStub.refreshToken(com.sanedge.paymentgateway.pb.RefreshTokenRequest.newBuilder()
                .setRefreshToken(Gql.s(refreshToken))
                .build());
        return Token.from(ProtoMapper.token(resp.getData()));
    }

    public record RegisterInput(String firstname, String username, String lastname, String email,
            String password, String confirmPassword) {
    }

    public record LoginInput(String username, String password) {
    }
}
