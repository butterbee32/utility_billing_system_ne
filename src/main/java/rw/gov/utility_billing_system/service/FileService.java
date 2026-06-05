package rw.gov.utility_billing_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rw.gov.utility_billing_system.dto.response.file.FileResponse;
import rw.gov.utility_billing_system.entity.UploadedFile;
import rw.gov.utility_billing_system.enums.AuditAction;
import rw.gov.utility_billing_system.enums.FileCategory;
import rw.gov.utility_billing_system.exception.ResourceNotFoundException;
import rw.gov.utility_billing_system.mapper.EntityMapper;
import rw.gov.utility_billing_system.repository.UploadedFileRepository;
import rw.gov.utility_billing_system.utility.FileStorageService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {

    private final UploadedFileRepository uploadedFileRepository;
    private final FileStorageService fileStorageService;
    private final AuditLogService auditLogService;

    @Transactional
    public FileResponse uploadProfilePicture(Long userId, MultipartFile file) {
        return upload(file, FileCategory.PROFILE_PICTURE, "User", userId, "profile-pictures");
    }

    @Transactional
    public FileResponse uploadCustomerDocument(Long customerId, MultipartFile file) {
        return upload(file, FileCategory.CUSTOMER_DOCUMENT, "Customer", customerId, "customer-documents");
    }

    public FileResponse getById(Long id) {
        return EntityMapper.toFileResponse(findById(id));
    }

    public List<FileResponse> getByEntity(String entityType, Long entityId) {
        return uploadedFileRepository.findByEntityTypeAndEntityId(entityType, entityId).stream()
                .map(EntityMapper::toFileResponse)
                .toList();
    }

    public Resource download(Long id) {
        UploadedFile file = findById(id);
        return fileStorageService.loadAsResource(file.getFilePath());
    }

    @Transactional
    public void delete(Long id) {
        UploadedFile file = findById(id);
        uploadedFileRepository.delete(file);
        auditLogService.log(AuditAction.DELETE, "UploadedFile", id, "File deleted");
    }

    private FileResponse upload(MultipartFile file, FileCategory category,
                                String entityType, Long entityId, String subDir) {
        String path = fileStorageService.store(file, subDir);
        UploadedFile uploadedFile = UploadedFile.builder()
                .storedFileName(path.substring(path.lastIndexOf(java.io.File.separator) + 1))
                .originalFileName(file.getOriginalFilename())
                .filePath(path)
                .category(category)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .entityType(entityType)
                .entityId(entityId)
                .build();
        uploadedFileRepository.save(uploadedFile);
        auditLogService.log(AuditAction.CREATE, "UploadedFile", uploadedFile.getId(),
                category + " uploaded for " + entityType + " " + entityId);
        return EntityMapper.toFileResponse(uploadedFile);
    }

    private UploadedFile findById(Long id) {
        return uploadedFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + id));
    }
}
