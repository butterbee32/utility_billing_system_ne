package rw.gov.utility_billing_system.dto.response.notification;

import lombok.Builder;
import lombok.Getter;
import rw.gov.utility_billing_system.enums.EmailStatus;
import rw.gov.utility_billing_system.enums.NotificationType;
import rw.gov.utility_billing_system.enums.ReadStatus;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponse {

    private final Long id;
    private final Long customerId;
    private final Long userId;
    private final String message;
    private final NotificationType type;
    private final ReadStatus readStatus;
    private final boolean emailSent;
    private final EmailStatus emailStatus;
    private final LocalDateTime sentAt;
    private final LocalDateTime createdAt;
}
