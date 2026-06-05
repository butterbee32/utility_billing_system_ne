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
import rw.gov.utility_billing_system.dto.request.user.RoleUpdateRequest;
import rw.gov.utility_billing_system.dto.request.user.StaffCreateRequest;
import rw.gov.utility_billing_system.dto.request.user.StatusUpdateRequest;
import rw.gov.utility_billing_system.dto.request.user.UserRequest;
import rw.gov.utility_billing_system.dto.response.PageResponse;
import rw.gov.utility_billing_system.dto.response.user.UserResponse;
import rw.gov.utility_billing_system.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = SwaggerTags.USERS, description = "Admin manages users and staff — ROLE_ADMIN")
public class UserController {

    private final UserService userService;

    @PostMapping("/staff")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "01 - Create staff (Operator/Finance)", description = "ROLE_ADMIN | Sends temp password email")
    public ResponseEntity<UserResponse> createStaff(@Valid @RequestBody StaffCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createStaff(request));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "02 - Create user", description = "ROLE_ADMIN")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "03 - List / search users", description = "ROLE_ADMIN | ?search=keyword")
    public ResponseEntity<PageResponse<UserResponse>> getAll(
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(userService.search(search, pageable));
        }
        return ResponseEntity.ok(userService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "04 - Get user by ID", description = "ROLE_ADMIN")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "05 - Update user", description = "ROLE_ADMIN")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    @PatchMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "06 - Update user roles", description = "ROLE_ADMIN | Sends notification email")
    public ResponseEntity<UserResponse> updateRoles(@PathVariable Long id,
                                                    @Valid @RequestBody RoleUpdateRequest request) {
        return ResponseEntity.ok(userService.updateRoles(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "07 - Update user status", description = "ROLE_ADMIN")
    public ResponseEntity<UserResponse> updateStatus(@PathVariable Long id,
                                                     @Valid @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(userService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "08 - Delete user", description = "ROLE_ADMIN")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
