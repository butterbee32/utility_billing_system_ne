package rw.gov.utility_billing_system.dto.response.user;

import lombok.Builder;
import lombok.Getter;
import rw.gov.utility_billing_system.enums.RoleName;
import rw.gov.utility_billing_system.enums.Status;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Builder
public class UserResponse {

    private final Long id;
    private final String fullNames;
    private final String email;
    private final String phoneNumber;
    private final Status status;
    private final boolean emailVerified;
    private final boolean mustChangePassword;
    private final boolean accountLocked;
    private final LocalDateTime lastLoginAt;
    private final Set<RoleName> roles;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
