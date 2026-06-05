package rw.gov.utility_billing_system.dto.response.auth;

import lombok.Builder;
import lombok.Getter;
import rw.gov.utility_billing_system.enums.RoleName;

import java.util.Set;

@Getter
@Builder
public class AuthResponse {

    private final String token;
    private final String type;
    private final Long userId;
    private final String email;
    private final String fullNames;
    private final Set<RoleName> roles;
    private final boolean mustChangePassword;
    private final String message;
}
