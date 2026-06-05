package rw.gov.utility_billing_system.dto.response.tariff;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class TariffTierResponse {

    private final Long id;
    private final BigDecimal minConsumption;
    private final BigDecimal maxConsumption;
    private final BigDecimal ratePerUnit;
}
