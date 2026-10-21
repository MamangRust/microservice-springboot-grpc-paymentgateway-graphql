package com.sanedge.paymentgateway.auth.service.impl;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.sanedge.paymentgateway.auth.domain.requests.AuthRequest;
import com.sanedge.paymentgateway.auth.domain.requests.RegisterRequest;
import com.sanedge.paymentgateway.auth.domain.responses.TokenResponse;
import com.sanedge.paymentgateway.auth.domain.responses.UserResponse;
import com.sanedge.paymentgateway.auth.models.RefreshToken;
import com.sanedge.paymentgateway.auth.repository.RefreshTokenCommandRepository;
import com.sanedge.paymentgateway.auth.repository.RefreshTokenQueryRepository;
import com.sanedge.paymentgateway.auth.service.AuthService;
import com.sanedge.paymentgateway.common.adapter.model.User;
import com.sanedge.paymentgateway.common.adapter.role.RolePort;
import com.sanedge.paymentgateway.common.adapter.support.AdapterException;
import com.sanedge.paymentgateway.common.adapter.user.AuthUserPort;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.exception.ResourceNotFoundException;
import com.sanedge.paymentgateway.common.observability.TracingMetrics;
import com.sanedge.paymentgateway.common.outbox.Outbox;
import com.sanedge.paymentgateway.common.outbox.OutboxEventFactory;
import com.sanedge.paymentgateway.common.outbox.OutboxRepository;
import com.sanedge.paymentgateway.common.security.JwtProvider;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

/**
 * Auth domain logic. User/role lookups and password verification are delegated
 * to the {@code user} and {@code role} services through their ports — this
 * module owns no {@code AuthenticationManager}/{@code UserDetailsService} and
 * persists only the refresh tokens locally.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AuthImplService implements AuthService {

    private static final String DEFAULT_ROLE = "ROLE_ADMIN";

    private static final String TOPIC_AUTH_REGISTER = "email-service-topic-auth-register";
    private static final String TOPIC_AUTH_FORGOT_PASSWORD = "email-service-topic-auth-forgot-password";

    private final AuthUserPort authUserPort;
    private final RolePort rolePort;
    private final JwtProvider jwtProvider;
    private final RefreshTokenQueryRepository refreshTokenQueryRepository;
    private final RefreshTokenCommandRepository refreshTokenCommandRepository;
    private final TracingMetrics tracingMetrics;
    private final OutboxRepository outboxRepository;

    @Override
    public ApiResponse<TokenResponse> login(AuthRequest loginRequest) {
        Attributes attrs = Attributes.builder()
                .put("user.email", loginRequest.getEmail() != null ? loginRequest.getEmail() : "")
                .build();

        return tracingMetrics.traceAndMeasure("login", "login", attrs, () -> {
            // Password verification happens inside the user service (BCrypt hash lives
            // there); we only receive whether it matched plus the user identity.
            AuthUserPort.VerifyResult verification = authUserPort.verifyPassword(
                    loginRequest.getEmail(), loginRequest.getPassword());

            if (!verification.valid() || verification.user() == null) {
                throw new IllegalArgumentException("Invalid email or password");
            }

            User user = verification.user();
            String email = user.email();
            Long userId = (long) user.id();

            String accessToken = jwtProvider.generateAccessToken(email);
            String refreshTokenStr = jwtProvider.generateRefreshToken(email);

            RefreshToken refreshTokenEntity = refreshTokenQueryRepository.findByUserId(userId)
                    .orElseGet(RefreshToken::new);
            refreshTokenEntity.setUserId(userId);
            refreshTokenEntity.setToken(refreshTokenStr);
            refreshTokenEntity.setExpiration(
                    new Timestamp(System.currentTimeMillis() + jwtProvider.getJwtRefreshExpirationMs()));
            refreshTokenEntity = refreshTokenCommandRepository.save(refreshTokenEntity);

            TokenResponse tokenResponse = TokenResponse.builder()
                    .access_token(accessToken)
                    .refresh_token(refreshTokenEntity.getToken())
                    .build();

            return ApiResponse.<TokenResponse>builder()
                    .status("success")
                    .message("Login successful")
                    .data(tokenResponse)
                    .build();
        });
    }

    @Override
    public ApiResponse<UserResponse> register(RegisterRequest registerRequest) {
        Attributes attrs = Attributes.builder()
                .put("user.email", registerRequest.getEmail() != null ? registerRequest.getEmail() : "")
                .build();

        return tracingMetrics.traceAndMeasure("register", "register", attrs, () -> {
            if (emailExists(registerRequest.getEmail())) {
                log.error("❌ [REGISTER] Email already taken | Email: {}", registerRequest.getEmail());
                throw new IllegalArgumentException("Email already registered");
            }

            // Memastikan role default ada; melempar ResourceNotFoundException bila tidak.
            rolePort.findByName(DEFAULT_ROLE);

            // The user service owns the User entity: it assigns the default role and
            // hashes the raw password before persisting.
            User created;
            try {
                created = authUserPort.create(new AuthUserPort.RegisterData(
                        registerRequest.getFirstname(),
                        registerRequest.getLastname(),
                        registerRequest.getEmail(),
                        registerRequest.getPassword(),
                        registerRequest.getConfirmPassword()));
                enqueueRegisterEmail(created);
            } catch (AdapterException e) {
                throw new IllegalArgumentException(e.getMessage());
            }

            return ApiResponse.<UserResponse>builder()
                    .status("success")
                    .message("User registered successfully")
                    .data(UserResponse.from(created))
                    .build();
        });
    }

    /**
     * Enqueues the welcome/verification email for a newly registered user.
     *
     * <p>The blueprint produced this topic with the user's own email as recipient;
     * the envelope follows the shared contract consumed by {@code EmailService}.
     */
    private void enqueueRegisterEmail(User user) {
        try {
            String subject = "Welcome to Quarkus Modular Monolith";
            String body = String.format(
                    "Hello %s %s,\n\nWelcome to our platform! Your account has been registered successfully.\n\nRegards,\nSupport Team",
                    user.firstname(), user.lastname());
            OutboxEventFactory.Envelope envelope = OutboxEventFactory.email(
                    TOPIC_AUTH_REGISTER, user.email(), subject, body);
            Outbox event = new Outbox();
            event.setAggregateType("USER");
            event.setAggregateId(user.email());
            event.setTopic(TOPIC_AUTH_REGISTER);
            event.setPayload(envelope.payload());
            outboxRepository.enqueue(event);
        } catch (RuntimeException e) {
            log.warn("⚠️ Failed to enqueue register email: {}", e.getMessage());
        }
    }

    @Override
    public ApiResponse<Void> forgotPassword(String email) {
        Attributes attrs = Attributes.builder()
                .put("user.email", email != null ? email : "")
                .build();

        return tracingMetrics.traceAndMeasure("forgotPassword", "forgot_password", attrs, () -> {
            User user = authUserPort.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            String token = UUID.randomUUID().toString();
            enqueueForgotPasswordEmail(user, token);

            return ApiResponse.<Void>builder()
                    .status("success")
                    .message("Password reset instructions have been sent to your email")
                    .build();
        });
    }

    /**
     * Enqueues the password-reset email for a user that requested it.
     *
     * <p>Mirrors the blueprint's {@code sendForgotPasswordEmail}: the recipient is
     * the user's own email and the envelope follows the shared contract consumed
     * by {@code EmailService}.
     */
    private void enqueueForgotPasswordEmail(User user, String token) {
        try {
            String subject = "Reset Password Verification";
            String body = String.format(
                    "Hello %s %s,\n\nYou have requested a password reset. Use the following token to reset your password:\n\n%s\n\nThis token will expire in 15 minutes.\n\nRegards,\nSupport Team",
                    user.firstname(), user.lastname(), token);
            OutboxEventFactory.Envelope envelope = OutboxEventFactory.email(
                    TOPIC_AUTH_FORGOT_PASSWORD, user.email(), subject, body);
            Outbox event = new Outbox();
            event.setAggregateType("USER");
            event.setAggregateId(user.email());
            event.setTopic(TOPIC_AUTH_FORGOT_PASSWORD);
            event.setPayload(envelope.payload());
            outboxRepository.enqueue(event);
        } catch (RuntimeException e) {
            log.warn("⚠️ Failed to enqueue forgot-password email: {}", e.getMessage());
        }
    }

    @Override
    public ApiResponse<TokenResponse> refreshToken(String refreshToken) {
        return tracingMetrics.traceAndMeasure("refreshToken", "refresh_token", () -> {
            RefreshToken storedToken = refreshTokenQueryRepository.findByToken(refreshToken)
                    .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));

            if (storedToken.getExpiration().before(new Date())) {
                log.warn("❌ [REFRESH TOKEN] Token expired | Token: {}", refreshToken);
                throw new IllegalArgumentException("Refresh token expired");
            }

            User user = authUserPort.findById(storedToken.getUserId().intValue());

            String newAccessToken = jwtProvider.generateAccessToken(user.email());

            storedToken.setExpiration(
                    new Timestamp(System.currentTimeMillis() + jwtProvider.getJwtRefreshExpirationMs()));
            refreshTokenCommandRepository.save(storedToken);

            TokenResponse tokenResponse = TokenResponse.builder()
                    .access_token(newAccessToken)
                    .refresh_token(refreshToken)
                    .build();

            return ApiResponse.<TokenResponse>builder()
                    .status("success")
                    .message("Access token refreshed successfully")
                    .data(tokenResponse)
                    .build();
        });
    }

    @Override
    public ApiResponse<UserResponse> getCurrentUser(Integer userId) {
        Attributes attrs = Attributes.builder()
                .put("user.id", String.valueOf(userId))
                .build();

        return tracingMetrics.traceAndMeasure("getCurrentUser", "get_current_user", attrs, () -> {
            User user = authUserPort.findById(userId);

            return ApiResponse.<UserResponse>builder()
                    .status("success")
                    .message("Current user fetched successfully")
                    .data(UserResponse.from(user))
                    .build();
        });
    }

    /**
     * Best-effort email pre-check against the user service, delegated to the
     * {@link AuthUserPort} (which mirrors the {@code FindAll} search and returns
     * an empty {@code Optional} when there is no exact match). The authoritative
     * uniqueness check still happens in the user service's {@code Create} RPC.
     */
    private boolean emailExists(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return authUserPort.findByEmail(email).isPresent();
    }
}
