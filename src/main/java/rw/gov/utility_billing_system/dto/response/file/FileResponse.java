package rw.gov.utility_billing_system.dto.response.file;

import lombok.Builder;
import lombok.Getter;
import rw.gov.utility_billing_system.enums.FileCategory;
import rw.gov.utility_billing_system.enums.UploadStatus;

import java.time.LocalDateTime;

@Getter
@Builder
public class FileResponse {

    private final Long id;
    private final String originalFileName;
    private final String fileDescription;
    private final UploadStatus uploadStatus;
    private final FileCategory category;
    private final String contentType;
    private final long fileSize;
    private final String entityType;
    private final Long entityId;
    private final LocalDateTime createdAt;
}
