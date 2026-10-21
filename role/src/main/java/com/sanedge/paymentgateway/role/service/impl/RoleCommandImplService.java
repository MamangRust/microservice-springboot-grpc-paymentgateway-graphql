package com.sanedge.paymentgateway.role.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.sanedge.paymentgateway.common.domain.responses.api.ApiResponse;
import com.sanedge.paymentgateway.common.exception.ResourceNotFoundException;
import com.sanedge.paymentgateway.common.observability.TracingMetrics;
import com.sanedge.paymentgateway.role.domain.requests.CreateRoleRequest;
import com.sanedge.paymentgateway.role.domain.requests.UpdateRoleRequest;
import com.sanedge.paymentgateway.role.domain.responses.RoleResponse;
import com.sanedge.paymentgateway.role.domain.responses.RoleResponseDeleteAt;
import com.sanedge.paymentgateway.role.models.Role;
import com.sanedge.paymentgateway.role.repository.RoleCommandRepository;
import com.sanedge.paymentgateway.role.repository.RoleQueryRepository;
import com.sanedge.paymentgateway.role.service.RoleCommandService;

import io.opentelemetry.api.common.Attributes;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class RoleCommandImplService implements RoleCommandService {

    private final RoleQueryRepository roleQueryRepository;
    private final RoleCommandRepository roleCommandRepository;
    private final Validator validator;
    private final TracingMetrics tracingMetrics;

    @Override
    public ApiResponse<RoleResponse> create(CreateRoleRequest request) {
        Attributes attrs = Attributes.builder()
                .put("role.name", request.getName() != null ? request.getName() : "")
                .build();

        return tracingMetrics.traceAndMeasure("createRole", "create_role", attrs, () -> {
            try {
                validateRequest(request);
                log.info("🆕 Creating role name={}", request.getName());

                roleQueryRepository.findByRoleName(request.getName())
                        .ifPresent(r -> {
                            log.warn("❌ Role creation failed. Role name '{}' already exists", request.getName());
                            throw new IllegalArgumentException("Role already exists");
                        });

                Role role = new Role();
                role.setRoleName(request.getName());
                role.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
                role.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

                Role savedRole = roleCommandRepository.save(role);
                log.info("✅ Role created successfully with id={}", savedRole.getRoleId());

                return ApiResponse.<RoleResponse>builder()
                        .status("success")
                        .message("Role created successfully")
                        .data(RoleResponse.from(savedRole))
                        .build();

            } catch (Exception e) {
                log.error("💥 Failed to create role name={}", request.getName(), e);
                return ApiResponse.<RoleResponse>builder()
                        .status("error")
                        .message("Role creation failed")
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<RoleResponse> update(UpdateRoleRequest request) {
        Attributes attrs = Attributes.builder()
                .put("role.id", String.valueOf(request.getId()))
                .build();

        return tracingMetrics.traceAndMeasure("updateRole", "update_role", attrs, () -> {
            try {
                validateRequest(request);
                if (request.getId() == null) {
                    log.error("❌ Role update failed. role_id is null");
                    throw new ResourceNotFoundException("role_id is required");
                }

                log.info("🔄 Updating role id={}", request.getId());

                Role role = roleCommandRepository.findById(request.getId())
                        .orElseThrow(() -> {
                            log.error("❌ Role with id {} not found", request.getId());
                            return new ResourceNotFoundException("Role not found");
                        });

                role.setRoleName(request.getName());
                role.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

                Role updatedRole = roleCommandRepository.save(role);
                log.info("✅ Role updated successfully with id={}", updatedRole.getRoleId());

                return ApiResponse.<RoleResponse>builder()
                        .status("success")
                        .data(RoleResponse.from(updatedRole))
                        .message("Role updated successfully")
                        .build();

            } catch (Exception e) {
                log.error("💥 Failed to update role id={}", request.getId(), e);
                return ApiResponse.<RoleResponse>builder()
                        .status("error")
                        .data(null)
                        .message("Role update failed")
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<RoleResponseDeleteAt> trash(Long id) {
        Attributes attrs = Attributes.builder()
                .put("role.id", String.valueOf(id))
                .build();

        return tracingMetrics.traceAndMeasure("trashRole", "trash_role", attrs, () -> {
            try {
                log.info("🗑️ Trashing role id={}", id);
                Role role = roleCommandRepository.trashed(id);
                return ApiResponse.<RoleResponseDeleteAt>builder()
                        .status("success")
                        .data(RoleResponseDeleteAt.from(role))
                        .message("Role trashed successfully")
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to trash role id={}", id, e);
                return ApiResponse.<RoleResponseDeleteAt>builder()
                        .status("error")
                        .data(null)
                        .message("Role trashing failed")
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<RoleResponseDeleteAt> restore(Long id) {
        Attributes attrs = Attributes.builder()
                .put("role.id", String.valueOf(id))
                .build();

        return tracingMetrics.traceAndMeasure("restoreRole", "restore_role", attrs, () -> {
            try {
                log.info("♻️ Restoring role id={}", id);
                Role role = roleCommandRepository.restore(id);
                return ApiResponse.<RoleResponseDeleteAt>builder()
                        .status("success")
                        .data(RoleResponseDeleteAt.from(role))
                        .message("Role restored successfully")
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to restore role id={}", id, e);
                return ApiResponse.<RoleResponseDeleteAt>builder()
                        .status("error")
                        .data(null)
                        .message("Role restoring failed")
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> delete(Long id) {
        Attributes attrs = Attributes.builder()
                .put("role.id", String.valueOf(id))
                .build();

        return tracingMetrics.traceAndMeasure("deleteRolePermanent", "delete_role_permanent", attrs, () -> {
            try {
                log.info("🧨 Permanently deleting role id={}", id);
                roleCommandRepository.deletePermanent(id);
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .data(true)
                        .message("Role permanently deleted")
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to permanently delete role id={}", id, e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .data(false)
                        .message("Role deletion failed")
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> restoreAll() {
        return tracingMetrics.traceAndMeasure("restoreAllRoles", "restore_all_roles", () -> {
            try {
                log.info("🔄 Restoring ALL trashed roles");
                roleCommandRepository.restoreAllDeleted();
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .data(true)
                        .message("Restored all trashed roles")
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to restore all roles", e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .data(false)
                        .message("Failed to restore all roles")
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> deleteAll() {
        return tracingMetrics.traceAndMeasure("deleteAllRoles", "delete_all_roles", () -> {
            try {
                log.info("💣 Permanently deleting ALL trashed roles");
                roleCommandRepository.deleteAllDeleted();
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .data(true)
                        .message("Deleted all trashed roles")
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to delete all roles", e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .data(false)
                        .message("Failed to delete all roles")
                        .build();
            }
        });
    }

    private <T> void validateRequest(T req) {
        Set<ConstraintViolation<T>> violations = validator.validate(req);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<T> violation : violations) {
                sb.append(violation.getPropertyPath()).append(": ").append(violation.getMessage()).append("; ");
            }
            log.error("Validation failed: {}", sb);
            throw new ConstraintViolationException("Validation failed", violations);
        }
    }
}
