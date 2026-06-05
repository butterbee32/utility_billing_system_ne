package rw.gov.utility_billing_system.dto.response.audit;

import lombok.Builder;
import lombok.Getter;
import rw.gov.utility_billing_system.enums.AuditAction;

import java.time.LocalDateTime;

@Getter
@Builder
public class AuditLogResponse {

    private final Long id;
    private final AuditAction action;
    private final String entityType;
    private final Long entityId;
    private final String details;
    private final String performedBy;
    private final String ipAddress;
    private final String userAgent;
    private final LocalDateTime createdAt;
}
