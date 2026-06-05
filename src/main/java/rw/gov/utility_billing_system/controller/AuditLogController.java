package rw.gov.utility_billing_system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.gov.utility_billing_system.config.SwaggerTags;
import rw.gov.utility_billing_system.dto.response.PageResponse;
import rw.gov.utility_billing_system.dto.response.audit.AuditLogResponse;
import rw.gov.utility_billing_system.service.AuditLogService;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = SwaggerTags.AUDIT, description = "Review system actions — ROLE_ADMIN")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "01 - List audit logs", description = "ROLE_ADMIN | ?user=email to filter")
    public ResponseEntity<PageResponse<AuditLogResponse>> getAll(
            @RequestParam(required = false) String user,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        if (user != null && !user.isBlank()) {
            return ResponseEntity.ok(auditLogService.searchByUser(user, pageable));
        }
        return ResponseEntity.ok(auditLogService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "02 - Get audit log by ID", description = "ROLE_ADMIN")
    public ResponseEntity<AuditLogResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(auditLogService.getById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "03 - Delete audit log", description = "ROLE_ADMIN")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        auditLogService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
