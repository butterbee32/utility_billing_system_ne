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
import rw.gov.utility_billing_system.dto.request.tariff.TariffRequest;
import rw.gov.utility_billing_system.dto.response.tariff.TariffResponse;
import rw.gov.utility_billing_system.service.TariffService;

import java.util.List;

@RestController
@RequestMapping("/api/tariffs")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = SwaggerTags.TARIFFS, description = "Configure pricing before generating bills — ROLE_ADMIN")
public class TariffController {

    private final TariffService tariffService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "01 - Create tariff", description = "ROLE_ADMIN | Do this before bill generation")
    public ResponseEntity<TariffResponse> create(@Valid @RequestBody TariffRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tariffService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "02 - List all tariffs", description = "ROLE_ADMIN, FINANCE")
    public ResponseEntity<List<TariffResponse>> getAll() {
        return ResponseEntity.ok(tariffService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "03 - Get tariff by ID", description = "ROLE_ADMIN, FINANCE")
    public ResponseEntity<TariffResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(tariffService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "04 - Update tariff", description = "ROLE_ADMIN")
    public ResponseEntity<TariffResponse> update(@PathVariable Long id, @Valid @RequestBody TariffRequest request) {
        return ResponseEntity.ok(tariffService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "05 - Deactivate tariff", description = "ROLE_ADMIN")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tariffService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
