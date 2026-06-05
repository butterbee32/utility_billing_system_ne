package rw.gov.utility_billing_system.dto.request.bill;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BillGenerateRequest {

    @NotNull(message = "Meter reading ID is required")
    private Long meterReadingId;
}
