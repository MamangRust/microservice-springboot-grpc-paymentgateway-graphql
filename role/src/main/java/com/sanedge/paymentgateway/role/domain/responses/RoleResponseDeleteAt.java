package com.sanedge.paymentgateway.role.domain.responses;

import com.sanedge.paymentgateway.role.models.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponseDeleteAt {
    private Long id;
    private String name;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;

    public static RoleResponseDeleteAt from(Role role) {
        if (role == null) {
            return null;
        }
        return RoleResponseDeleteAt.builder()
                .id(role.getRoleId())
                .name(role.getRoleName())
                .createdAt(role.getCreatedAt() != null ? role.getCreatedAt().toString() : null)
                .updatedAt(role.getUpdatedAt() != null ? role.getUpdatedAt().toString() : null)
                .deletedAt(role.getDeletedAt() != null ? role.getDeletedAt().toString() : null)
                .build();
    }
}
