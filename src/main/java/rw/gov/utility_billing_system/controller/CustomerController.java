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
import rw.gov.utility_billing_system.dto.request.customer.CustomerRequest;
import rw.gov.utility_billing_system.dto.request.customer.CustomerSelfRegisterRequest;
import rw.gov.utility_billing_system.dto.response.PageResponse;
import rw.gov.utility_billing_system.dto.response.customer.CustomerResponse;
import rw.gov.utility_billing_system.service.CustomerService;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = SwaggerTags.CUSTOMERS, description = "Customers self-register (public) or admin/operator registers on their behalf")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/self-register")
    @Operation(summary = "01 - Self register (public)", description = "Public | Customer signs up online. Then verify OTP in 01 - Authentication")
    public ResponseEntity<CustomerResponse> selfRegister(@Valid @RequestBody CustomerSelfRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.selfRegister(request));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "02 - Create customer (admin)", description = "ROLE_ADMIN, OPERATOR | Includes national ID. Temp password emailed — no OTP needed")
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "03 - List / search customers", description = "ROLE_ADMIN, OPERATOR, FINANCE")
    public ResponseEntity<PageResponse<CustomerResponse>> getAll(
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(customerService.search(search, pageable));
        }
        return ResponseEntity.ok(customerService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE', 'CUSTOMER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "04 - Get customer by ID", description = "All roles")
    public ResponseEntity<CustomerResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "05 - Update customer", description = "ROLE_ADMIN, ROLE_OPERATOR")
    public ResponseEntity<CustomerResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "06 - Delete customer", description = "ROLE_ADMIN")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
