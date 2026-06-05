package rw.gov.utility_billing_system.dto.response.reading;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class MeterReadingResponse {

    private final Long id;
    private final Long meterId;
    private final String meterNumber;
    private final BigDecimal previousReading;
    private final BigDecimal currentReading;
    private final LocalDate readingDate;
    private final int billingMonth;
    private final int billingYear;
    private final BigDecimal consumption;
    private final LocalDateTime createdAt;
}
