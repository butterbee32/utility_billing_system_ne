package rw.gov.utility_billing_system.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import rw.gov.utility_billing_system.utility.validation.ValidEmail;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @ValidEmail
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
