package com.sanedge.paymentgateway.user.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sanedge.paymentgateway.common.adapter.role.RolePort;
import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.exception.ResourceNotFoundException;
import com.sanedge.paymentgateway.common.observability.TracingMetrics;
import com.sanedge.paymentgateway.user.domain.requests.CreateUserRequest;
import com.sanedge.paymentgateway.user.domain.requests.UpdateUserRequest;
import com.sanedge.paymentgateway.user.domain.responses.UserResponse;
import com.sanedge.paymentgateway.user.domain.responses.UserResponseDeleteAt;
import com.sanedge.paymentgateway.user.domain.responses.VerifyPasswordResponse;
import com.sanedge.paymentgateway.user.models.User;
import com.sanedge.paymentgateway.user.models.UserRole;
import com.sanedge.paymentgateway.user.repository.UserCommandRepository;
import com.sanedge.paymentgateway.user.repository.UserQueryRepository;
import com.sanedge.paymentgateway.user.repository.UserRoleRepository;
import com.sanedge.paymentgateway.user.service.UserCommandService;

import io.opentelemetry.api.common.Attributes;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserCommandImplService implements UserCommandService {
    private final UserQueryRepository userQueryRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserCommandRepository userCommandRepository;
    private final RolePort rolePort;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;
    private final TracingMetrics tracingMetrics;

    private static final String DEFAULT_ROLE = "ROLE_ADMIN";

    @Override
    public ApiResponse<UserResponse> create(CreateUserRequest req) {
        Attributes attrs = Attributes.builder()
                .put("user.username", req.getUsername() != null ? req.getUsername() : "")
                .put("user.email", req.getEmail() != null ? req.getEmail() : "")
                .build();

        return tracingMetrics.traceAndMeasure("createUser", "create_user", attrs, () -> {
            // The gRPC CreateUserRequest contract has no username field, so derive a
            // unique one from the email when the caller did not provide it.
            if (req.getUsername() == null || req.getUsername().isBlank()) {
                req.setUsername(deriveUsernameFromEmail(req.getEmail()));
            }

            validateRequest(req);

            Optional<User> existingUser = userQueryRepository.findByEmail(req.getEmail());
            if (existingUser.isPresent()) {
                log.error("❌ [REGISTER] Email already taken | Email: {}", req.getEmail());
                throw new IllegalArgumentException("Email already registered");
            }

            Long roleId = resolveDefaultRoleId();

            User newUser = new User();
            newUser.setUsername(req.getUsername());
            newUser.setFirstname(req.getFirstname());
            newUser.setLastname(req.getLastname());
            newUser.setEmail(req.getEmail());
            newUser.setPassword(passwordEncoder.encode(req.getPassword()));

            try {
                newUser = userCommandRepository.save(newUser);

                UserRole userRole = new UserRole();
                userRole.setUserId(newUser.getUserId());
                userRole.setRoleId(roleId);
                userRoleRepository.save(userRole);
            } catch (Exception e) {
                log.error("❌ Failed to create user", e);
                return ApiResponse.<UserResponse>builder()
                        .status("error")
                        .message("Failed to create user")
                        .build();
            }

            UserResponse userResponse = UserResponse.from(newUser);

            return ApiResponse.<UserResponse>builder()
                    .status("success")
                    .message("User registered successfully")
                    .data(userResponse)
                    .build();
        });
    }

    @Override
    public ApiResponse<UserResponse> update(UpdateUserRequest req) {
        Attributes attrs = Attributes.builder()
                .put("user.id", String.valueOf(req.getId()))
                .build();

        return tracingMetrics.traceAndMeasure("updateUser", "update_user", attrs, () -> {
            validateRequest(req);

            try {
                User user = userQueryRepository.findById(req.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with ID " + req.getId()));

                if (req.getEmail() != null && !req.getEmail().equals(user.getEmail())) {
                    Optional<User> existingUser = userQueryRepository.findByEmail(req.getEmail());
                    if (existingUser.isPresent()) {
                        log.error("❌ [UPDATE] Email already taken | Email: {}", req.getEmail());
                        return ApiResponse.<UserResponse>builder()
                                .status("error")
                                .message("Email already registered")
                                .build();
                    }
                    user.setEmail(req.getEmail());
                }

                if (req.getUsername() != null)
                    user.setUsername(req.getUsername());
                if (req.getFirstname() != null)
                    user.setFirstname(req.getFirstname());
                if (req.getLastname() != null)
                    user.setLastname(req.getLastname());

                if (req.getPassword() != null) {
                    if (!req.getPassword().equals(req.getConfirmPassword())) {
                        log.error("❌ [UPDATE] Password and confirm password do not match for user ID {}", req.getId());
                        return ApiResponse.<UserResponse>builder()
                                .status("error")
                                .message("Password and confirm password do not match")
                                .build();
                    }
                    user.setPassword(passwordEncoder.encode(req.getPassword()));
                }

                // The monolith replaced the user's role set with Set.of(ROLE_ADMIN) via the
                // @ManyToMany join table; mirror that by rewriting the user_roles rows.
                Long roleId = resolveDefaultRoleId();
                userRoleRepository.deleteByUserId(user.getUserId());
                UserRole userRole = new UserRole();
                userRole.setUserId(user.getUserId());
                userRole.setRoleId(roleId);
                userRoleRepository.save(userRole);

                user.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

                User updatedUser = userCommandRepository.save(user);

                log.info("✅ User updated successfully | User ID: {}", updatedUser.getUserId());

                return ApiResponse.<UserResponse>builder()
                        .status("success")
                        .message("✅ User updated successfully!")
                        .data(UserResponse.from(updatedUser))
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to update user id={}", req.getId(), e);
                return ApiResponse.<UserResponse>builder()
                        .status("error")
                        .message("Failed to update user")
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<UserResponseDeleteAt> trashed(Long userId) {
        Attributes attrs = Attributes.builder()
                .put("user.id", String.valueOf(userId))
                .build();

        return tracingMetrics.traceAndMeasure("trashUser", "trash_user", attrs, () -> {
            log.info("🗑️ Trashing user id={}", userId);

            try {
                User user = userCommandRepository.trashed(userId);
                return ApiResponse.<UserResponseDeleteAt>builder()
                        .status("success")
                        .message("🗑️ User trashed successfully!")
                        .data(UserResponseDeleteAt.from(user))
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to trash user id={}", userId, e);
                return ApiResponse.<UserResponseDeleteAt>builder()
                        .status("error")
                        .message("💥 Failed to trash user")
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<UserResponseDeleteAt> restore(Long userId) {
        Attributes attrs = Attributes.builder()
                .put("user.id", String.valueOf(userId))
                .build();

        return tracingMetrics.traceAndMeasure("restoreUser", "restore_user", attrs, () -> {
            log.info("♻️ Restoring user id={}", userId);

            try {
                User user = userCommandRepository.restore(userId);
                return ApiResponse.<UserResponseDeleteAt>builder()
                        .status("success")
                        .message("♻️ User restored successfully!")
                        .data(UserResponseDeleteAt.from(user))
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to restore user id={}", userId, e);
                return ApiResponse.<UserResponseDeleteAt>builder()
                        .status("error")
                        .message("💥 Failed to restore user")
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> deletePermanent(Long userId) {
        Attributes attrs = Attributes.builder()
                .put("user.id", String.valueOf(userId))
                .build();

        return tracingMetrics.traceAndMeasure("deleteUserPermanent", "delete_user_permanent", attrs, () -> {
            log.info("🧨 Permanently deleting user id={}", userId);

            try {
                userCommandRepository.deletePermanent(userId);
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .message("🧨 User permanently deleted!")
                        .data(true)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to permanently delete user id={}", userId, e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .message("💥 Failed to permanently delete user")
                        .data(false)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> restoreAll() {
        return tracingMetrics.traceAndMeasure("restoreAllTrashedUsers", "restore_all_trashed_users", () -> {
            log.info("🔄 Restoring ALL trashed users");

            try {
                userCommandRepository.restoreAllDeleted();
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .message("🔄 All users restored successfully!")
                        .data(true)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to restore all users", e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .message("💥 Failed to restore all users")
                        .data(false)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> deleteAll() {
        return tracingMetrics.traceAndMeasure("deleteAllTrashedUsers", "delete_all_trashed_users", () -> {
            log.info("💣 Permanently deleting ALL trashed users");

            try {
                userCommandRepository.deleteAllDeleted();
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .message("💣 All users permanently deleted!")
                        .data(true)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to delete all users", e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .message("💥 Failed to delete all users")
                        .data(false)
                        .build();
            }
        });
    }

    @Override
    public VerifyPasswordResponse verifyPassword(String email, String password) {
        Attributes attrs = Attributes.builder()
                .put("user.email", email != null ? email : "")
                .build();

        return tracingMetrics.traceAndMeasure("verifyPassword", "verify_password", attrs, () -> {
            log.info("🔐 Verifying password for email={}", email);

            Optional<User> userOpt = userQueryRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return VerifyPasswordResponse.builder()
                        .valid(false)
                        .user(null)
                        .build();
            }

            User user = userOpt.get();
            boolean valid = passwordEncoder.matches(password, user.getPassword());

            return VerifyPasswordResponse.builder()
                    .valid(valid)
                    .user(valid ? UserResponse.from(user) : null)
                    .build();
        });
    }

    /**
     * Resolves the default role id through the role service port (replaces the
     * monolith's local {@code RoleQueryRepository.findByRoleName}); the adapter
     * throws {@link ResourceNotFoundException} when the role is missing.
     */
    private Long resolveDefaultRoleId() {
        return (long) rolePort.findByName(DEFAULT_ROLE).id();
    }

    private String deriveUsernameFromEmail(String email) {
        String prefix = email == null ? "" : email;
        int at = prefix.indexOf('@');
        if (at >= 0) {
            prefix = prefix.substring(0, at);
        }
        prefix = prefix.toLowerCase().replaceAll("[^a-z0-9]", "");
        if (prefix.isEmpty()) {
            prefix = "user";
        }
        String suffix = String.valueOf(ThreadLocalRandom.current().nextInt(1000, 10000));
        int maxPrefix = 20 - 1 - suffix.length();
        if (prefix.length() > maxPrefix) {
            prefix = prefix.substring(0, maxPrefix);
        }
        return prefix + "_" + suffix;
    }

    private <T> void validateRequest(T req) {
        Set<ConstraintViolation<T>> violations = validator.validate(req);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<T> violation : violations) {
                sb.append(violation.getPropertyPath()).append(": ").append(violation.getMessage()).append("; ");
            }
            log.error("Validation failed: {}", sb.toString());
            throw new ConstraintViolationException("Validation failed: " + sb, violations);
        }
    }

}
