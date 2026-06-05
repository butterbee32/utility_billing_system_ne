package rw.gov.utility_billing_system.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import rw.gov.utility_billing_system.enums.RoleName;
import rw.gov.utility_billing_system.utility.validation.ValidEmail;
import rw.gov.utility_billing_system.utility.validation.ValidPassword;
import rw.gov.utility_billing_system.utility.validation.ValidPhone;

import java.util.Set;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Full names are required")
    private String fullNames;

    @NotBlank(message = "Email is required")
    @ValidEmail
    private String email;

    @NotBlank(message = "Phone number is required")
    @ValidPhone
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @ValidPassword
    private String password;

    @NotEmpty(message = "At least one role is required")
    private Set<RoleName> roles;
}
