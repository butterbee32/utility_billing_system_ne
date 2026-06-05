package rw.gov.utility_billing_system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.gov.utility_billing_system.config.SwaggerTags;
import rw.gov.utility_billing_system.dto.request.bill.BillGenerateRequest;
import rw.gov.utility_billing_system.dto.response.PageResponse;
import rw.gov.utility_billing_system.dto.response.bill.BillResponse;
import rw.gov.utility_billing_system.service.BillService;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = SwaggerTags.BILLS, description = "Generate → Approve → then pay in 08 - Payments")
public class BillController {

    private final BillService billService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "01 - Generate bill", description = "ADMIN/FINANCE | Use meterReadingId from 05")
    public ResponseEntity<BillResponse> generate(@Valid @RequestBody BillGenerateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(billService.generateBill(request));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "02 - Approve bill", description = "ROLE_FINANCE | Required before payment")
    public ResponseEntity<BillResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(billService.approve(id));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "03 - Reject bill", description = "ROLE_FINANCE")
    public ResponseEntity<BillResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(billService.reject(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    @Operation(summary = "04 - List / search bills", description = "All roles | ?customerId=1 or ?search=ref")
    public ResponseEntity<PageResponse<BillResponse>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long customerId,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        if (customerId != null) {
            return ResponseEntity.ok(billService.getByCustomer(customerId, pageable));
        }
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(billService.search(search, pageable));
        }
        return ResponseEntity.ok(billService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    @Operation(summary = "05 - Get bill by ID", description = "All roles")
    public ResponseEntity<BillResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(billService.getById(id));
    }

    @GetMapping("/reference/{reference}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    @Operation(summary = "06 - Get bill by reference", description = "All roles")
    public ResponseEntity<BillResponse> getByReference(@PathVariable String reference) {
        return ResponseEntity.ok(billService.getByReference(reference));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "07 - Delete bill", description = "ROLE_ADMIN | Not if PAID")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        billService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
