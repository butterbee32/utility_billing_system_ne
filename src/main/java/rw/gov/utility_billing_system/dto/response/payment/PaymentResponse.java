package rw.gov.utility_billing_system.dto.response.payment;

import lombok.Builder;
import lombok.Getter;
import rw.gov.utility_billing_system.enums.PaymentMethod;
import rw.gov.utility_billing_system.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentResponse {

    private final Long id;
    private final String paymentReference;
    private final String billReference;
    private final BigDecimal amountPaid;
    private final PaymentMethod paymentMethod;
    private final LocalDate paymentDate;
    private final PaymentStatus paymentStatus;
    private final LocalDateTime processedAt;
    private final LocalDateTime createdAt;
}
