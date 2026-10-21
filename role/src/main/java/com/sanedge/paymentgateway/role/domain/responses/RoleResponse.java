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
public class RoleResponse {
    private Long id;
    private String name;
    private String createdAt;
    private String updatedAt;

    public static RoleResponse from(Role role) {

        return RoleResponse.builder()
                .id(role.getRoleId())
                .name(role.getRoleName())
                .createdAt(role.getCreatedAt() != null ? role.getCreatedAt().toString() : null)
                .updatedAt(role.getUpdatedAt() != null ? role.getUpdatedAt().toString() : null)
                .build();
    }
}
