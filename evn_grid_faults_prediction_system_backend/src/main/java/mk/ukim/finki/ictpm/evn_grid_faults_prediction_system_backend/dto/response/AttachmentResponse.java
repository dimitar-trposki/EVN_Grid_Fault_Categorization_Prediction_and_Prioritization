package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response;

import java.time.LocalDateTime;

public record AttachmentResponse(
        Long id,
        String fileName,
        String fileType,
        Long fileSize,
        LocalDateTime uploadedAt
) {}
