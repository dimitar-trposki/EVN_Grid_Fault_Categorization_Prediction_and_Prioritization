package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.BatchStatus;

import java.time.LocalDateTime;

public record ExportBatchResponse(
        Long id,
        String type,
        String format,
        BatchStatus status,
        String downloadUrl,
        LocalDateTime createdAt
) {}
