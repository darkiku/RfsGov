package kz.gov.rfs.dto;

import jakarta.validation.constraints.NotNull;
import kz.gov.rfs.entity.Role;
import lombok.Data;

@Data
public class UpdateRoleRequest {
    @NotNull(message = "Role is required")
    private Role role;
}