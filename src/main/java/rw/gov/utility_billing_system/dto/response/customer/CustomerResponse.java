package rw.gov.utility_billing_system.dto.response.customer;

import lombok.Builder;
import lombok.Getter;
import rw.gov.utility_billing_system.enums.RegistrationSource;
import rw.gov.utility_billing_system.enums.Status;

import java.time.LocalDateTime;

@Getter
@Builder
public class CustomerResponse {

    private final Long id;
    private final String fullNames;
    private final String nationalId;
    private final String email;
    private final String phoneNumber;
    private final String address;
    private final Status status;
    private final RegistrationSource registrationSource;
    private final boolean accountVerified;
    private final Long userId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
