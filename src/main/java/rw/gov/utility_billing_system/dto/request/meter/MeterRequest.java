package rw.gov.utility_billing_system.dto.request.meter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import rw.gov.utility_billing_system.enums.MeterType;
import rw.gov.utility_billing_system.enums.Status;

import java.time.LocalDate;

@Getter
@Setter
public class MeterRequest {

    @NotBlank(message = "Meter number is required")
    private String meterNumber;

    @NotNull(message = "Meter type is required")
    private MeterType meterType;

    @NotNull(message = "Installation date is required")
    private LocalDate installationDate;

    private Status status;

    @NotNull(message = "Customer ID is required")
    private Long customerId;
}
