package rw.gov.utility_billing_system.dto.response.meter;

import lombok.Builder;
import lombok.Getter;
import rw.gov.utility_billing_system.enums.MeterType;
import rw.gov.utility_billing_system.enums.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class MeterResponse {

    private final Long id;
    private final String meterNumber;
    private final MeterType meterType;
    private final LocalDate installationDate;
    private final Status status;
    private final Long customerId;
    private final String customerName;
    private final LocalDateTime createdAt;
}
