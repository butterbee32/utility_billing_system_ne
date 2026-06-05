package rw.gov.utility_billing_system.dto.request.customer;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import rw.gov.utility_billing_system.enums.Status;
import rw.gov.utility_billing_system.utility.validation.ValidEmail;
import rw.gov.utility_billing_system.utility.validation.ValidNationalId;
import rw.gov.utility_billing_system.utility.validation.ValidPhone;

@Getter
@Setter
public class CustomerRequest {

    @NotBlank(message = "Full names are required")
    private String fullNames;

    @NotBlank(message = "National ID is required")
    @ValidNationalId
    private String nationalId;

    @NotBlank(message = "Email is required")
    @ValidEmail
    private String email;

    @NotBlank(message = "Phone number is required")
    @ValidPhone
    private String phoneNumber;

    @NotBlank(message = "Address is required")
    private String address;

    private Status status;

    private Long userId;
}
