package rw.gov.utility_billing_system.dto.response.tariff;

import lombok.Builder;
import lombok.Getter;
import rw.gov.utility_billing_system.enums.MeterType;
import rw.gov.utility_billing_system.enums.TariffType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class TariffResponse {

    private final Long id;
    private final String name;
    private final MeterType meterType;
    private final TariffType tariffType;
    private final BigDecimal flatRate;
    private final BigDecimal fixedServiceCharge;
    private final BigDecimal taxRate;
    private final BigDecimal penaltyRate;
    private final int version;
    private final LocalDate effectiveFrom;
    private final LocalDate effectiveTo;
    private final boolean active;
    private final List<TariffTierResponse> tiers;
    private final LocalDateTime createdAt;
}
