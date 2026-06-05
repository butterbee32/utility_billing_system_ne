package rw.gov.utility_billing_system.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import rw.gov.utility_billing_system.utility.validation.ValidPassword;

@Getter
@Setter
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @ValidPassword
    private String newPassword;
}
