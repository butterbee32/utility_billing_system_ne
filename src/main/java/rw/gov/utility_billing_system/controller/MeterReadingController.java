package rw.gov.utility_billing_system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.gov.utility_billing_system.config.SwaggerTags;
import rw.gov.utility_billing_system.dto.request.reading.MeterReadingRequest;
import rw.gov.utility_billing_system.dto.response.reading.MeterReadingResponse;
import rw.gov.utility_billing_system.service.MeterReadingService;

import java.util.List;

@RestController
@RequestMapping("/api/meter-readings")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = SwaggerTags.READINGS, description = "Operator captures readings — login as OPERATOR first")
public class MeterReadingController {

    private final MeterReadingService meterReadingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "01 - Capture meter reading", description = "ROLE_OPERATOR | Use meterId from 04")
    public ResponseEntity<MeterReadingResponse> create(@Valid @RequestBody MeterReadingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(meterReadingService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE')")
    @Operation(summary = "02 - List all readings", description = "ROLE_ADMIN, OPERATOR, FINANCE")
    public ResponseEntity<List<MeterReadingResponse>> getAll() {
        return ResponseEntity.ok(meterReadingService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE')")
    @Operation(summary = "03 - Get reading by ID", description = "ROLE_ADMIN, OPERATOR, FINANCE")
    public ResponseEntity<MeterReadingResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(meterReadingService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "04 - Update reading", description = "ROLE_OPERATOR | Only if no bill linked")
    public ResponseEntity<MeterReadingResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody MeterReadingRequest request) {
        return ResponseEntity.ok(meterReadingService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "05 - Delete reading", description = "ROLE_ADMIN")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        meterReadingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
