package rw.gov.utility_billing_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.gov.utility_billing_system.entity.UploadedFile;
import rw.gov.utility_billing_system.enums.FileCategory;

import java.util.List;
import java.util.Optional;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {

    List<UploadedFile> findByEntityTypeAndEntityId(String entityType, Long entityId);

    Optional<UploadedFile> findByEntityTypeAndEntityIdAndCategory(
            String entityType, Long entityId, FileCategory category);
}
