package rw.gov.utility_billing_system.dto.request.tariff;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import rw.gov.utility_billing_system.enums.MeterType;
import rw.gov.utility_billing_system.enums.TariffType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class TariffRequest {

    @NotBlank(message = "Tariff name is required")
    private String name;

    @NotNull(message = "Meter type is required")
    private MeterType meterType;

    @NotNull(message = "Tariff type is required")
    private TariffType tariffType;

    private BigDecimal flatRate;

    @NotNull(message = "Fixed service charge is required")
    @DecimalMin(value = "0.0", message = "Fixed charge must be non-negative")
    private BigDecimal fixedServiceCharge;

    @NotNull(message = "Tax rate is required")
    @DecimalMin(value = "0.0", message = "Tax rate must be non-negative")
    private BigDecimal taxRate;

    @NotNull(message = "Penalty rate is required")
    @DecimalMin(value = "0.0", message = "Penalty rate must be non-negative")
    private BigDecimal penaltyRate;

    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    @Valid
    private List<TariffTierRequest> tiers;
}
