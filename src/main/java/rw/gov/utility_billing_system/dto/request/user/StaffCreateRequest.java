package rw.gov.utility_billing_system.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import rw.gov.utility_billing_system.enums.RoleName;
import rw.gov.utility_billing_system.utility.validation.ValidEmail;
import rw.gov.utility_billing_system.utility.validation.ValidPhone;

@Getter
@Setter
public class StaffCreateRequest {

    @NotBlank(message = "Full names are required")
    private String fullNames;

    @NotBlank(message = "Email is required")
    @ValidEmail
    private String email;

    @NotBlank(message = "Phone number is required")
    @ValidPhone
    private String phoneNumber;

    @NotNull(message = "Role is required")
    private RoleName role;
}
