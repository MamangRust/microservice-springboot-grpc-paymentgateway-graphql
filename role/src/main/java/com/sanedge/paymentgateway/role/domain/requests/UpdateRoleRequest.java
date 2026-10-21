package com.sanedge.paymentgateway.role.domain.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateRoleRequest {

    @Min(value = 1, message = "ID role minimal 1")
    private Long id;

    @NotBlank(message = "Nama role wajib diisi")
    private String name;
}
