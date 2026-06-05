package rw.gov.utility_billing_system.dto.request.user;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import rw.gov.utility_billing_system.enums.Status;

@Getter
@Setter
public class StatusUpdateRequest {

    @NotNull(message = "Status is required")
    private Status status;
}
