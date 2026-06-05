package rw.gov.utility_billing_system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.gov.utility_billing_system.config.SwaggerTags;
import rw.gov.utility_billing_system.dto.response.notification.NotificationResponse;
import rw.gov.utility_billing_system.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = SwaggerTags.NOTIFICATIONS, description = "Check after bill generation and payment")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    @Operation(summary = "01 - Get customer notifications", description = "Use customerId from step 03")
    public ResponseEntity<List<NotificationResponse>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(notificationService.getByCustomer(customerId));
    }

    @GetMapping("/customer/{customerId}/unread")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    @Operation(summary = "02 - Get unread notifications", description = "Use customerId from step 03")
    public ResponseEntity<List<NotificationResponse>> getUnread(@PathVariable Long customerId) {
        return ResponseEntity.ok(notificationService.getUnreadByCustomer(customerId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "03 - List all notifications", description = "ROLE_ADMIN, FINANCE")
    public ResponseEntity<List<NotificationResponse>> getAll() {
        return ResponseEntity.ok(notificationService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    @Operation(summary = "04 - Get notification by ID", description = "All roles")
    public ResponseEntity<NotificationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getById(id));
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    @Operation(summary = "05 - Mark notification as read", description = "All roles")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "06 - Delete notification", description = "ROLE_ADMIN")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        notificationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
