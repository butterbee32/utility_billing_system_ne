package rw.gov.utility_billing_system.dto.request.tariff;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TariffTierRequest {

    @NotNull(message = "Minimum consumption is required")
    @DecimalMin(value = "0.0", message = "Minimum consumption must be non-negative")
    private BigDecimal minConsumption;

    private BigDecimal maxConsumption;

    @NotNull(message = "Rate per unit is required")
    @DecimalMin(value = "0.01", message = "Rate per unit must be positive")
    private BigDecimal ratePerUnit;
}
