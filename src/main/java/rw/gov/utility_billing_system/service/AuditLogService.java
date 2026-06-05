package rw.gov.utility_billing_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import rw.gov.utility_billing_system.dto.response.audit.AuditLogResponse;
import rw.gov.utility_billing_system.dto.response.PageResponse;
import rw.gov.utility_billing_system.entity.AuditLog;
import rw.gov.utility_billing_system.enums.AuditAction;
import rw.gov.utility_billing_system.exception.ResourceNotFoundException;
import rw.gov.utility_billing_system.mapper.EntityMapper;
import rw.gov.utility_billing_system.repository.AuditLogRepository;
import rw.gov.utility_billing_system.security.SecurityUtils;
import rw.gov.utility_billing_system.utility.RequestContextHolder;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(AuditAction action, String entityType, Long entityId, String details) {
        AuditLog log = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .performedBy(SecurityUtils.getCurrentUserEmail())
                .ipAddress(RequestContextHolder.getClientIp())
                .userAgent(RequestContextHolder.getUserAgent())
                .build();
        auditLogRepository.save(log);
    }

    public AuditLogResponse getById(Long id) {
        return EntityMapper.toAuditLogResponse(findById(id));
    }

    public PageResponse<AuditLogResponse> getAll(Pageable pageable) {
        return PageResponse.from(auditLogRepository.findAll(pageable).map(EntityMapper::toAuditLogResponse));
    }

    public PageResponse<AuditLogResponse> searchByUser(String user, Pageable pageable) {
        Page<AuditLogResponse> page = auditLogRepository
                .findByPerformedByContainingIgnoreCase(user, pageable)
                .map(EntityMapper::toAuditLogResponse);
        return PageResponse.from(page);
    }

    public void delete(Long id) {
        auditLogRepository.delete(findById(id));
    }

    private AuditLog findById(Long id) {
        return auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audit log not found: " + id));
    }
}
