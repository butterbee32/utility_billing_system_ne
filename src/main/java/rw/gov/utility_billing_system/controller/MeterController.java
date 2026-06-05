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
import rw.gov.utility_billing_system.dto.request.meter.MeterRequest;
import rw.gov.utility_billing_system.dto.response.PageResponse;
import rw.gov.utility_billing_system.dto.response.meter.MeterResponse;
import rw.gov.utility_billing_system.service.MeterService;

@RestController
@RequestMapping("/api/meters")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = SwaggerTags.METERS, description = "Assign meters to customers — needs customerId from step 03")
public class MeterController {

    private final MeterService meterService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "01 - Create meter", description = "ROLE_ADMIN, OPERATOR | Use customerId from 03")
    public ResponseEntity<MeterResponse> create(@Valid @RequestBody MeterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(meterService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE')")
    @Operation(summary = "02 - List / search meters", description = "ROLE_ADMIN, OPERATOR, FINANCE")
    public ResponseEntity<PageResponse<MeterResponse>> getAll(
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(meterService.search(search, pageable));
        }
        return ResponseEntity.ok(meterService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE', 'CUSTOMER')")
    @Operation(summary = "03 - Get meter by ID", description = "All roles")
    public ResponseEntity<MeterResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(meterService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "04 - Update meter", description = "ROLE_ADMIN, OPERATOR")
    public ResponseEntity<MeterResponse> update(@PathVariable Long id, @Valid @RequestBody MeterRequest request) {
        return ResponseEntity.ok(meterService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "05 - Delete meter", description = "ROLE_ADMIN")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        meterService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
