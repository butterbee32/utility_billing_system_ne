package rw.gov.utility_billing_system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rw.gov.utility_billing_system.config.SwaggerTags;
import rw.gov.utility_billing_system.dto.response.file.FileResponse;
import rw.gov.utility_billing_system.service.FileService;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = SwaggerTags.FILES, description = "Upload profile pictures and customer documents")
public class FileController {

    private final FileService fileService;

    @PostMapping(value = "/profile/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @Operation(summary = "01 - Upload profile picture", description = "ROLE_ADMIN, CUSTOMER | multipart file")
    public ResponseEntity<FileResponse> uploadProfile(@PathVariable Long userId,
                                                      @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(fileService.uploadProfilePicture(userId, file));
    }

    @PostMapping(value = "/customer/{customerId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "02 - Upload customer document", description = "ROLE_ADMIN, OPERATOR | multipart file")
    public ResponseEntity<FileResponse> uploadCustomerDoc(@PathVariable Long customerId,
                                                            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(fileService.uploadCustomerDocument(customerId, file));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "03 - Get file metadata", description = "Authenticated")
    public ResponseEntity<FileResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(fileService.getById(id));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "04 - List files by entity", description = "Authenticated | entityType=Customer or User")
    public ResponseEntity<List<FileResponse>> getByEntity(@PathVariable String entityType,
                                                          @PathVariable Long entityId) {
        return ResponseEntity.ok(fileService.getByEntity(entityType, entityId));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "05 - Download file", description = "Authenticated")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        FileResponse meta = fileService.getById(id);
        Resource resource = fileService.download(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(meta.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + meta.getOriginalFileName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "06 - Delete file", description = "ROLE_ADMIN")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fileService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
