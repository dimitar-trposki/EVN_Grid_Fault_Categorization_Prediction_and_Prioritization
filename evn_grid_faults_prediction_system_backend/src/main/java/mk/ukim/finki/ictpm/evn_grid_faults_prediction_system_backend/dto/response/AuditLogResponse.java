package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        Long userId,
        String userFullName,
        String entityName,
        Long entityId,
        String actionType,
        String oldValue,
        String newValue,
        LocalDateTime createdAt
) {}
