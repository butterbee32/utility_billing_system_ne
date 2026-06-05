package rw.gov.utility_billing_system.dto.request.user;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import rw.gov.utility_billing_system.enums.RoleName;

import java.util.Set;

@Getter
@Setter
public class RoleUpdateRequest {

    @NotEmpty(message = "At least one role is required")
    private Set<RoleName> roles;
}
