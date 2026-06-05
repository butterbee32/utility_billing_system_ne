package rw.gov.utility_billing_system.dto.response.bill;

import lombok.Builder;
import lombok.Getter;
import rw.gov.utility_billing_system.enums.BillStatus;
import rw.gov.utility_billing_system.enums.MeterType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class BillResponse {

    private final Long id;
    private final String billReference;
    private final Long customerId;
    private final String customerName;
    private final Long meterReadingId;
    private final int billingMonth;
    private final int billingYear;
    private final MeterType meterType;
    private final BigDecimal consumptionAmount;
    private final BigDecimal tariffAmount;
    private final BigDecimal fixedCharge;
    private final BigDecimal taxAmount;
    private final BigDecimal penaltyAmount;
    private final BigDecimal totalAmount;
    private final BigDecimal paidAmount;
    private final BigDecimal outstandingBalance;
    private final BillStatus status;
    private final LocalDateTime generatedAt;
    private final LocalDateTime approvedAt;
    private final LocalDateTime createdAt;
}
