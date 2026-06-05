package rw.gov.utility_billing_system.dto.request.reading;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class MeterReadingRequest {

    @NotNull(message = "Meter ID is required")
    private Long meterId;

    @NotNull(message = "Previous reading is required")
    @DecimalMin(value = "0.0", message = "Previous reading must be non-negative")
    private BigDecimal previousReading;

    @NotNull(message = "Current reading is required")
    @DecimalMin(value = "0.01", message = "Current reading must be positive")
    private BigDecimal currentReading;

    @NotNull(message = "Reading date is required")
    private LocalDate readingDate;
}
