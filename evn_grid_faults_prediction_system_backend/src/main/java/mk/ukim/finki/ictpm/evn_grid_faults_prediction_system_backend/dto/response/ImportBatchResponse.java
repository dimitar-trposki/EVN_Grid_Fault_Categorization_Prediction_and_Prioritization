package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.BatchStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ImportBatchResponse(
        Long id,
        String fileName,
        String fileType,
        Integer totalRecords,
        Integer successfulRecords,
        Integer failedRecords,
        BatchStatus status,
        LocalDateTime createdAt,
        List<String> errors
) {}
